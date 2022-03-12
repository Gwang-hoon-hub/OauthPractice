package com.pang.mobuza.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pang.mobuza.controller.KakaoUserInfoDto;
import com.pang.mobuza.dto.*;
import com.pang.mobuza.model.Member;
import com.pang.mobuza.model.RefreshToken;
import com.pang.mobuza.repository.MemberRepository;
import com.pang.mobuza.repository.RefreshTokenRepository;
import com.pang.mobuza.security.filter.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.ObjectUtils;
import org.springframework.web.client.RestTemplate;

import javax.transaction.Transactional;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@RequiredArgsConstructor
@Service
@Slf4j
@Transactional
public class Oauth2MemberService {
    private final JwtTokenProvider jwtTokenProvider;
    private final RefreshTokenRepository refreshTokenRepository;
    private final MemberRepository memberRepository;
    private final RedisTemplate redisTemplate;

    @Transactional
    public ResponseTokenDto kakaoLogin(String code) throws JsonProcessingException {
        // 1. "인가 코드"로 "액세스 토큰" 요청
        String accessToken = getAccessToken(code);

        // 2. 토큰으로 카카오 API 호출
        KakaoUserInfoDto kakaoUserInfoDto = getKakaoUserInfo(accessToken);

        RequestRegisterDto dto = RequestRegisterDto.builder()
                .kakaoId(kakaoUserInfoDto.getKakaoId())
//                .nickname(kakaoUserInfoDto.getNickname())
                .email(kakaoUserInfoDto.getEmail())
                .build();
        // 회원가입, 로그인 처리
        register(dto);

        // access, refresh 둘다 만들어줌
        String access = jwtTokenProvider.createAccessToken(kakaoUserInfoDto.getEmail());
        String refresh = jwtTokenProvider.createRefreshToken(kakaoUserInfoDto.getEmail());
        System.out.println(" 서비스단 토큰 발급 : " + access + "  ====  " + refresh);

        // 4. RefreshToken Redis 저장 (expirationTime 설정을 통해 자동 삭제 처리)
        redisTemplate.opsForValue()
                .set("RT:" + kakaoUserInfoDto.getEmail(), refresh, jwtTokenProvider.getExpiration(refresh), TimeUnit.MILLISECONDS);

//        Long exp = jwtTokenProvider.getExpiration(refresh);
        RefreshToken refreshToken = RefreshToken.builder()
                .token(refresh)
                .email(kakaoUserInfoDto.getEmail())
                .build();

        refreshTokenRepository.save(refreshToken);

        return ResponseTokenDto.builder()
                .accessToken(access)
                .refreshToken(refresh)
                .build();
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
    @Transactional
    public ResponseEntity updateMemberInfo(RequestMemberUpdateDto dto, String email){
        Member byEmail = memberRepository.findByEmail(email);
        if(byEmail == null){
            throw new UsernameNotFoundException("유저를 찾을 수 없습니다.");
        }
        Member m2 = byEmail.updateInfo(dto);
//        memberRepository.save(m2);
        return ResponseEntity.ok().body("캐릭터, 닉네임 설정 완료");
    }

//    @Transactional
//    public TokenDto reissue(RequestTokenDto dto) {
//        // 1. Refresh Token 검증
//        if (!jwtTokenProvider.validateToken(dto.getRefresh())) {
//            throw new RuntimeException("Refresh Token 이 유효하지 않습니다.");
//        }
//        System.out.println("리이슈 마지막 부분1");
//        // 2. Access Token 에서 Member ID 가져오기
//        Authentication authentication = jwtTokenProvider.getAuthentication(dto.getAccess());
//        System.out.println("authentication : "+authentication.getName());
//        // 3. 저장소에서 Member ID 를 기반으로 Refresh Token 값 가져옴
//        RefreshToken refreshToken = refreshTokenRepository.findByEmail(authentication.getName());
//
//        // 4. Refresh Token 일치하는지 검사
//        if (!refreshToken.getToken().equals(dto.getRefresh())) {
//            throw new RuntimeException("토큰의 유저 정보가 일치하지 않습니다.");
//        }
//        System.out.println("리이슈 마지막 부분2");
//        // 5. 새로운 토큰 생성
//
//        TokenDto tokenDto = TokenDto.builder()
//                .refresh(jwtTokenProvider.createRefreshToken(authentication.getName()))
//                .access(jwtTokenProvider.createAccessToken(authentication.getName()))
//                .build();
//
//        // 6. 저장소 정보 업데이트
//        RefreshToken newRefreshToken = refreshToken.update(refreshToken.getToken());
//        refreshTokenRepository.save(newRefreshToken);
//        System.out.println("리이슈 마지막 부분3");
//        // 토큰 발급
//        return tokenDto;
//    }

    @Transactional
    public TokenDto reissue(RequestTokenDto dto) {
        // 1. Refresh Token 검증
        if (!jwtTokenProvider.validateToken(dto.getRefresh())) {
            throw new RuntimeException("Refresh Token 이 유효하지 않습니다.");
        }
        // 2. Access Token 에서 Member ID 가져오기
        Authentication authentication = jwtTokenProvider.getAuthentication(dto.getAccess());
        System.out.println("authentication : "+authentication.getName());

        // 3. 저장소에서 Member ID 를 기반으로 Refresh Token 값 가져옴
        String refreshToken = (String)redisTemplate.opsForValue().get("RT:" + authentication.getName());

        // 4. Refresh Token 일치하는지 검사
        if(ObjectUtils.isEmpty(refreshToken)) {
            throw new RuntimeException("잘못된 요청");
//            return ResponseEntity.badRequest().body("잘못된 요청");
        }
        if(!refreshToken.equals(dto.getRefresh())) {
            throw new IllegalArgumentException("리프래쉬 일치하지 않음.");
//            return ResponseEntity.badRequest().body("Refresh 정보가 일치 하지 않음.");
        }

        // 5. 새로운 토큰 생성
        TokenDto tokenDto = TokenDto.builder()
                .refresh(jwtTokenProvider.createRefreshToken(authentication.getName()))
                .access(jwtTokenProvider.createAccessToken(authentication.getName()))
                .build();

        // 3. Redis 에서 해당 User email 로 저장된 Refresh Token 이 있는지 여부를 확인 후 있을 경우 삭제합니다.
//        if (redisTemplate.opsForValue().get("RT:" + authentication.getName()) != null) {
//            // Refresh Token 삭제
//            redisTemplate.delete("RT:" + authentication.getName());
//        }

        // 6. 저장소 정보 업데이트
        redisTemplate.opsForValue()
                .set("RT:" + authentication.getName(), tokenDto.getRefresh(),
                        jwtTokenProvider.getExpiration(tokenDto.getRefresh()), TimeUnit.MILLISECONDS);
        // 토큰 발급
        return tokenDto;
    }
    // 로그아웃 처리
    public ResponseEntity logout(RequestTokenDto dto) {
        // 1. Access Token 검증
        if (!jwtTokenProvider.validateToken(dto.getAccess())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("잘봇된요청");
        }
        // 2. Access Token 에서 User email 을 가져옵니다.
        Authentication authentication = jwtTokenProvider.getAuthentication(dto.getAccess());

        // 3. Redis 에서 해당 User email 로 저장된 Refresh Token 이 있는지 여부를 확인 후 있을 경우 삭제합니다.
        if (redisTemplate.opsForValue().get("RT:" + authentication.getName()) != null) {
            // Refresh Token 삭제
            redisTemplate.delete("RT:" + authentication.getName());
        }

        // 4. 해당 Access Token 유효시간 가지고 와서 BlackList 로 저장하기
        Long expiration = jwtTokenProvider.getExpiration(dto.getAccess());
        redisTemplate.opsForValue()
                .set(dto.getAccess(), "logout", expiration, TimeUnit.MILLISECONDS);
        return ResponseEntity.ok().body("로그아웃 성공");

    }
}
