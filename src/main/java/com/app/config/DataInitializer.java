package com.app.config;

import com.app.model.Admin;
import com.app.repository.AdminRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {
    
    @Autowired
    private AdminRepository adminRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @Value("${app.admin.username:admin}")
    private String adminUsername;
    
    @Value("${app.admin.password:admin@123}")
    private String adminPassword;
    
    @Override
    public void run(String... args) throws Exception {
        // Check if admin user already exists
        Admin admin = adminRepository.findByUsername(adminUsername);
        
        if (admin == null) {
            // Create default admin user
            admin = new Admin();
            admin.setFirstName("Admin");
            admin.setLastName("User");
            admin.setEmail("admin@example.com");
            admin.setUsername(adminUsername);
            admin.setPassword(passwordEncoder.encode(adminPassword));
            admin.setPhone("0000000000");
            admin.setDepartment("Administration");
            admin.setRole("SUPER_ADMIN");
            admin.setStatus("ACTIVE");
            
            adminRepository.save(admin);
            System.out.println("Default admin user created with username: " + adminUsername);
        }
    }
}
