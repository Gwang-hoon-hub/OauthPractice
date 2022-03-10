package com.pang.mobuza.model;

import com.pang.mobuza.dto.RequestRegisterDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Entity
public class Member {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String password;

    private String kakaoId;

    private String nickname;

    private String email;

    @Builder
    public Member(String kakaoId, String nickname, String email) {
        this.kakaoId = kakaoId;
        this.nickname = nickname;
        this.email = email;
    }

    public Member fromDto(RequestRegisterDto dto, String kakaoId) {
        return new Member().builder()
                .kakaoId(kakaoId)
                .nickname(dto.getNickname())
                .email(dto.getEmail())
                .build();
    }
}
