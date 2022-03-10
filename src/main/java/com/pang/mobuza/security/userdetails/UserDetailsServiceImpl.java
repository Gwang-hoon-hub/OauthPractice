package com.pang.mobuza.security.userdetails;

import com.pang.mobuza.model.Member;
import com.pang.mobuza.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Optional;

@RequiredArgsConstructor
@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    private final MemberRepository memberRepository;

    @Override
    public UserDetails loadUserByUsername(String kakaoId) throws UsernameNotFoundException {

        Member member = Optional
                .ofNullable(memberRepository.findByKakaoId(kakaoId))
                .orElseThrow(() -> new UsernameNotFoundException("해당 유저 없음"));

        return new UserDetailsImpl(member);
    }
}
