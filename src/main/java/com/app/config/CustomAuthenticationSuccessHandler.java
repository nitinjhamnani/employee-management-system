package com.app.config;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Collection;

@Component
public class CustomAuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {
        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
        
        String redirectUrl = "/login";
        
        // Check if user has ADMIN role
        boolean isAdmin = authorities.stream()
                .anyMatch(authority -> authority.getAuthority().equals("ROLE_ADMIN"));
        
        // Check if user has EMPLOYEE role
        boolean isEmployee = authorities.stream()
                .anyMatch(authority -> authority.getAuthority().equals("ROLE_EMPLOYEE"));
        
        if (isAdmin) {
            redirectUrl = "/";
        } else if (isEmployee) {
            redirectUrl = "/employee/dashboard";
        }
        
        response.sendRedirect(redirectUrl);
    }
}
