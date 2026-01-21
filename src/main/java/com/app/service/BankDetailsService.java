package com.app.service;

import com.app.model.BankDetails;
import com.app.model.Employee;
import com.app.repository.BankDetailsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class BankDetailsService {
    
    @Autowired
    private BankDetailsRepository bankDetailsRepository;
    
    public List<BankDetails> getBankDetailsByEmployee(Employee employee) {
        return bankDetailsRepository.findByEmployee(employee);
    }
    
    public Optional<BankDetails> getPrimaryBankDetails(Employee employee) {
        return bankDetailsRepository.findByEmployeeAndIsPrimary(employee, true);
    }
    
    public BankDetails saveBankDetails(BankDetails bankDetails) {
        // If this is set as primary, unset other primary accounts
        if (bankDetails.getIsPrimary() != null && bankDetails.getIsPrimary()) {
            Optional<BankDetails> existingPrimary = bankDetailsRepository.findByEmployeeAndIsPrimary(
                bankDetails.getEmployee(), true);
            if (existingPrimary.isPresent() && !existingPrimary.get().getId().equals(bankDetails.getId())) {
                existingPrimary.get().setIsPrimary(false);
                bankDetailsRepository.save(existingPrimary.get());
            }
        }
        return bankDetailsRepository.save(bankDetails);
    }
    
    public void deleteBankDetails(Long id) {
        bankDetailsRepository.deleteById(id);
    }
    
    public Optional<BankDetails> getBankDetailsById(Long id) {
        return bankDetailsRepository.findById(id);
    }
}
