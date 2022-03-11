package com.pang.mobuza.dto;

import com.pang.mobuza.model.HeroNames;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.EnumType;
import javax.persistence.Enumerated;

@Getter
@Builder
@Setter
public class RequestMemberUpdateDto {

    private String nickname;
    @Enumerated(EnumType.STRING)
    private HeroNames heroName;
}
