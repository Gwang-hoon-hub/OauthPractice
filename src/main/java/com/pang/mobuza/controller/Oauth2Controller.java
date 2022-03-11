package com.pang.mobuza.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.pang.mobuza.dto.RequestMemberUpdateDto;
import com.pang.mobuza.security.userdetails.UserDetailsImpl;
import com.pang.mobuza.security.userdetails.UserDetailsServiceImpl;
import com.pang.mobuza.service.Oauth2MemberService;
import com.pang.mobuza.util.CustomResponseEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

@RestController
@RequiredArgsConstructor
public class Oauth2Controller {

    private final Oauth2MemberService memberService;

    @GetMapping("/user/kakao/callback")
    public ResponseEntity kakaoLogin(@RequestParam String code) throws JsonProcessingException {
        String accessToken = memberService.kakaoLogin(code);
//        return new ResponseEntity("에서스토큰 갔니?", HttpStatus.OK);
        CustomResponseEntity response = CustomResponseEntity.builder()
                .authorization(accessToken)
                .code(HttpStatus.OK)
                .message("어세스토큰 : authorization")
                .data(null)
                .build();
        return response.responseAll();
    }

    @PutMapping("/member/info")
    public ResponseEntity update(@RequestBody RequestMemberUpdateDto dto,
                                 @AuthenticationPrincipal UserDetailsImpl userDetails) throws JsonProcessingException {
        System.out.println("컨트롤단 dto : " + dto.getNickname());
        String email = userDetails.getUsername();
//        System.out.println("userDetails.getUsername(); = " + userDetails.getUsername());
        // 회원이 캐릭터랑 닉네임 설정한 경우
        return memberService.updateMemberInfo(dto, email);
    }


//    @GetMapping("/user/app")
//    public String user(@AuthenticationPrincipal UserDetailsImpl
//                                   userDetails){
//        String nickname = userDetails.getUsername();
//        return nickname;
//    }

    @GetMapping("/home")
    public String rest(HttpServletRequest request){
        System.out.println("여기는 도는가");
        String token = request.getHeader("authorization");
        System.out.println(token);
        return "홈화면";
    }

}
