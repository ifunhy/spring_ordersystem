package com.example.ordersystem.member.domain;
import com.example.ordersystem.common.domain.BaseTimeEntity;
import com.example.ordersystem.product.domain.Product;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Where;

@NoArgsConstructor
@Getter
@AllArgsConstructor
@ToString
@Builder
@Entity
// JPQL을 제외하고 모든 조회 쿼리에 where del_yn = "N"을 붙이는 효과
@Where(clause = "del_yn = 'N'")
public class Member extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;

    @Column(length = 50, unique = true, nullable = false)
    private String email;
    private String password;

    @Builder.Default
    private String delYn = "N";

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private Role role = Role.USER;

}