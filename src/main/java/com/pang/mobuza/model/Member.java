package com.pang.mobuza.model;

import com.pang.mobuza.dto.RequestMemberUpdateDto;
import com.pang.mobuza.dto.RequestRegisterDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.ColumnDefault;

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

    private Long kakaoId;

    private String nickname;

    private String email;

    @Enumerated(EnumType.STRING)
    private HeroNames heroName;

    @Builder
    public Member(String password, Long kakaoId, String nickname, String email, HeroNames heroName) {
        this.password = password;
        this.kakaoId = kakaoId;
        this.nickname = nickname;
        this.email = email;
        this.heroName = heroName;
    }

    @Builder
    public Member(String password, Long kakaoId, String nickname, String email) {
        this.password = password;
        this.kakaoId = kakaoId;
        this.nickname = nickname;
        this.email = email;
    }

    public Member updateInfo(RequestMemberUpdateDto dto){
        this.nickname = dto.getNickname();
        this.heroName = dto.getHeroName();
        System.out.println("닉네임이 들어가는가 : " + this.nickname);
        return Member.builder()
                .password(this.getPassword())
                .email(this.getEmail())
                .nickname(this.getNickname())
                .kakaoId(this.getKakaoId())
                .heroName(this.getHeroName())
                .build();
    }

    public Member fromDto(RequestRegisterDto dto, String password) {
        return new Member().builder()
                .password(password)
                .kakaoId(dto.getKakaoId())
                .email(dto.getEmail())
                .build();
    }

}
