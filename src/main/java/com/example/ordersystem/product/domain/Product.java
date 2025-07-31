package com.example.ordersystem.product.domain;

import com.example.ordersystem.common.domain.BaseTimeEntity;
import com.example.ordersystem.member.domain.Member;
import com.example.ordersystem.product.dto.ProductUpdateDto;
import jakarta.persistence.*;
import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@ToString
@Builder
@Entity
public class Product extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private String category;
    private Integer price;
    private Integer stockQuantity;  // 재고

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;
    private String imagePath;

    // 이미지 재업로드
    public void updateImageUrl(String imgUrl) {
        this.imagePath = imgUrl;
    }

    // 상품정보 업데이트
    public void updateProduct(ProductUpdateDto updateDto) {
        this.name = updateDto.getName();
        this.price = updateDto.getPrice();
        this.category = updateDto.getCategory();
        this.stockQuantity = updateDto.getStockQuantity();
    }

    // 재고 갱신
    public void updateStockQuantity(int orderQuantity) {
        this.stockQuantity = this.getStockQuantity() - orderQuantity;
    }
}