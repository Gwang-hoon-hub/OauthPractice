package com.pang.magazine.util;

import lombok.*;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import java.nio.charset.Charset;

@NoArgsConstructor
@ToString
@Data
public class CustomResponseEntity{
    private HttpStatus code;
    private String message;
    private Object data;
    private String accessToken;

//    @Builder
//    public CustomResponseEntity(HttpStatus code, String message, Object data) {
//        this.code = code;
//        this.message = message;
//        this.data = data;
//    }

    @Builder
    public CustomResponseEntity(String accessToken, HttpStatus code, String message, Object data) {
        this.accessToken = accessToken;
        this.code = code;
        this.message = message;
        this.data = data;
    }

    public ResponseEntity responseAll(){
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(new MediaType("application", "json", Charset.forName("UTF-8")));
        CustomResponseEntity response = CustomResponseEntity.builder()
                .code(this.code)
                .message(this.message)
                .data(this.data)
                .accessToken(this.accessToken)
                .build();
        return new ResponseEntity(response, headers, this.code);
    }

    public ResponseEntity responseNotData(){
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(new MediaType("application", "json", Charset.forName("UTF-8")));
        CustomResponseEntity response = CustomResponseEntity.builder()
                .code(this.code)
                .message(this.message)
                .build();
        return new ResponseEntity(response, headers, this.code);
    }
}
