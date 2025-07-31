package com.example.ordersystem.common.service;

import com.example.ordersystem.member.domain.Member;
import com.example.ordersystem.member.domain.Role;
import com.example.ordersystem.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

// CommandLineRunner를 구현함으로써 해당 컴포넌트가 스프링 빈으로 등록되는 시점에 run 메서드 자동 실행
@Component
@RequiredArgsConstructor
public class InitialDataLoader implements CommandLineRunner {
    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        if(memberRepository.findByEmail("admin@naver.com").isPresent()) {
            return;
        }

        Member member = Member.builder()
                .email("admin@naver.com")
                .role(Role.ADMIN)
                .password(passwordEncoder.encode("12341234"))
                .build();

        memberRepository.save(member);
    }
}
