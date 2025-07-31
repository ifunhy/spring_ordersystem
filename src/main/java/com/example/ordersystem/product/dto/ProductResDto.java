package com.example.ordersystem.product.dto;

import com.example.ordersystem.member.domain.Member;
import com.example.ordersystem.member.dto.MemberResDto;
import com.example.ordersystem.product.domain.Product;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class ProductResDto {
    private Long id;
    private String name;
    private String category;
    private Integer price;
    private Integer stockQuantity;
    private String productImage;

    public static ProductResDto fromEntity(Product product) {
        return (ProductResDto.builder()
                .id(product.getId())
                .name(product.getName())
                .category(product.getCategory())
                .price(product.getPrice())
                .stockQuantity(product.getStockQuantity())
                .productImage(product.getImagePath())
                .build());
    }
}
