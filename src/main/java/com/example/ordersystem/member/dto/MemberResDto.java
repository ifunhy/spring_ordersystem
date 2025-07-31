package com.example.ordersystem.member.dto;

import com.example.ordersystem.member.domain.Member;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class MemberResDto {
    private Long id;
    private String name;
    private String email;


    public static MemberResDto fromEntity(Member member) {
        return MemberResDto.builder()
                .id(member.getId())
                .name(member.getName())
                .email(member.getEmail())
                .build();
    }
}
