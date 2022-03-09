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

    private Long kakaoId;

    private String nickname;

    private String email;

    @Builder
    public Member(Long kakaoId, String nickname, String email) {
        this.kakaoId = kakaoId;
        this.nickname = nickname;
        this.email = email;
    }

    public Member fromDto(RequestRegisterDto dto){
        return new Member().builder()
                .kakaoId(dto.getKakaoId())
                .nickname(dto.getNickname())
                .email(dto.getEmail())
                .build();
    }
}
