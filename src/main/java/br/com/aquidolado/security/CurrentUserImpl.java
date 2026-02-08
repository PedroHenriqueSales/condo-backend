package br.com.aquidolado.security;

import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;

import java.util.Collection;

@Getter
public class CurrentUserImpl extends User implements CurrentUser {

    private final Long userId;

    public CurrentUserImpl(Long userId, String username, String password, Collection<? extends GrantedAuthority> authorities) {
        super(username, password, authorities);
        this.userId = userId;
    }

    @Override
    public Long getUserId() {
        return userId;
    }
}
