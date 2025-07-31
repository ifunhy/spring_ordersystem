package com.example.ordersystem.ordering.dto;

import com.example.ordersystem.ordering.domain.OrderingDetail;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class OrderDetailResDto {
    private Long detailId;
    private String productName;
    private Integer productCount;

    // 작업공통화
    public static OrderDetailResDto fromEntity(OrderingDetail orderingDetail) {

        OrderDetailResDto orderDetailResDto = OrderDetailResDto.builder()
                .detailId(orderingDetail.getId())
                .productName(orderingDetail.getProduct().getName())
                .productCount(orderingDetail.getQuantity())
                .build();
        return orderDetailResDto;
    }
}
