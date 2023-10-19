package com.msik404.karmaappgateway.user;

import java.util.Collection;
import java.util.List;

import org.bson.types.ObjectId;
import org.springframework.lang.NonNull;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

public record UserDetailsImpl(@NonNull ObjectId userId, @NonNull String hashedPassword,
                              @NonNull Role role) implements UserDetails {

    @NonNull
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority(role.name()));
    }

    @NonNull
    @Override
    public String getPassword() {
        return hashedPassword;
    }

    @NonNull
    @Override
    public String getUsername() {
        return userId.toHexString();
    }

    @NonNull
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
