package com.example.ordersystem.ordering.service;

import com.example.ordersystem.common.service.StockInventoryService;
import com.example.ordersystem.common.service.StockRabbitMqService;
import com.example.ordersystem.member.domain.Member;
import com.example.ordersystem.member.repository.MemberRepository;
import com.example.ordersystem.ordering.domain.Ordering;
import com.example.ordersystem.ordering.domain.OrderingDetail;
import com.example.ordersystem.ordering.dto.OrderCreateDto;
import com.example.ordersystem.ordering.dto.OrderDetailResDto;
import com.example.ordersystem.ordering.dto.OrderListResDto;
import com.example.ordersystem.ordering.repository.OrderDetailRepository;
import com.example.ordersystem.ordering.repository.OrderingRepository;
import com.example.ordersystem.product.domain.Product;
import com.example.ordersystem.product.dto.ProductResDto;
import com.example.ordersystem.product.dto.ProductSearchDto;
import com.example.ordersystem.product.repository.ProductRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.criteria.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class OrderingService {
    private final OrderingRepository orderingRepository;
    private final MemberRepository memberRepository;
    private final ProductRepository productRepository;
    private final OrderDetailRepository orderDetailRepository;
    private final StockInventoryService stockInventoryService;
    private final StockRabbitMqService stockRabbitMqService;

    // 주문
    public Long create(List<OrderCreateDto> orderCreateDtoList) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        Member member = memberRepository.findByEmail(email).orElseThrow(() -> new EntityNotFoundException("member is not found"));

        Ordering ordering = Ordering.builder()
                .member(member)
                .build();

        for (OrderCreateDto dto : orderCreateDtoList) {
            // 상품 먼저 찾음
            Product product = productRepository.findById(dto.getProductId()).orElseThrow(() -> new EntityNotFoundException("해당 상품이 없습니다."));

            // 재고 부족할 경우
            if (product.getStockQuantity() < dto.getProductCount()) {
                // 예외를 강제 발생시킴으로서, 모두 임시저장사항들은 rollback 처리
                throw new IllegalArgumentException("재고가 부족합니다.");
            }

            // 발생 가능한 이슈 2가지
            // 1. 동시에 접근하는 상황에서 update값의 정합성이 깨지고 갱신 이상(lost update)이 발생
            // 2. spring 버전 또는 mysql 버전에 따라 JPA에서 강제 에러(deadlock)를 유발시켜 대부분의 요청실패 발생
            product.updateStockQuantity(dto.getProductCount()); // 기존 재고에서 빼고자 하는 재고 처리(lostUpdate 처리)

            // 주문상세 객체 생성 후 cascading
            OrderingDetail orderingDetail = OrderingDetail.builder()
                    .product(product)
                    .quantity(dto.getProductCount())
                    .ordering(ordering)
                    .build();
//            orderDetailRepository.save(orderingDetail);
            ordering.getOrderingDetailList().add(orderingDetail);
        }
        orderingRepository.save(ordering);
        return ordering.getId();
    }

    // redis에서 재고수량 관리
    @Transactional(isolation = Isolation.READ_COMMITTED)    // 격리레벨을 낮춤으로써, 성능향상과 lock관련 문제 원천 차단
    public Long createConcurrent(List<OrderCreateDto> orderCreateDtoList) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        Member member = memberRepository.findByEmail(email).orElseThrow(() -> new EntityNotFoundException("member is not found"));

        Ordering ordering = Ordering.builder()
                .member(member)
                .build();

        for (OrderCreateDto dto : orderCreateDtoList) {
            // 상품 먼저 찾음
            Product product = productRepository.findById(dto.getProductId()).orElseThrow(() -> new EntityNotFoundException("해당 상품이 없습니다."));

            // redis에서 재고수량 확인 및 재고수량 감소 처리
            int newQuantity = stockInventoryService.decreaseStockQuantity(product.getId(), dto.getProductCount());

            if (newQuantity < 0) {
                throw new IllegalArgumentException("재고가 부족합니다.");
            }

            // 주문상세 객체 생성 후 cascading
            OrderingDetail orderingDetail = OrderingDetail.builder()
                    .product(product)
                    .quantity(dto.getProductCount())
                    .ordering(ordering)
                    .build();
            ordering.getOrderingDetailList().add(orderingDetail);
            // rdb에 사후 update를 위한 메시지 발행 (비동기처리)
            stockRabbitMqService.publish(dto.getProductId(), dto.getProductCount());
        }
        orderingRepository.save(ordering);


        return ordering.getId();
    }

    // 주문목록조회
    public List<OrderListResDto> findAll(){
        return orderingRepository.findAll().stream()
                .map(o -> OrderListResDto.fromEntity(o)).collect(Collectors.toList());
    }

    // 내주문목록조회
    public List<OrderListResDto> findAllByMember(){
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        Member member = memberRepository.findByEmail(email).orElseThrow(() -> new EntityNotFoundException("member is not found"));

        return orderingRepository.findAllByMember(member).stream()
                .map(o -> OrderListResDto.fromEntity(o)).collect(Collectors.toList());
    }
}
