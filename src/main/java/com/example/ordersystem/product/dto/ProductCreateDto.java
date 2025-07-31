package com.example.ordersystem.product.dto;
import com.example.ordersystem.member.domain.Member;
import com.example.ordersystem.product.domain.Product;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.multipart.MultipartFile;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class ProductCreateDto {
    @NotEmpty(message = "이름은 필수 입력 항목입니다.")
    private String name;

    @NotEmpty(message = "카테고리는 필수 입력 항목입니다.")
    private String category;

    private Integer price;

    private Integer stockQuantity;

    private MultipartFile productImage;

    public Product toEntity(Member member){
        return Product.builder()
                .name(this.name)
                .category(this.category)
                .price(this.price)
                .stockQuantity(this.stockQuantity)
                .member(member)
                .build();
    }
}
