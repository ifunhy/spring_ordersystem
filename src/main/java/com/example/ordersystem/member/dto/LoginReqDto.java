package com.example.ordersystem.member.dto;

import com.example.ordersystem.member.domain.Member;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class LoginReqDto {
    @NotEmpty(message = "email은 필수 입력 항목입니다.")
    private String email;
    @NotEmpty(message = "password은 필수 입력 항목입니다.")
    @Size(min = 8, message = "password의 길이가 너무 짧습니다.")
    private String password;

    public Member memberDoLoginToEntity(){
        return Member
                .builder()
                .email(this.email)
                .password(this.password)
                .build();
    }
}

