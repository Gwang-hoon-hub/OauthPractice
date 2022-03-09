package com.pang.mobuza.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.pang.mobuza.service.Oauth2MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;

@RestController
@RequiredArgsConstructor
public class Oauth2Controller {
    private final Oauth2MemberService memberService;


    @GetMapping("/user/kakao/callback")
    public String kakaoLogin(@RequestParam String code) throws JsonProcessingException {
        memberService.kakaoLogin(code);
        return "인증완료";
    }

    @GetMapping("/user")
    public String rest(Principal principal){

        return "rest test";
    }

//
}
