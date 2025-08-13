package com.example.ordersystem.member.service;

import com.example.ordersystem.member.domain.Member;
import com.example.ordersystem.member.dto.MemberCreateDto;
import com.example.ordersystem.member.dto.LoginReqDto;
import com.example.ordersystem.member.dto.MemberResDto;
import com.example.ordersystem.member.repository.MemberRepository;
import com.example.ordersystem.member.repository.MemberRepositoryImpl;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.stream.Collectors;

@Transactional
@RequiredArgsConstructor
@Service
public class MemberService {

    private final MemberRepository memberRepository;
    private final MemberRepositoryImpl memberRepositoryImpl;
    private final PasswordEncoder passwordEncoder;

    public Long save(MemberCreateDto memberCreateDto) {
        if (memberRepository.findByEmail(memberCreateDto.getEmail()).isPresent()) {
            throw new IllegalArgumentException("이미 존재하는 이메일입니다.");
        }

        String hashedPassword = passwordEncoder.encode(memberCreateDto.getPassword());
        Member member = memberCreateDto.authorToEntity(hashedPassword);
        memberRepository.save(member);
        return member.getId();
    }

    public Member doLogin(LoginReqDto loginReqDto){
        Optional<Member> optAuthor = memberRepository.findByEmail(loginReqDto.getEmail());
        boolean check = true;

        if(!optAuthor.isPresent()){
            check = false;
        } else {
            // 비밀번호 일치 여부 검증 : matches를 통해서 암호화 되지 않은 값을 다시 암호화 하여 db의 password를 검증
            check = passwordEncoder.matches(loginReqDto.getPassword(), optAuthor.get().getPassword());
        }

        if(!check){
            throw new IllegalArgumentException("email 혹은 비밀번호가 일치하지 않습니다!");
        }

        return optAuthor.get();
    }

    public List<MemberResDto> findAll() {
        return memberRepository
                .findAll()
                .stream()
                .map(MemberResDto::fromEntity)
                .collect(Collectors.toList());
    }

    public MemberResDto findByEmail() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();

        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new NoSuchElementException("사용자를 찾을 수 없습니다."));

        return MemberResDto.fromEntity(member);
    }

    public String softDeleteByEmail() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();

        memberRepository.findByEmail(email)
                .orElseThrow(() -> new NoSuchElementException("사용자를 찾을 수 없습니다."));

        memberRepositoryImpl.softDeleteByEmail(email);

        return email;
    }

    public MemberResDto findById(Long id){
        Member member = memberRepository.findById(id).orElseThrow(()->new EntityNotFoundException("member is not found"));
        return MemberResDto.fromEntity(member);
    }
}