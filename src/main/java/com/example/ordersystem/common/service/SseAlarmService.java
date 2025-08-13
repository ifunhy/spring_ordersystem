package com.example.ordersystem.common.service;

import com.example.ordersystem.common.dto.SseMessageDto;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.core.Message;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Component
public class SseAlarmService implements MessageListener {
    private final SseEmitterRegistry sseEmitterRegistry;
    private final RedisTemplate<String, String> redisTemplate;

    public SseAlarmService(SseEmitterRegistry sseEmitterRegistry, @Qualifier("ssePubSub") RedisTemplate<String, String> redisTemplate) {
        this.sseEmitterRegistry = sseEmitterRegistry;
        this.redisTemplate = redisTemplate;
    }

    // 특정 사용자에게 message 발송
    public void publishMessage(String receiver, String sender, Long orderingId) {
        SseMessageDto dto = SseMessageDto.builder()
                .sender(sender)
                .receiver(receiver)
                .orderingId(orderingId)
                .build();
        // json으로 변환
        ObjectMapper objectMapper = new ObjectMapper();
        String data = null;
        try {
            data = objectMapper.writeValueAsString(dto);    // data는 dto 객체
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        // emitter객체를 통해 메시지 전송
        SseEmitter sseEmitter = sseEmitterRegistry.getEmitter(receiver);   // receiver email을 꺼내 emitter객체를 찾기 위함
        // emitter객체가 현재 서버에 있으면, 직접 알림 발송. 그렇지 않으면, redis에 publish
        if (sseEmitter != null) {
            try {
                sseEmitter.send(SseEmitter.event().name("ordered").data(data)); // 이벤트이름, json형태의 주문정보를 send
            // 사용자가 로그아웃(새로고침) 후 다시 화면에 들어왔을 때 알림메시지가 남아있으려면 DB에 추가적으로 저장 필요
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            redisTemplate.convertAndSend("order-channel", data);    // redis 리스너 객체인 redisMessageListenerContainer() 주문채널에 data를 넘김
        }
    }

    @Override
    public void onMessage(org.springframework.data.redis.connection.Message message, byte[] pattern) {
        // Message : 실질적인 메시지가 담겨있는 객체
        // pattern : 채널명
        String channel_name = new String(pattern);  // 여러 개의 채널을 구독하고 있을 경우, 채널명으로 분기처리
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            SseMessageDto dto = objectMapper.readValue(message.getBody(), SseMessageDto.class);


            SseEmitter sseEmitter = sseEmitterRegistry.getEmitter(dto.getReceiver());   // receiver email을 꺼내 emitter객체를 찾기 위함
            // emitter객체가 현재 서버에 있으면, 직접 알림 발송. 그렇지 않으면, redis에 publish
            if (sseEmitter != null) {
                try {
                    sseEmitter.send(SseEmitter.event().name("ordered").data(dto)); // 이벤트이름, json형태의 주문정보를 send
                    // 사용자가 로그아웃(새로고침) 후 다시 화면에 들어왔을 때 알림메시지가 남아있으려면 DB에 추가적으로 저장 필요
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
