package com.pang.mobuza.security.userdetails;

import com.pang.mobuza.model.Member;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;

@Getter
public class UserDetailsImpl implements UserDetails {

    private final Member member;

    public UserDetailsImpl(Member member) {
        this.member = member;
    }

    public Member getMember(){
        return member;
        // 멤버는
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return null;
    }

    @Override
    public String getPassword() {
        return member.getPassword();
    }

    @Override
    public String getUsername() {
        return member.getKakaoId().toString();
    }
    
    // 커스텀 한 : 카카오 아이디 갖고오는 메소드
//    public Long getKakaoId() {
//        return member.getKakaoId();
//    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
