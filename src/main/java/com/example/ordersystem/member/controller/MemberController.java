package com.example.ordersystem.member.controller;

import com.example.ordersystem.common.auth.JwtTokenProvider;
import com.example.ordersystem.common.dto.CommonDto;
import com.example.ordersystem.member.domain.Member;
import com.example.ordersystem.member.dto.*;
import com.example.ordersystem.member.service.MemberService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@SpringBootApplication
@RestController
@RequiredArgsConstructor
@RequestMapping("/member")
public class MemberController {
    private final MemberService memberService;
    private final JwtTokenProvider jwtTokenProvider;

    // 회원가입
    @PostMapping("/create")
    public ResponseEntity<?> save(@RequestBody @Valid MemberCreateDto memberCreateDto) {
        try {
            Long id = this.memberService.save(memberCreateDto);
            return (new ResponseEntity<>(
                    CommonDto.builder()
                            .result(id)
                            .status_code(HttpStatus.CREATED.value())
                            .status_message("회원가입 완료!")
                            .build(),
                    HttpStatus.CREATED));
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
            return (new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST));
        }
    }

    // 로그인
    @PostMapping("/doLogin")
    public ResponseEntity<?> doLogin(@RequestBody @Valid LoginReqDto loginReqDto){
        Member member = memberService.doLogin(loginReqDto);

        // AT 토큰 생성
        String accessToken = jwtTokenProvider.createAtToken(member);

        // RT 토큰 생성
        String refreshToken = jwtTokenProvider.createRtToken(member);

        LoginResDto loginResDto = LoginResDto.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();

        return new ResponseEntity<>(
                CommonDto.builder()
                        .result(loginResDto)
                        .status_code(
                                HttpStatus.OK.value())
                        .status_message("로그인 성공")
                        .build(),
                HttpStatus.OK);
    }

    // RT를 통한 AT 갱신 요청
    @PostMapping("/refresh-at")
    public ResponseEntity<?> generateNewAt(@RequestBody @Valid RefreshTokenDto refreshTokenDto){
        // RT 검증 로직
        Member member = jwtTokenProvider.validateRt(refreshTokenDto.getRefreshToken());

        // AT 신규 생성 로직
        String accessToken = jwtTokenProvider.createAtToken(member);

        LoginResDto loginResDto = LoginResDto.builder()
                .accessToken(accessToken)
                .build();

        return new ResponseEntity<>(
                CommonDto.builder()
                        .result(loginResDto)
                        .status_code(
                                HttpStatus.OK.value())
                        .status_message("accessToken 재발급 성공!")
                        .build(),
                HttpStatus.OK);
    }

    // 회원목록조회 : url 패턴("/author/list")
    @GetMapping("/list")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> findAll() {
        List<MemberResDto> memberResDtoList = memberService.findAll();

        return new ResponseEntity<>(
                CommonDto.builder()
                        .result(memberResDtoList)
                        .status_code(
                                HttpStatus.OK.value())
                        .status_message("회원목록조회성공")
                        .build(),
                HttpStatus.OK);
    }

    // 내정보조회
    @GetMapping("/myinfo")
    @PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
    public ResponseEntity<?> findById() {

        MemberResDto myInfo = memberService.findByEmail();

        return new ResponseEntity<>(
                CommonDto.builder()
                        .result(myInfo)
                        .status_code(
                                HttpStatus.OK.value())
                        .status_message("마이 인포 조회성공")
                        .build(),
                HttpStatus.OK);
    }

    // 회원삭제
    @DeleteMapping("/delete")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> softDelete() {

        memberService.softDeleteByEmail();

        return new ResponseEntity<>(
                CommonDto.builder()
                        .result("OK")
                        .status_code(
                                HttpStatus.OK.value())
                        .status_message("회원 탈퇴 성공")
                        .build(),
                HttpStatus.OK);
    }

    // 회원상세조회
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/detail/{id}")
    public ResponseEntity<?> memberDetail(@PathVariable Long id){
        return new ResponseEntity<>(
                CommonDto.builder()
                        .result(memberService.findById(id))
                        .status_code(HttpStatus.OK.value())
                        .status_message("회원상세조회완료")
                        .build(),
                HttpStatus.OK);
    }
}