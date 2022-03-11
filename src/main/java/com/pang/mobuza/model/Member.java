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
    public Member(String password, Long kakaoId, String nickname, String email) {
        this.password = password;
        this.kakaoId = kakaoId;
        this.nickname = nickname;
        this.email = email;
    }

    public void updateInfo(RequestMemberUpdateDto dto){
        this.nickname = dto.getNickname();
        this.heroName = dto.getHeroName();
    }


    public Member fromDto(RequestRegisterDto dto, String password) {
        return new Member().builder()
                .password(password)
                .kakaoId(dto.getKakaoId())
                .email(dto.getEmail())
                .build();
    }
}
