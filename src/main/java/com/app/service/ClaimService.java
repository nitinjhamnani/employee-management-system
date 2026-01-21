package com.app.service;

import com.app.model.Claim;
import com.app.model.Employee;
import com.app.repository.ClaimRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class ClaimService {
    
    @Autowired
    private ClaimRepository claimRepository;
    
    public List<Claim> getClaimsByEmployee(Employee employee) {
        return claimRepository.findByEmployee(employee);
    }
    
    public List<Claim> getClaimsByEmployeeAndStatus(Employee employee, String status) {
        return claimRepository.findByEmployeeAndStatus(employee, status);
    }
    
    public List<Claim> getPendingClaims() {
        return claimRepository.findByStatus("PENDING");
    }
    
    public Claim saveClaim(Claim claim) {
        return claimRepository.save(claim);
    }
    
    public Optional<Claim> getClaimById(Long id) {
        return claimRepository.findById(id);
    }
    
    public Claim approveClaim(Long claimId, Employee approvedBy, String remarks) {
        Claim claim = claimRepository.findById(claimId)
                .orElseThrow(() -> new RuntimeException("Claim not found"));
        claim.setStatus("APPROVED");
        claim.setApprovedBy(approvedBy);
        claim.setApprovedAt(LocalDateTime.now());
        claim.setRemarks(remarks);
        return claimRepository.save(claim);
    }
    
    public Claim rejectClaim(Long claimId, Employee rejectedBy, String remarks) {
        Claim claim = claimRepository.findById(claimId)
                .orElseThrow(() -> new RuntimeException("Claim not found"));
        claim.setStatus("REJECTED");
        claim.setApprovedBy(rejectedBy);
        claim.setApprovedAt(LocalDateTime.now());
        claim.setRemarks(remarks);
        return claimRepository.save(claim);
    }
    
    public void deleteClaim(Long id) {
        claimRepository.deleteById(id);
    }
}
