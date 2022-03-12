package com.pang.mobuza.model;

import lombok.*;

import javax.persistence.*;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RefreshToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "refresh_id")
    private Long id;

    private String token;

    private String email;

//    @Column(name = "EXP_AT")
//    private Long expAt;


    @Builder
    public RefreshToken(String token, String email) {
        this.token = token;
        this.email = email;
    }


    public RefreshToken update(String token){
        this.token = token;
        return RefreshToken.builder()
                .email(this.email)
                .token(this.token)
                .build();
    }

}
