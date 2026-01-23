package com.app.service;

import com.app.model.Employee;
import com.app.repository.EmployeeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class EmployeeService {
    
    @Autowired
    private EmployeeRepository employeeRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    public List<Employee> getAllEmployees() {
        return employeeRepository.findAll();
    }
    
    public List<Employee> getActiveEmployees() {
        return employeeRepository.findByStatus("ACTIVE");
    }
    
    public List<Employee> getEmployeesByDepartment(String department) {
        return employeeRepository.findByDepartment(department);
    }
    
    public Optional<Employee> getEmployeeById(Long id) {
        return employeeRepository.findById(id);
    }
    
    public Employee saveEmployee(Employee employee) {
        // Generate employee ID (username) for new employees
        if (employee.getId() == null && (employee.getUsername() == null || employee.getUsername().isEmpty())) {
            String employeeId = generateEmployeeId();
            employee.setUsername(employeeId);
            // Set password same as employee ID
            employee.setPassword(passwordEncoder.encode(employeeId));
        } else if (employee.getId() != null) {
            // For existing employees, only encode password if it's being changed and not already encoded
            if (employee.getPassword() != null && !employee.getPassword().isEmpty()) {
                // Check if password is already encoded (starts with $2a$ or $2b$)
                if (!employee.getPassword().startsWith("$2a$") && !employee.getPassword().startsWith("$2b$")) {
                    employee.setPassword(passwordEncoder.encode(employee.getPassword()));
                }
            } else {
                // If password is empty during edit, don't change it - load existing password
                Optional<Employee> existingEmployee = employeeRepository.findById(employee.getId());
                if (existingEmployee.isPresent()) {
                    employee.setPassword(existingEmployee.get().getPassword());
                }
            }
        }
        
        // Set position from hierarchy level if position is not set
        if (employee.getPosition() == null || employee.getPosition().isEmpty()) {
            employee.setPosition(employee.getHierarchyLevel());
        }
        
        return employeeRepository.save(employee);
    }
    
    private String generateEmployeeId() {
        SecureRandom random = new SecureRandom();
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        StringBuilder employeeId = new StringBuilder("PG");
        
        // Generate 6 more alphanumeric characters (total 8: PG + 6 chars)
        for (int i = 0; i < 6; i++) {
            employeeId.append(chars.charAt(random.nextInt(chars.length())));
        }
        
        // Ensure uniqueness
        while (employeeRepository.findByUsername(employeeId.toString()) != null) {
            employeeId = new StringBuilder("PG");
            for (int i = 0; i < 6; i++) {
                employeeId.append(chars.charAt(random.nextInt(chars.length())));
            }
        }
        
        return employeeId.toString();
    }
    
    public void deleteEmployee(Long id) {
        employeeRepository.deleteById(id);
    }
    
    public List<Employee> searchEmployees(String keyword) {
        return employeeRepository.findByFirstNameContainingOrLastNameContaining(keyword, keyword);
    }
    
    public List<Employee> getEmployeesByHierarchyLevel(String hierarchyLevel) {
        return employeeRepository.findByHierarchyLevel(hierarchyLevel);
    }
    
    public List<Employee> searchEmployeesByHierarchyLevel(String hierarchyLevel, String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return getEmployeesByHierarchyLevel(hierarchyLevel);
        }
        return employeeRepository.findByHierarchyLevelAndNameContaining(hierarchyLevel, keyword.trim());
    }
    
    /**
     * Returns active employees who can be reporting managers for the given hierarchy level.
     * e.g. Zonal Head can report to Cluster Head or Area Sales Manager; Promoter can report to any higher level.
     */
    public List<Employee> getActiveManagersForHierarchyLevel(String hierarchyLevel) {
        List<Employee> all = getActiveEmployees();
        return all.stream()
                .filter(e -> isHigherHierarchy(e.getHierarchyLevel(), hierarchyLevel))
                .toList();
    }

    /**
     * Reporting manager options for admin portal when creating/editing by type.
     * Promoter → none (self); Zonal Head → Promoters only; Cluster Head → Zonal Heads only; ASM → Cluster Heads only.
     */
    public List<Employee> getReportingManagerOptionsForAdmin(String hierarchyLevel) {
        return switch (hierarchyLevel) {
            case "PROMOTER" -> List.of();
            case "ZONAL_HEAD" -> getActiveEmployees().stream().filter(e -> "PROMOTER".equals(e.getHierarchyLevel())).toList();
            case "CLUSTER_HEAD" -> getActiveEmployees().stream().filter(e -> "ZONAL_HEAD".equals(e.getHierarchyLevel())).toList();
            case "AREA_SALES_MANAGER" -> getActiveEmployees().stream().filter(e -> "CLUSTER_HEAD".equals(e.getHierarchyLevel())).toList();
            default -> List.of();
        };
    }
    
    private boolean isHigherHierarchy(String managerLevel, String reporteeLevel) {
        if (managerLevel == null || reporteeLevel == null) return false;
        int m = hierarchyOrder(managerLevel);
        int r = hierarchyOrder(reporteeLevel);
        return m > r; // manager must be above reportee
    }
    
    private int hierarchyOrder(String level) {
        return switch (level) {
            case "PROMOTER" -> 1;
            case "ZONAL_HEAD" -> 2;
            case "CLUSTER_HEAD" -> 3;
            case "AREA_SALES_MANAGER" -> 4;
            default -> 0;
        };
    }

    /** Type slug -> hierarchy level. */
    private static final java.util.Map<String, String> TYPE_TO_HIERARCHY = java.util.Map.of(
            "promoters", "PROMOTER",
            "zonal-heads", "ZONAL_HEAD",
            "cluster-heads", "CLUSTER_HEAD",
            "area-sales-managers", "AREA_SALES_MANAGER"
    );

    /** Type slug -> display label. */
    private static final java.util.Map<String, String> TYPE_TO_LABEL = java.util.Map.of(
            "promoters", "Promoter",
            "zonal-heads", "Zonal Head",
            "cluster-heads", "Cluster Head",
            "area-sales-managers", "Area Sales Manager"
    );

    /**
     * Returns type slugs that an employee with the given hierarchy level can manage in the employee portal.
     * Each role can add only the immediate next level: Promoter -> Zonal Head only;
     * Zonal Head -> Cluster Head only; Cluster Head -> Area Sales Manager only; ASM -> none.
     */
    public List<String> getManageableTypesForHierarchy(String hierarchyLevel) {
        int order = hierarchyOrder(hierarchyLevel);
        if (order >= 4) return List.of(); // AREA_SALES_MANAGER
        if (order >= 3) return List.of("area-sales-managers"); // CLUSTER_HEAD -> ASM only
        if (order >= 2) return List.of("cluster-heads"); // ZONAL_HEAD -> Cluster Head only
        if (order >= 1) return List.of("zonal-heads"); // PROMOTER -> Zonal Head only
        return List.of();
    }

    public String getHierarchyForType(String type) {
        String h = TYPE_TO_HIERARCHY.get(type);
        if (h == null) throw new IllegalArgumentException("Invalid employee type: " + type);
        return h;
    }

    public String getLabelForType(String type) {
        return TYPE_TO_LABEL.getOrDefault(type, type);
    }
    
    public boolean emailExists(String email) {
        return employeeRepository.findByEmail(email) != null;
    }
    
    public boolean emailExistsForOtherEmployee(String email, Long id) {
        Employee employee = employeeRepository.findByEmail(email);
        return employee != null && !employee.getId().equals(id);
    }
    
    public boolean phoneExists(String phone) {
        return employeeRepository.findByPhone(phone) != null;
    }
    
    public boolean phoneExistsForOtherEmployee(String phone, Long id) {
        Employee employee = employeeRepository.findByPhone(phone);
        return employee != null && !employee.getId().equals(id);
    }
    
    public boolean emailAndPhoneExists(String email, String phone) {
        return employeeRepository.findByEmailAndPhone(email, phone) != null;
    }
    
    public boolean emailAndPhoneExistsForOtherEmployee(String email, String phone, Long id) {
        Employee employee = employeeRepository.findByEmailAndPhone(email, phone);
        return employee != null && !employee.getId().equals(id);
    }
    
    public Optional<Employee> getEmployeeByUsername(String username) {
        Employee employee = employeeRepository.findByUsername(username);
        return Optional.ofNullable(employee);
    }
    
    /**
     * Gets all employees that report to the given employee (directly or indirectly).
     * This includes all employees in the hierarchy below the given employee.
     */
    public List<Employee> getAllReportingEmployees(Employee manager) {
        if (manager == null) {
            return List.of();
        }
        List<Employee> allReporting = new java.util.ArrayList<>();
        List<Employee> directReports = employeeRepository.findAll().stream()
                .filter(e -> e.getReportingManager() != null && e.getReportingManager().getId().equals(manager.getId()))
                .toList();
        
        allReporting.addAll(directReports);
        
        // Recursively get all employees reporting to direct reports
        for (Employee directReport : directReports) {
            allReporting.addAll(getAllReportingEmployees(directReport));
        }
        
        return allReporting;
    }
    
    /**
     * Gets all employee IDs that report to the given employee (including the employee themselves).
     */
    public List<Long> getAllReportingEmployeeIds(Employee employee) {
        List<Long> ids = new java.util.ArrayList<>();
        ids.add(employee.getId());
        List<Employee> reporting = getAllReportingEmployees(employee);
        ids.addAll(reporting.stream().map(Employee::getId).toList());
        return ids;
    }
}

