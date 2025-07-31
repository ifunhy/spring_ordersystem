package com.example.ordersystem.product.dto;

import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class ProductUpdateDto {
    @NotEmpty(message = "이름은 필수 입력 항목입니다.")
    private String name;

    @NotEmpty(message = "카테고리는 필수 입력 항목입니다.")
    private String category;

    private Integer price;

    private Integer stockQuantity;

    private MultipartFile productImage;
}
