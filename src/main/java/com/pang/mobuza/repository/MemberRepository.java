package com.pang.mobuza.repository;

import com.pang.mobuza.model.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface MemberRepository extends JpaRepository<Member, Long> {
    @Query("select m from Member m where m.kakaoId  = :kakaoId")
    Member findByKakaoId(@Param("kakaoId") String kakaoId);
}
