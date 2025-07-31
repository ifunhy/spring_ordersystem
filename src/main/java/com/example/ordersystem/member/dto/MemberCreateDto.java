package com.example.ordersystem.member.dto;

import com.example.ordersystem.member.domain.Member;
import com.example.ordersystem.member.domain.Role;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class MemberCreateDto {
    @NotEmpty(message = "이름은 필수 입력 항목입니다.")
    private String name;

    @NotEmpty(message = "email은 필수 입력 항목입니다.")
    private String email;

    @NotEmpty(message = "password은 필수 입력 항목입니다.")
    @Size(min = 8, message = "password의 길이가 너무 짧습니다.")
    private String password;

    public Member authorToEntity(String hashedPassword) {
        return Member.builder().name(this.name).password(hashedPassword).email(this.email).role(Role.USER).build();
    }
}