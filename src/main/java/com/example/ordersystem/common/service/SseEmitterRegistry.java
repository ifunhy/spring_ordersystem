package com.example.ordersystem.common.service;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class SseEmitterRegistry {
    // SseEmitter는 연결된 사용자정보(ip, macaddress 정보 등)를 의미
    // ConcurrentHashMap은 Thread-Safe한 map (동시성 이슈 발생 x)
    private Map<String, SseEmitter> emitterMap = new ConcurrentHashMap<>(); // key는 email

    public SseEmitter getEmitter(String email) {
        return emitterMap.get(email);
    }

    public void addSseEmitter(String email, SseEmitter sseEmitter) {
        emitterMap.put(email, sseEmitter);
    }

    public void removeEmitter(String email) {
        emitterMap.remove(email);
    }
}
