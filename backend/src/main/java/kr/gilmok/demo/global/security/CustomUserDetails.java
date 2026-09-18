package kr.gilmok.demo.global.security;

import kr.gilmok.demo.global.dto.AuthUserDto;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

public record CustomUserDetails(AuthUserDto user) implements UserDetails {

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority(user.role()));
    }

    @Override
    public String getUsername() {
        return user.username();
    }

    @Override
    public String getPassword() {
        return user.passwordHash();
    }

    @Override
    public boolean isEnabled() {
        return "ACTIVE".equals(user.status());
    }

    @Override
    public boolean isAccountNonLocked() {
        return !"LOCKED".equals(user.status());
    }
}
