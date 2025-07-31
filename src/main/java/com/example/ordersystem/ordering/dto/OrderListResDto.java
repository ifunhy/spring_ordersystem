package com.example.ordersystem.ordering.dto;

import com.example.ordersystem.ordering.domain.OrderStatus;
import com.example.ordersystem.ordering.domain.Ordering;
import com.example.ordersystem.ordering.domain.OrderingDetail;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class OrderListResDto {
    private Long id;
    private String memberEmail;
    private OrderStatus orderStatus;
    List<OrderDetailResDto> orderDetails;

    public static OrderListResDto fromEntity(Ordering ordering){
        List<OrderDetailResDto> orderDetailResDtoList = new ArrayList<>();
        for (OrderingDetail orderDetail : ordering.getOrderingDetailList()){
            orderDetailResDtoList.add(OrderDetailResDto.fromEntity(orderDetail));
        }
        OrderListResDto dto = OrderListResDto.builder()
                .id(ordering.getId())
                .memberEmail(ordering.getMember().getEmail())
                .orderStatus(ordering.getOrderStatus())
                .orderDetails(orderDetailResDtoList)
                .build();
        return dto;
    }
}