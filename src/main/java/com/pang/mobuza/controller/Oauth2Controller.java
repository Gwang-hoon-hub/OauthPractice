package com.pang.mobuza.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.pang.mobuza.dto.RequestMemberUpdateDto;
import com.pang.mobuza.dto.RequestTokenDto;
import com.pang.mobuza.dto.ResponseTokenDto;
import com.pang.mobuza.dto.TokenDto;
import com.pang.mobuza.security.userdetails.UserDetailsImpl;
import com.pang.mobuza.service.Oauth2MemberService;
import com.pang.mobuza.util.CustomResponseEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

@RestController
@RequiredArgsConstructor
public class Oauth2Controller {

    private final Oauth2MemberService memberService;

    @GetMapping("/user/kakao/callback")
    public ResponseEntity kakaoLogin(@RequestParam String code) throws JsonProcessingException {
        ResponseTokenDto dto = memberService.kakaoLogin(code);
//        return new ResponseEntity("에서스토큰 갔니?", HttpStatus.OK);
        CustomResponseEntity response = CustomResponseEntity.builder()
                .authorization(null)
                .code(HttpStatus.OK)
                .message("어세스토큰 : authorization")
                // data 안에 access, refresh  두개 다 담겨있다.
                .data(dto)
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
        System.out.println("cicd 준비");
        return "홈화면";
    }

    @GetMapping("/api/reissue")
    public ResponseEntity reissue(HttpServletRequest request){
        TokenDto tokenDto = memberService.reissue(request);
        CustomResponseEntity response = CustomResponseEntity.builder()
                .data(tokenDto)
                .message("레디스 저장 성공")
                .code(HttpStatus.OK)
                .build();
        return response.responseAll();
    }

    @GetMapping("/api/logout")
    public ResponseEntity<?> logout(@RequestBody RequestTokenDto dto){
        return memberService.logout(dto);
    }

}
