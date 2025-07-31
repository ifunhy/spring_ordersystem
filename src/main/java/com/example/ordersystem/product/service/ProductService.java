package com.example.ordersystem.product.service;

import com.example.ordersystem.common.service.StockInventoryService;
import com.example.ordersystem.member.domain.Member;
import com.example.ordersystem.member.dto.MemberResDto;
import com.example.ordersystem.common.service.S3Uploader;
import com.example.ordersystem.member.repository.MemberRepository;
import com.example.ordersystem.product.domain.Product;
import com.example.ordersystem.product.dto.ProductCreateDto;
import com.example.ordersystem.product.dto.ProductResDto;
import com.example.ordersystem.product.dto.ProductSearchDto;
import com.example.ordersystem.product.dto.ProductUpdateDto;
import com.example.ordersystem.product.repository.ProductRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.parameters.P;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class ProductService {
    private final ProductRepository productRepository;
    private final MemberRepository memberRepository;
    private final S3Uploader s3Uploader;
    private final S3Client s3Client;
    private final StockInventoryService stockInventoryService;
    @Value("${cloud.aws.s3.bucket}")
    private String bucket;


//    public Long save(ProductCreateDto productCreateDto) {
//        String email = SecurityContextHolder.getContext().getAuthentication().getName();
//        Member member = memberRepository.findByEmail(email).orElseThrow(() -> new EntityNotFoundException("상품등록실패"));
//
//        String productImage = s3Uploader.upload(productCreateDto.getProductImage());
//
//        // Entity 변환 및 저장
//        Product product = productCreateDto.toEntity(member, productImage);
//        productRepository.save(product);
//        return (product.getId());
//    }

    // 강사님 코드
    public Long save(ProductCreateDto productCreateDto) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        Member member = memberRepository.findByEmail(email).orElseThrow(() -> new EntityNotFoundException("member is not found"));
        Product product = productRepository.save(productCreateDto.toEntity(member));

        if (productCreateDto.getProductImage() != null) {
//        image명 설정
            String fileName = "product-" + product.getId() + "-productImage-" + productCreateDto.getProductImage().getOriginalFilename();

//        저장 객체 구성
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucket)
                    .key(fileName)
                    .contentType(productCreateDto.getProductImage().getContentType()) //image/jpeg, video/mp4 ...
                    .build();

//        이미지를 업로드(byte형태로)
            try {
                s3Client.putObject(putObjectRequest, RequestBody.fromBytes(productCreateDto.getProductImage().getBytes()));
            } catch (Exception e) {
//            checked -> unchecked로 바꿔 전체 rollback되도록 예외처리
                throw new IllegalArgumentException("이미지 업로드 실패");
            }

//        // 이미지 삭제 시
//        s3Client.deleteObject(a -> a.bucket(버킷명).key(파일명));

//        이미지 url추출
            String imgUrl = s3Client.utilities().getUrl(a -> a.bucket(bucket).key(fileName)).toExternalForm();
            product.updateImageUrl(imgUrl);
        }
        // 상품등록 시 redis에 재고 세팅
        stockInventoryService.makeStockQuantity(product.getId(), product.getStockQuantity());
        return product.getId();
    }


    // 상품목록조회
    public Page<ProductResDto> findAll(Pageable pageable, ProductSearchDto productSearchDto){
//        return productRepository.findAll().stream().map(p->ProductResDto.fromEntity(p)).collect(Collectors.toList());
        Specification<Product> specification = new Specification<Product>() {
            @Override
            public Predicate toPredicate(Root<Product> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {
                // Root : Entity의 속성을 접근하기 위한 객체, CriteriaBuilder : 쿼리를 생성하기 위한 객체
                List<Predicate> predicateList = new ArrayList<>();  // Predicate는 조건이라 생각하면 됨

                if (productSearchDto.getCategory() != null) {    // and category=inputCategory (동적)
                    predicateList.add(criteriaBuilder.equal(root.get("category"), productSearchDto.getCategory()));
                }
                if (productSearchDto.getProductName() != null) {   // and title like"%inputTitle%" (동적)
                    predicateList.add(criteriaBuilder.like(root.get("name"), "%" + productSearchDto.getProductName() + "%"));
                }
                Predicate[] predicateArr = new Predicate[predicateList.size()];

                for (int i = 0; i < predicateList.size(); i++) {
                    predicateArr[i] = predicateList.get(i);
                }

                // 위의 검색 조건들을 하나(한줄)의 Predicate형 객체로 만들어서 return
                Predicate predicate = criteriaBuilder.and(predicateArr);
                return (predicate);    // Predicate형 객체로 리턴
            }
        };
        Page<Product> productList = productRepository.findAll(specification ,pageable);  // 삭제되지 않고, 예약하지 않은 상품만 조회
        return (productList.map(p -> ProductResDto.fromEntity(p)));
    }

    public ProductResDto findById(Long id){
        Product product = productRepository.findById(id).orElseThrow(()->new EntityNotFoundException("상품정보없음"));
        return ProductResDto.fromEntity(product);
    }

    // 상품수정
    // 기존이미지 삭제 후 새로운 이미지 등록 및 url 변경
    public Long update(ProductUpdateDto productUpdateDto, Long productId) {
        Product product = productRepository.findById(productId).orElseThrow(() -> new EntityNotFoundException("해당 상품이 없습니다."));
        product.updateProduct(productUpdateDto);    // 이미지 외 나머지 사항들 업데이트

        if (productUpdateDto.getProductImage() != null && !productUpdateDto.getProductImage().isEmpty()) {

            // 기존이미지 삭제 : 파일명으로 삭제
            String imgUrl = product.getImagePath();
            String fileName = imgUrl.substring(imgUrl.lastIndexOf("/") + 1);    // 슬래시 이후의 값만 잘라냄
            s3Client.deleteObject(a -> a.bucket(bucket).key(fileName));

            // 신규이미지 등록
            String newFileName = "product-" + product.getId() + "-productImage-" + productUpdateDto.getProductImage().getOriginalFilename();
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucket)
                    .key(newFileName)
                    .contentType(productUpdateDto.getProductImage().getContentType()) //image/jpeg, video/mp4 ...
                    .build();

            try {
                s3Client.putObject(putObjectRequest, RequestBody.fromBytes(productUpdateDto.getProductImage().getBytes()));
            } catch (Exception e) {
                throw new IllegalArgumentException("이미지 업로드 실패");
            }


            String newImgUrl = s3Client.utilities().getUrl(a -> a.bucket(bucket).key(fileName)).toExternalForm();
            product.updateImageUrl(newImgUrl);
        } else {
            // s3에서 이미지 삭제 후 url 갱신
            product.updateImageUrl(null);   // 이미지를 넣지 않고 업데이트
        }
        return product.getId();
    }

}
