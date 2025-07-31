package com.example.ordersystem.ordering.controller;

import com.example.ordersystem.common.dto.CommonDto;
import com.example.ordersystem.ordering.dto.OrderCreateDto;
import com.example.ordersystem.ordering.dto.OrderListResDto;
import com.example.ordersystem.ordering.service.OrderingService;
import com.example.ordersystem.product.dto.ProductResDto;
import com.example.ordersystem.product.dto.ProductSearchDto;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/ordering")
public class OrderingController {
    private final OrderingService orderingService;

    // 주문
    @PostMapping("/create")
    public ResponseEntity<?> create(@RequestBody List<OrderCreateDto> orderCreateDtos) {
        Long id = orderingService.createConcurrent(orderCreateDtos);

        return  new ResponseEntity<>(
                CommonDto.builder()
                        .result(id)
                        .status_code(HttpStatus.CREATED.value())
                        .status_message("주문완료")
                        .build(),
                HttpStatus.CREATED
        );
    }

    // 주문목록조회
    @GetMapping("/list")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> findAll() {
        List<OrderListResDto> orderListResDtos = orderingService.findAll();
        return new ResponseEntity<>(
                CommonDto.builder()
                        .result(orderListResDtos)
                        .status_code(HttpStatus.OK.value())
                        .status_message("주문목록조회성공")
                        .build(),
                HttpStatus.OK
        );
    }

    // 내주문목록조회
    @GetMapping("/myorders")
    public ResponseEntity<?> myOrders() {
        List<OrderListResDto> orderListResDtos = orderingService.findAllByMember();
        return new ResponseEntity<>(
                CommonDto.builder()
                        .result(orderListResDtos)
                        .status_code(HttpStatus.OK.value())
                        .status_message("내주문목록조회성공")
                        .build(),
                HttpStatus.OK
        );
    }
}