package br.com.aquidolado.security;

import org.springframework.security.core.userdetails.UserDetails;

public interface CurrentUser extends UserDetails {

    Long getUserId();
}
