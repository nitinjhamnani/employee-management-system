package com.app.repository;

import com.app.model.Payment;
import com.app.model.Sale;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {
    List<Payment> findBySale(Sale sale);
    List<Payment> findBySaleId(Long saleId);
    Payment findByInvoiceNumber(String invoiceNumber);
    
    @Query("SELECT p FROM Payment p JOIN p.sale s WHERE s.employee.id = :employeeId")
    List<Payment> findBySaleEmployeeId(@Param("employeeId") Long employeeId);
    
    @Query("SELECT p FROM Payment p JOIN p.sale s WHERE s.employee.id IN :employeeIds")
    List<Payment> findBySaleEmployeeIdIn(@Param("employeeIds") List<Long> employeeIds);
}
