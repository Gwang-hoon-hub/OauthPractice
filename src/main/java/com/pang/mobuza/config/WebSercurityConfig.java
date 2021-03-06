package com.pang.mobuza.config;

import com.pang.mobuza.security.filter.*;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
@RequiredArgsConstructor
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class WebSercurityConfig extends WebSecurityConfigurerAdapter {

    private final JwtTokenProvider jwtProvider;
    private final RedisTemplate redisTemplate;
//    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;
//    private final JwtAccessDeniedHandler jwtAccessDeniedHandler;


    @Override
    public void configure(WebSecurity web) throws Exception {
        web.ignoring().antMatchers("/h2-console/**")
                .antMatchers("/api/reissue"); }


    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.cors().configurationSource(corsConfigurationSource());
        http.csrf().disable();
        http.sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS);

//        http    .exceptionHandling()
//                .accessDeniedHandler(new JwtAccessDeniedHandler())
//                .authenticationEntryPoint(new JwtAuthenticationEntryPoint());
        //todo : ?????? ????????? reissue api ?????? ??? ??? ????????? ?????? ????????? ??????????????? exceptionHandler ????????????
        
        http    .httpBasic().disable()
                .authorizeRequests()
                .antMatchers(HttpMethod.GET, "/user/kakao/callback").permitAll()
//                .antMatchers("/h2-console/*","favicon.ico").permitAll()
                .antMatchers("/index.html").permitAll()
                .antMatchers(HttpMethod.GET, "/api/reissue").permitAll()
                .antMatchers(HttpMethod.GET, "/health").permitAll()
                .anyRequest().authenticated()
                .and()
                .exceptionHandling()
                .accessDeniedHandler(new JwtAccessDeniedHandler())
                .authenticationEntryPoint(new JwtAuthenticationEntryPoint());

        // ?????? ????????? ???????????? ?????? ??????????????? ????????? ????????? ?????? ????????? ???????????? ???????????? ?????? ????????? ?????? ???????????????
        // ????????????...

        http    .addFilterBefore(new CustomAuthenticationFilter(jwtProvider, redisTemplate), UsernamePasswordAuthenticationFilter.class);
        http    .addFilterBefore(new JwtExceptionFilter(), CustomAuthenticationFilter.class);
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        configuration.addAllowedOrigin("http://moabuza.s3-website.ap-northeast-2.amazonaws.com");
        configuration.addAllowedHeader("*");
        configuration.addAllowedMethod("*");
        configuration.setMaxAge(3600L);
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

}
