package com.pang.mobuza.security.filter;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.GenericFilterBean;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

@Slf4j
public class CustomAuthenticationFilter extends GenericFilterBean {


    // 권한 검증
    private final JwtTokenProvider jwtProvider;

    private static final Logger logger = LoggerFactory.getLogger(CustomAuthenticationFilter.class);

    public static final String AUTHORIZATION_HEADER = "authorization";

    public CustomAuthenticationFilter(JwtTokenProvider jwtProvider) {
        this.jwtProvider = jwtProvider;
    }

    // jwt 토큰의 인증정보를 SecurityContext 에 담는 역할 - doFilter
    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain)
            throws IOException, ServletException {
        HttpServletRequest httpServletRequest = (HttpServletRequest) servletRequest;
        String jwtAccess = resolveAccessToken(httpServletRequest);
        String jwtRefresh = resolveRefreshToken(httpServletRequest);
        String requestURI = httpServletRequest.getRequestURI();


//        if (StringUtils.hasText(jwtAccess) && jwtProvider.validateToken(jwtAccess)) {
//            Authentication authentication = jwtProvider.getAuthentication(jwtAccess);
//            SecurityContext context = SecurityContextHolder.createEmptyContext();
//            context.setAuthentication(authentication);
//            SecurityContextHolder.setContext(context);
//
//            logger.debug("Security Context에 '{}' 인증 정보를 저장했습니다, uri: {}", authentication.getName(), requestURI);
//        } else {
//            logger.debug("유효한 JWT 토큰이 없습니다, uri: {}", requestURI);
//        }

        // ACCESS 토큰 먼저 검증
        if(jwtAccess != null){
            checkToken(jwtAccess);
        } else if (jwtRefresh != null){
            checkToken(jwtRefresh);
        }
        filterChain.doFilter(servletRequest, servletResponse);
    }

    private void checkToken(String token) {
        jwtProvider.validateToken(token);
        Authentication authentication = jwtProvider.getAuthentication(token);
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    // 필터링을 하기 위해 토큰 정보를 가져오는 메소드
    private String resolveAccessToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("A-AUTH-TOKEN");
        log.info("token : " + bearerToken);
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }

    // 필터링을 하기 위해 토큰 정보를 가져오는 메소드
    private String resolveRefreshToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("R-AUTH-TOKEN");
        log.info("token : " + bearerToken);
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}
