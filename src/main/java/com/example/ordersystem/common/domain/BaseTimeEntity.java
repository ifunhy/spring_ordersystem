package com.example.ordersystem.common.domain;

import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

// 기본적으로 Entity는 상속 불가능. MappedSuperclass어노테이션 사용 시 상속 가능
@MappedSuperclass
@Getter
public class BaseTimeEntity {
    // 컬럼명에 캐멀케이스 사용 시, DB에는 created_time으로 컬럼 생성
    @CreationTimestamp
    private LocalDateTime createdTime;  // 등록시간
    @UpdateTimestamp
    private LocalDateTime updatedTime;  // 업데이트시간
}
