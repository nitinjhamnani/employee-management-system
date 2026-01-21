package com.app.repository;

import com.app.model.Admin;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AdminRepository extends JpaRepository<Admin, Long> {
    Admin findByUsername(String username);
    Admin findByEmail(String email);
    List<Admin> findByStatus(String status);
    List<Admin> findByRole(String role);
    List<Admin> findByParentAdmin_Id(Long parentAdminId);
    List<Admin> findByDepartment(String department);
}
