package com.pang.mobuza.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pang.mobuza.controller.KakaoUserInfoDto;
import com.pang.mobuza.dto.RequestRegisterDto;
import com.pang.mobuza.model.Member;
import com.pang.mobuza.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

@RequiredArgsConstructor
@Service
public class Oauth2MemberService {

    private final MemberRepository memberRepository;

    public void kakaoLogin(String code) throws JsonProcessingException {
        // 1. "인가 코드"로 "액세스 토큰" 요청
        String accessToken = getAccessToken(code);
        // 2. 토큰으로 카카오 API 호출
        KakaoUserInfoDto kakaoUserInfoDto = getKakaoUserInfo(accessToken);
        System.out.println("kakaoUserInfoDto = " + kakaoUserInfoDto.toString());

        RequestRegisterDto dto = RequestRegisterDto.builder()
                .kakaoId(kakaoUserInfoDto.getId())
                .nickname(kakaoUserInfoDto.getNickname())
                .email(kakaoUserInfoDto.getEmail())
                .build();

        System.out.println(register(dto));


    }

    public String getAccessToken(String code) throws JsonProcessingException {
        System.out.println(code);
        // HTTP Header 생성
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-type", "application/x-www-form-urlencoded;charset=utf-8");
// HTTP Body 생성
        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("grant_type", "authorization_code");
        body.add("client_id", "f367d5c13479608400bba9be2af87fc6");
        body.add("redirect_uri", "http://localhost:8080/user/kakao/callback");
        body.add("code", code);
        body.add("client_secret", "X8m672khDWbTiYJlRBNwNGtH8K3k7HVE");
// HTTP 요청 보내기
        HttpEntity<MultiValueMap<String, String>> kakaoTokenRequest =
                new HttpEntity<>(body, headers);

        RestTemplate rt = new RestTemplate();
        ResponseEntity<String> response = rt.exchange(
                "https://kauth.kakao.com/oauth/token",
                HttpMethod.POST,
                kakaoTokenRequest,
                String.class
        );
        // HTTP 응답 (JSON) -> 액세스 토큰 파싱
        String responseBody = response.getBody();
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode jsonNode = objectMapper.readTree(responseBody);
        return jsonNode.get("access_token").asText();
    }

    public KakaoUserInfoDto getKakaoUserInfo(String accessToken) throws JsonProcessingException {
        // HTTP Header 생성
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "Bearer " + accessToken);
        headers.add("Content-type", "application/x-www-form-urlencoded;charset=utf-8");

// HTTP 요청 보내기
        HttpEntity<MultiValueMap<String, String>> kakaoUserInfoRequest = new HttpEntity<>(headers);
        RestTemplate rt = new RestTemplate();
        ResponseEntity<String> response = rt.exchange(
                "https://kapi.kakao.com/v2/user/me",
                HttpMethod.POST,
                kakaoUserInfoRequest,
                String.class
        );

        System.out.println("=========================================");
        System.out.println("acceessToken : " + accessToken);
        System.out.println("=========================================");

        String responseBody = response.getBody();
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode jsonNode = objectMapper.readTree(responseBody);
        Long id = jsonNode.get("id").asLong();
        String nickname = jsonNode.get("properties")
                .get("nickname").asText();
//        String imgurl = jsonNode.get("properties")
//                .get("thumbnail_image").asText();
        String email = jsonNode.get("kakao_account")
                .get("email").asText();


        System.out.println("카카오 사용자 정보: " + id + ", " + nickname + ", " + email + " response : " + response);
        return new KakaoUserInfoDto(id, nickname, email);
    }

    public ResponseEntity register(RequestRegisterDto dto){
        Member member = new Member();
        memberRepository.save(member.fromDto(dto));
        return new ResponseEntity("회원가입 완료", null, HttpStatus.OK);
    }

}
