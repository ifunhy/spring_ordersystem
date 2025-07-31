package com.example.ordersystem.common.service;

import com.example.ordersystem.common.dto.StockRabbitMqDto;
import com.example.ordersystem.product.domain.Product;
import com.example.ordersystem.product.repository.ProductRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityNotFoundException;
import lombok.Builder;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

// 큐에 메시지 발행, 수신
@Component
@RequiredArgsConstructor
public class StockRabbitMqService {
    private final RabbitTemplate rabbitTemplate;
    private final ProductRepository productRepository;

    // rabbitmq에 메시지 발행
    public void publish(Long productId, int productCount) {
        StockRabbitMqDto dto = StockRabbitMqDto.builder()
                                                .productId(productId)
                                                .productCount(productCount)
                                                .build();

        rabbitTemplate.convertAndSend("stockDecreaseQueue", dto);    // ("문자열", 객체)
    }

    // rabbitmq에 발행된 메시지를 수신
    // listener는 단일스레드로 메시지를 처리하므로, 동시성 이슈 발생 X
    @RabbitListener(queues = "stockDecreaseQueue")
    @Transactional
    public void subscribe(Message message) throws JsonProcessingException {
        String messageBody = new String(message.getBody()); // message를 Body부분으로 꺼내 String으로 형변환
        ObjectMapper objectMapper = new ObjectMapper();
        StockRabbitMqDto dto = objectMapper.readValue(messageBody, StockRabbitMqDto.class); // 파싱
        Product product = productRepository.findById(dto.getProductId())
                .orElseThrow(() -> new EntityNotFoundException("해당 상품이 없습니다."));
        product.updateStockQuantity(dto.getProductCount());
        System.out.println(messageBody);
    }
}
