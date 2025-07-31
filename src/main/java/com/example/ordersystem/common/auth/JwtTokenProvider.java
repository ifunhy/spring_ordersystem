package com.example.ordersystem.common.auth;

import com.example.ordersystem.member.domain.Member;
import com.example.ordersystem.member.repository.MemberRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import jakarta.annotation.PostConstruct;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import javax.crypto.spec.SecretKeySpec;
import java.security.Key;
import java.util.Date;
import java.util.concurrent.TimeUnit;

@Component
public class JwtTokenProvider {
    private final MemberRepository memberRepository;
    private final RedisTemplate<String, String> redisTemplate;

    @Value("${jwt.expirationAt}")
    private int expirationAt;

    @Value("${jwt.secretKeyAt}")
    private String secretKeyAt;

    @Value("${jwt.expirationRt}")
    private int expirationRt;

    @Value("${jwt.secretKeyRt}")
    private String secretKeyRt;

    private Key secret_at_key;
    private Key secret_rt_key;

    // 생성자 주입
    // Qualifier는 기본적으로 메소드를 통한 주입 가능. 그래서, 이 경우 생성자 주입 방식을 해야 Qualifier 사용 가능
    public JwtTokenProvider(MemberRepository memberRepository, @Qualifier("rtInventory") RedisTemplate<String, String> redisTemplate) {
        this.memberRepository = memberRepository;
        this.redisTemplate = redisTemplate;
    }

    // 스프링 빈이 만들어지는 시점에 Bean이 만들어진 직후에 아래 메서드가 바로 실행
    @PostConstruct
    public void makeKey(){
        secret_at_key = new SecretKeySpec(
                java
                        .util
                        .Base64
                        .getDecoder()
                        .decode(secretKeyAt),
                SignatureAlgorithm.
                        HS512.
                        getJcaName());

        secret_rt_key = new SecretKeySpec(
                java
                        .util
                        .Base64
                        .getDecoder()
                        .decode(secretKeyRt),
                SignatureAlgorithm.
                        HS512.
                        getJcaName());
    }

    public String createAtToken(Member member){
        String email = member.getEmail();
        String role = member.getRole().toString();

        // claims는 페이로드이다. 그리고 페이로드는 사용자 정보를 의미한다.
        Claims claims = Jwts.claims().setSubject(email);

        // 주된 키값을 제외한 나머지 사용자 정보는 put 사용해서 key:value 세팅한다.
        claims.put("role", role);
        Date now = new Date();

        String accessToken = Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(now)
                .setExpiration(new Date(now.getTime() + expirationAt*60*1000L)) // 30분
                // secret 키를 통해 signature 생성
                .signWith(secret_at_key)
                .compact();

        return accessToken;
    }

    public String createRtToken(Member member){
        // 유효기간이 긴 RT 토큰 생성
        String email = member.getEmail();
        String role = member.getRole().toString();

        // claims는 페이로드이다. 그리고 페이로드는 사용자 정보를 의미한다.
        Claims claims = Jwts.claims().setSubject(email);

        // 주된 키값을 제외한 나머지 사용자 정보는 put 사용해서 key:value 세팅한다.
        claims.put("role", role);
        Date now = new Date();

        String refreshToken = Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(now)
                .setExpiration(new Date(now.getTime() + expirationRt*60*1000L)) // 30분
                // secret 키를 통해 signature 생성
                .signWith(secret_rt_key)
                .compact();

        // RT 토큰을 Redis에 저장 : key-value 형식으로 set
        redisTemplate.opsForValue().set(member.getEmail(), refreshToken);
//        redisTemplate.opsForValue().set(member.getEmail(), refreshToken, 200, TimeUnit.DAYS);   // 200일 ttl(만료시간)

        return refreshToken;
    }

    public Member validateRt(String refreshToken){
        // rt 그 자체를 검증
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(secretKeyRt)
                .build()
                .parseClaimsJws(refreshToken)
                .getBody();

        String email = claims.getSubject();

        Member member = memberRepository
                .findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("존재하지 않는 계정입니다."));

        // redis의 값과 비교햐는 검증
        String redisRt = redisTemplate.opsForValue().get(member.getEmail());
        if (!redisRt.equals(refreshToken)) {
            throw new IllegalArgumentException("잘못된 토큰입니다.");
        }

        return member;
    }
}
