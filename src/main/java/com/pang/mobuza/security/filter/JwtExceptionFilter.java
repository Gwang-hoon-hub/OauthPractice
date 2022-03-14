package com.pang.mobuza.security.filter;

import io.jsonwebtoken.JwtException;
import org.json.simple.JSONObject;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

@Component
public class JwtExceptionFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain) throws ServletException, IOException {
        try {
            chain.doFilter(req, res); // go to 'JwtAuthenticationFilter'
        } catch (JwtException | IOException ex) {
            setErrorResponse(HttpStatus.UNAUTHORIZED, res, ex);
        }
    }

    public void setErrorResponse(HttpStatus status, HttpServletResponse res, Throwable ex) throws IOException {
        res.setStatus(status.value());
        res.setContentType("application/json; charset=UTF-8");
        PrintWriter out = res.getWriter();

        // res에 담는 코드가 없는데 담아지는 건가요? ㅋㅋㅋㅋㅋ
        // 이게 제대로 담는건지 모르겠씁니다.

        //create Json Object
        JSONObject json = new JSONObject();
        // put some value pairs into the JSON object .
        json.put("code", HttpStatus.UNAUTHORIZED);
        json.put("message", "권한의 문제 reissue");
        // finally output the json string
        out.print(json.toString());

//        JwtExceptionResponse jwtExceptionResponse = new JwtExceptionResponse(ex.getMessage(), HttpStatus.UNAUTHORIZED);
//        res.getWriter().write(jwtExceptionResponse.convertToJson());
    }
}