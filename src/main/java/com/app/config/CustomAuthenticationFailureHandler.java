package com.app.config;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class CustomAuthenticationFailureHandler implements AuthenticationFailureHandler {

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
                                       AuthenticationException exception) throws IOException, ServletException {
        // Check the referer to determine which login page was used
        String referer = request.getHeader("Referer");
        
        String redirectUrl = "/employee/login?error=true";
        
        // If the request came from admin login page, redirect back there
        if (referer != null && referer.contains("/admin/login")) {
            redirectUrl = "/admin/login?error=true";
        }
        
        response.sendRedirect(redirectUrl);
    }
}
