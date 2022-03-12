package com.pang.mobuza.config;

import com.pang.mobuza.security.filter.CustomAuthenticationFilter;
import com.pang.mobuza.security.filter.JwtTokenProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
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
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class WebSercurityConfig extends WebSecurityConfigurerAdapter {

    private JwtTokenProvider jwtProvider;

    public WebSercurityConfig(JwtTokenProvider jwtProvider) {
        this.jwtProvider = jwtProvider;
    }

    @Override
    public void configure(WebSecurity web) throws Exception {
        web.ignoring().antMatchers("/h2-console/**"); }


    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.cors().configurationSource(corsConfigurationSource());
        http.csrf().disable();
        http.httpBasic().disable();
        http.sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS);

        http
                .authorizeRequests()
                .antMatchers(HttpMethod.GET, "/user/kakao/callback").permitAll()
//                .antMatchers("/h2-console/*","favicon.ico").permitAll()
                .antMatchers("/index.html").permitAll()
                .antMatchers(HttpMethod.GET, "/api/reissue").permitAll()
                .anyRequest().authenticated();

        http    .addFilterBefore(new CustomAuthenticationFilter(jwtProvider), UsernamePasswordAuthenticationFilter.class);
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
