package com.pang.mobuza.util;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.json.simple.JSONObject;
import org.springframework.http.HttpStatus;

@Getter
@NoArgsConstructor
public class JwtExceptionResponse {
    private String message;
    private HttpStatus status;

    @Builder
    public JwtExceptionResponse(String message, HttpStatus status) {
        this.message = message;
        this.status = status;
    }

//    public int convertToJson() {
//        JSONObject json = new JSONObject();
//        json.put("code", status);
//        json.put("message", message);
//        return json;
//    }
}
