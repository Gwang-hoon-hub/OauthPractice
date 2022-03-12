package com.pang.mobuza.repository;

import com.pang.mobuza.model.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
    // findById => 로 리프래쉬 토큰 갖고 와서 맞는 리프래쉬 토큰인지 확인하는 절차를 갖기

    RefreshToken findByEmail(@Param("email") String email);
}
