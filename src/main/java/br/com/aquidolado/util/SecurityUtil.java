package br.com.aquidolado.util;

import br.com.aquidolado.security.CurrentUser;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public final class SecurityUtil {

    private SecurityUtil() {
    }

    public static Long getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || !(auth.getPrincipal() instanceof CurrentUser currentUser)) {
            throw new IllegalStateException("Usuário não autenticado");
        }
        return currentUser.getUserId();
    }
}
