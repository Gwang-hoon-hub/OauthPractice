package com.pang.mobuza.dto;

import lombok.Builder;
import lombok.Data;
import lombok.Getter;

@Builder
@Data
public class ResponseTokenDto {
    private String accessToken;
    private String refreshToken;
}
