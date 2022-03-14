package com.pang.mobuza.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ErrorHandller {

    private ErrorResponse response;

//    @ExceptionHandler(JwtExpiredException.class)
//    public ResponseEntity jwtExpiredException(JwtExpiredException e){
//        response = ErrorResponse.builder()
//                .message(e.getMessage())
//                .build();
//        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
//    }

}
