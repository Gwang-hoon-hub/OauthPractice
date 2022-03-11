package com.pang.mobuza.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pang.mobuza.controller.KakaoUserInfoDto;
import com.pang.mobuza.dto.RequestMemberUpdateDto;
import com.pang.mobuza.dto.RequestRegisterDto;
import com.pang.mobuza.model.Member;
import com.pang.mobuza.repository.MemberRepository;
import com.pang.mobuza.security.filter.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.Optional;
import java.util.UUID;

@RequiredArgsConstructor
@Service
@Slf4j
public class Oauth2MemberService {

    private final JwtTokenProvider jwtTokenProvider;

    private final MemberRepository memberRepository;

    public String kakaoLogin(String code) throws JsonProcessingException {
        // 1. "인가 코드"로 "액세스 토큰" 요청
        String accessToken = getAccessToken(code);
        log.info(accessToken);
        // 2. 토큰으로 카카오 API 호출
        KakaoUserInfoDto kakaoUserInfoDto = getKakaoUserInfo(accessToken);
        System.out.println("kakaoUserInfoDto = " + kakaoUserInfoDto.toString());

        RequestRegisterDto dto = RequestRegisterDto.builder()
                .kakaoId(kakaoUserInfoDto.getKakaoId())
//                .nickname(kakaoUserInfoDto.getNickname())
                .email(kakaoUserInfoDto.getEmail())
                .build();

        System.out.println(register(dto));

        return jwtTokenProvider.createToken(kakaoUserInfoDto.getEmail());
//        return accessToken;
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
//        body.add("redirect_uri", "http://moabuza.s3-website.ap-northeast-2.amazonaws.com/callback");
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

        String responseBody = response.getBody();
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode jsonNode = objectMapper.readTree(responseBody);
        Long kakaoId = jsonNode.get("id").asLong();
//        String nickname = jsonNode.get("properties")
//                .get("nickname").asText();
//        String imgurl = jsonNode.get("properties")
//                .get("thumbnail_image").asText();
        String email = jsonNode.get("kakao_account")
                .get("email").asText();


        System.out.println("카카오 사용자 정보: " + kakaoId + ", " + email + " response : " + response);
        return new KakaoUserInfoDto(kakaoId, email);
    }

    public ResponseEntity register(RequestRegisterDto dto){
        Member member = new Member();
        
        // 기존회원이 아니면 회원가입 완료
        if(!memberRepository.existsByEmail(dto.getEmail())){
            String password = String.valueOf(UUID.randomUUID());
            memberRepository.save(member.fromDto(dto, password));
            return new ResponseEntity("회원가입 완료", null, HttpStatus.OK);
            // 회원가입한 회원은 온보딩 화면을 보여주도록 한다.
            // boolean 으로 : 회원가입, 로그인 인지를 알려주고
            // FE에서 온보딩 API를 호출한다.
            // 캐릭터 설정, 닉네임 설정 => PATCH API를 날려서 유저가 설정한 값으로 DEFAULRT 값을 수정하도록 한다.
        }
        // 기존 회원이면 그냥 로그인완료 메세지
        return new ResponseEntity("로그인 완료", HttpStatus.OK);

    }

    // todo : 회원이 회원이 캐릭터랑 닉네임 설정한 경우
    public ResponseEntity updateMemberInfo(RequestMemberUpdateDto dto, String email){
        Member byEmail = memberRepository.findByEmail(email);

        if(byEmail == null){
            throw new UsernameNotFoundException("유저를 찾을 수 없습니다.");
        }
        System.out.println(byEmail.getId());
        byEmail.updateInfo(dto);
        return ResponseEntity.ok().body("캐릭터, 닉네임 설정 완료");
    }

}
