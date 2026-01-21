package com.app.service;

import com.app.model.Admin;
import com.app.repository.AdminRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class AdminUserDetailsService implements UserDetailsService {
    
    @Autowired
    private AdminRepository adminRepository;
    
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Admin admin = adminRepository.findByUsername(username);
        
        if (admin == null || !"ACTIVE".equals(admin.getStatus())) {
            throw new UsernameNotFoundException("Admin not found or inactive: " + username);
        }
        
        List<GrantedAuthority> authorities = new ArrayList<>();
        
        // Assign role based on admin role
        String adminRole = admin.getRole();
        if ("SUPER_ADMIN".equals(adminRole)) {
            authorities.add(new SimpleGrantedAuthority("ROLE_ADMIN"));
            authorities.add(new SimpleGrantedAuthority("ROLE_SUPER_ADMIN"));
        } else if ("ADMIN".equals(adminRole)) {
            authorities.add(new SimpleGrantedAuthority("ROLE_ADMIN"));
        } else {
            // SUB_ADMIN or default
            authorities.add(new SimpleGrantedAuthority("ROLE_ADMIN"));
        }
        
        return User.builder()
                .username(admin.getUsername())
                .password(admin.getPassword())
                .authorities(authorities)
                .accountExpired(false)
                .accountLocked(false)
                .credentialsExpired(false)
                .disabled(false)
                .build();
    }
}
