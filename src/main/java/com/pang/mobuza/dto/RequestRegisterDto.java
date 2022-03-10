package com.pang.mobuza.dto;

import com.pang.mobuza.model.Member;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import org.springframework.validation.annotation.Validated;

import javax.validation.Valid;
import javax.validation.constraints.Email;
import javax.validation.constraints.Pattern;

@Getter
@AllArgsConstructor
@Builder
public class RequestRegisterDto {
    private Long kakaoId;
    private String nickname;
    private String email;

}
