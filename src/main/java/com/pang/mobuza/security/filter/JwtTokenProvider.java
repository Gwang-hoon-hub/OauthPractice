package com.pang.mobuza.security.filter;

import com.pang.mobuza.exception.JwtExpiredException;
import com.pang.mobuza.security.userdetails.UserDetailsServiceImpl;

import io.jsonwebtoken.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import java.util.Base64;
import java.util.Date;

@Component
@Slf4j
public class JwtTokenProvider {
    private long accessTokenTime = 1000 * 30 * 1; // 30초
    private long refreshTokenTime = 1000 * 60 * 8; // 4분

    @Autowired
    private UserDetailsServiceImpl userDetailsService;

    private String secretKey = "abwieineprmdspowejropsadasdasdasdvsddvsdvasd";

    @PostConstruct
    protected void init() {
        secretKey = Base64.getEncoder().encodeToString(secretKey.getBytes());
    }

    // 어세스 토큰 생성
    public String createAccessToken(String email) {
        Claims claims = Jwts.claims().setSubject(email);
        Date now = new Date();
        return Jwts.builder()
                .setSubject(email)
                .setClaims(claims)
                .setIssuedAt(now)
                .setExpiration(new Date(now.getTime() + accessTokenTime))
                .signWith(SignatureAlgorithm.HS256, secretKey)
                .compact();
    }

    // 리프레쉬 토큰 생성
    public String createRefreshToken(String email) {
        Claims claims = Jwts.claims().setSubject(email);
        Date now = new Date();
        return Jwts.builder()
                .setSubject(email)
                .setClaims(claims)
                .setIssuedAt(now)
                .setExpiration(new Date(now.getTime() + refreshTokenTime))
                .signWith(SignatureAlgorithm.HS256, secretKey)
                .compact();
    }

    public Authentication getAuthentication(String token) {

        Claims claims = parseClaims(token);

        UserDetails userDetails = null;
        try {
//            userDetails = userDetailsService.loadUserByUsername(this.getUserInfo(token));
            userDetails = userDetailsService.loadUserByUsername(claims.getSubject());
        } catch (UsernameNotFoundException e) {
            throw new UsernameNotFoundException("해당 유저가 없습니다");
        }
        return new UsernamePasswordAuthenticationToken(userDetails, "", null);

    }

    public String getUserInfo(String token) {
        log.info("getUserInfo=====");
        String sub = Jwts.parserBuilder().setSigningKey(secretKey).build().parseClaimsJws(token).getBody().getSubject();
        return sub;
    }

    // 필터링을 하기 위해 토큰 정보를 가져오는 메소드
    public String resolveAccessToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("A-AUTH-TOKEN");
        log.info("에세스----- : " + bearerToken);
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }

    public String resolveRefreshToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("R-AUTH-TOKEN");
        log.info("리프래쉬---- : " + bearerToken);
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }

    //x토큰 의 유혀성 검증
    public boolean validateToken(String jwtToken) {
        Jws<Claims> claims = null;
        try {
            claims = Jwts.parserBuilder().setSigningKey(secretKey).build().parseClaimsJws(jwtToken);
            System.out.println("claims : " + claims);
            System.out.println("=======" + claims.getBody().getExpiration() + "=============" +
                    new Date());
            System.out.println(claims.getBody().getExpiration().before(new Date()));
            return !claims.getBody().getExpiration().before(new Date());
        } catch (UnsupportedJwtException e) {
            throw new JwtException("인수가 Claims JWS를 나타내지 않는 경우");
        } catch (MalformedJwtException e) {
            throw new MalformedJwtException(" 문자열이 유효한 JWS가 아닌 경우");
        } catch (SignatureException e) {
            throw new SignatureException("JWS 서명 유효성 검사가 실패한 경우");
        } catch (JwtExpiredException e) {
            throw new JwtExpiredException(null, claims.getBody(), "만료된 토큰");
//            log.info("엔트리 포인트가 잡아가는가");
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("문자열이 null이거나 비어 있거나 공백만 있는 경우");
        }
    }

    private Claims parseClaims(String accessToken) {
        try {
            return Jwts.parserBuilder().setSigningKey(secretKey).build().parseClaimsJws(accessToken).getBody();
        } catch (ExpiredJwtException e) {
            return e.getClaims();
        }
    }

    // 유효성 검사
    public Long getExpiration(String accessToken) {
        // accessToken 남은 유효시간
//        Date expiration = Jwts.parserBuilder().setSigningKey(secretKey).build().parseClaimsJws(accessToken).getBody().getExpiration();
        System.out.println("만료시간 갖고오기 에러?");
        Date expiration = Jwts.parserBuilder().setSigningKey(secretKey).build().parseClaimsJws(accessToken).getBody().getExpiration();
        log.info("만료시간 : " + expiration);
                // 현재 시간
        Long now = new Date().getTime();
        return (expiration.getTime() - now);
    }

}
