package com.app.config;

import com.app.service.AdminUserDetailsService;
import com.app.service.EmployeeUserDetailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class CustomAuthenticationProvider implements AuthenticationProvider {
    
    @Autowired
    private AdminUserDetailsService adminUserDetailsService;
    
    @Autowired
    private EmployeeUserDetailsService employeeUserDetailsService;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        String username = authentication.getName();
        String password = authentication.getCredentials().toString();
        
        UserDetails userDetails = null;
        
        // Try to authenticate as Admin first
        try {
            userDetails = adminUserDetailsService.loadUserByUsername(username);
            if (passwordEncoder.matches(password, userDetails.getPassword())) {
                return new UsernamePasswordAuthenticationToken(
                    userDetails, password, userDetails.getAuthorities());
            }
        } catch (org.springframework.security.core.userdetails.UsernameNotFoundException e) {
            // Admin not found, try Employee
        }
        
        // Try to authenticate as Employee
        try {
            userDetails = employeeUserDetailsService.loadUserByUsername(username);
            if (passwordEncoder.matches(password, userDetails.getPassword())) {
                return new UsernamePasswordAuthenticationToken(
                    userDetails, password, userDetails.getAuthorities());
            }
        } catch (org.springframework.security.core.userdetails.UsernameNotFoundException e) {
            // Employee not found either
        }
        
        throw new BadCredentialsException("Invalid username or password");
    }
    
    @Override
    public boolean supports(Class<?> authentication) {
        return UsernamePasswordAuthenticationToken.class.isAssignableFrom(authentication);
    }
}
