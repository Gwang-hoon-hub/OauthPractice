package com.pang.mobuza.controller;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.ToString;

@AllArgsConstructor
@ToString
@Data
public class KakaoUserInfoDto {

    private Long id;
    private String nickname;
    private String email;
}
