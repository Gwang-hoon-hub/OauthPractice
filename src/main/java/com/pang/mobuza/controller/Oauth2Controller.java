package com.pang.mobuza.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.pang.mobuza.security.userdetails.UserDetailsImpl;
import com.pang.mobuza.service.Oauth2MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;

@RestController
@RequiredArgsConstructor
public class Oauth2Controller {

    private final Oauth2MemberService memberService;

    @GetMapping("/user/kakao/callback")
    public ResponseEntity kakaoLogin(@RequestParam String code) throws JsonProcessingException {
        System.out.println("여기오냐");
        ResponseEntity response = memberService.kakaoLogin(code);
//        return new ResponseEntity("에서스토큰 갔니?", HttpStatus.OK);
        return response;
    }


    @GetMapping("/user/app")
    public String user(@AuthenticationPrincipal UserDetailsImpl
                                   userDetails){
        String nickname = userDetails.getUsername();
        return nickname;
    }

    @GetMapping("/home")
    public String rest(){

        // 제이슨~~~

        return "홈화면";
    }
}
