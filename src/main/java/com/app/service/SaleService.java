package com.app.service;

import com.app.model.Sale;
import com.app.model.Employee;
import com.app.model.Customer;
import com.app.model.Product;
import com.app.model.Payment;
import com.app.repository.SaleRepository;
import com.app.repository.EmployeeRepository;
import com.app.repository.CustomerRepository;
import com.app.repository.PaymentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class SaleService {
    
    @Autowired
    private SaleRepository saleRepository;
    
    @Autowired
    private EmployeeRepository employeeRepository;
    
    @Autowired
    private CustomerRepository customerRepository;
    
    @Autowired
    private PaymentRepository paymentRepository;
    
    @Autowired
    private ProductService productService;
    
    @Autowired
    private TransactionService transactionService;
    
    public List<Sale> getAllSales() {
        return saleRepository.findAll();
    }
    
    public List<Sale> getSalesByEmployee(Long employeeId) {
        Optional<Employee> employee = employeeRepository.findById(employeeId);
        if (employee.isEmpty()) {
            return List.of();
        }
        List<Sale> sales = saleRepository.findByEmployee(employee.get());
        // Initialize payments for each sale to avoid lazy loading issues
        for (Sale sale : sales) {
            try {
                List<Payment> payments = paymentRepository.findBySaleId(sale.getId());
                sale.setPayments(payments);
            } catch (Exception e) {
                sale.setPayments(java.util.Collections.emptyList());
            }
        }
        return sales;
    }
    
    public List<Sale> getSalesByCustomer(Long customerId) {
        Optional<Customer> customer = customerRepository.findById(customerId);
        return customer.map(saleRepository::findByCustomer).orElse(List.of());
    }
    
    public List<Sale> getSalesByDateRange(LocalDate startDate, LocalDate endDate) {
        return saleRepository.findBySaleDateBetween(startDate, endDate);
    }
    
    public Optional<Sale> getSaleById(Long id) {
        return saleRepository.findById(id);
    }
    
    public Sale saveSale(Sale sale) {
        // Calculate total amount from product or unit price
        if (sale.getProduct() != null && sale.getProduct().getUnitPrice() != null && sale.getQuantity() != null) {
            sale.setUnitPrice(sale.getProduct().getUnitPrice());
            sale.setTotalAmount(sale.getUnitPrice().multiply(BigDecimal.valueOf(sale.getQuantity())));
            // Set product name from product
            if (sale.getProduct().getName() != null) {
                sale.setProductName(sale.getProduct().getName());
            }
        } else if (sale.getUnitPrice() != null && sale.getQuantity() != null) {
            sale.setTotalAmount(sale.getUnitPrice().multiply(BigDecimal.valueOf(sale.getQuantity())));
        }
        
        // Ensure productName is set
        if ((sale.getProductName() == null || sale.getProductName().isEmpty()) && sale.getProduct() != null) {
            sale.setProductName(sale.getProduct().getName());
        }
        
        // Commission will be calculated only when payment is fully completed
        // Set commission to zero initially
        sale.setCommissionAmount(BigDecimal.ZERO);
        
        return saleRepository.save(sale);
    }
    
    /**
     * Calculates and sets commission for the sale.
     * Commission is calculated only for Area Sales Managers.
     */
    private void calculateAndSetCommission(Sale sale) {
        Employee employee = sale.getEmployee();
        if (!"AREA_SALES_MANAGER".equals(employee.getHierarchyLevel())) {
            sale.setCommissionAmount(BigDecimal.ZERO);
            return;
        }
        
        Product product = sale.getProduct();
        if (product == null || sale.getTotalAmount() == null) {
            sale.setCommissionAmount(BigDecimal.ZERO);
            return;
        }
        
        // Calculate commission based on product's commission settings
        BigDecimal commission = product.calculateCommission(sale.getTotalAmount());
        sale.setCommissionAmount(commission);
    }
    
    /**
     * Adds a payment to a sale and generates an invoice number
     */
    public Payment addPayment(Long saleId, BigDecimal amount, String transactionMode, String transactionId, String notes, java.time.LocalDate dueDate) {
        Sale sale = saleRepository.findById(saleId)
                .orElseThrow(() -> new IllegalArgumentException("Sale not found"));
        
        Payment payment = new Payment();
        payment.setSale(sale);
        payment.setAmount(amount);
        payment.setPaymentDate(LocalDateTime.now());
        payment.setTransactionMode(transactionMode);
        payment.setTransactionId(transactionId);
        payment.setNotes(notes);
        
        // Generate invoice number for this transaction
        String invoiceNumber = transactionService.generateInvoiceNumber();
        payment.setInvoiceNumber(invoiceNumber);
        
        Payment savedPayment = paymentRepository.save(payment);
        
        // Update sale due date if provided and sale is not fully paid
        if (dueDate != null && !sale.isFullyPaid()) {
            sale.setDueDate(dueDate);
        }
        
        // Update sale status based on payment
        updateSalePaymentStatus(sale);
        
        return savedPayment;
    }
    
    /**
     * Updates sale status based on payment status
     * Calculates and applies commission only when payment is fully completed
     */
    private void updateSalePaymentStatus(Sale sale) {
        List<Payment> payments = paymentRepository.findBySale(sale);
        BigDecimal totalPaid = payments.stream()
                .map(Payment::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        String previousStatus = sale.getStatus();
        boolean isFullyPaid = totalPaid.compareTo(sale.getTotalAmount()) >= 0;
        boolean commissionNotCalculated = sale.getCommissionAmount() == null || 
                                          sale.getCommissionAmount().compareTo(BigDecimal.ZERO) == 0;
        
        if (isFullyPaid) {
            sale.setStatus("COMPLETED");
            // Calculate and apply commission only when payment is fully completed and commission hasn't been calculated yet
            if (commissionNotCalculated) {
                calculateAndSetCommission(sale);
            }
        } else if (totalPaid.compareTo(BigDecimal.ZERO) > 0) {
            sale.setStatus("PARTIAL");
            // Reset commission to zero if status changes from COMPLETED to PARTIAL (shouldn't happen, but safety check)
            if ("COMPLETED".equals(previousStatus) && !commissionNotCalculated) {
                sale.setCommissionAmount(BigDecimal.ZERO);
            }
        } else {
            sale.setStatus("PENDING");
            // Reset commission to zero if status changes from COMPLETED to PENDING (shouldn't happen, but safety check)
            if ("COMPLETED".equals(previousStatus) && !commissionNotCalculated) {
                sale.setCommissionAmount(BigDecimal.ZERO);
            }
        }
        
        saleRepository.save(sale);
    }
    
    /**
     * Gets all payments for a sale
     */
    public List<Payment> getPaymentsBySale(Long saleId) {
        return paymentRepository.findBySaleId(saleId);
    }
    
    /**
     * Gets total commission for an employee in a date range
     */
    public BigDecimal getTotalCommissionByEmployee(Long employeeId, LocalDate startDate, LocalDate endDate) {
        Optional<Employee> employee = employeeRepository.findById(employeeId);
        if (employee.isEmpty()) {
            return BigDecimal.ZERO;
        }
        
        List<Sale> sales = saleRepository.findByEmployeeAndSaleDateBetween(employee.get(), startDate, endDate);
        return sales.stream()
                .filter(s -> s.getCommissionAmount() != null)
                .map(Sale::getCommissionAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
    
    /**
     * Gets total accumulated commission for an employee (all time)
     */
    public BigDecimal getTotalAccumulatedCommission(Long employeeId) {
        Optional<Employee> employee = employeeRepository.findById(employeeId);
        if (employee.isEmpty()) {
            return BigDecimal.ZERO;
        }
        
        List<Sale> sales = saleRepository.findByEmployee(employee.get());
        return sales.stream()
                .filter(s -> s.getCommissionAmount() != null)
                .map(Sale::getCommissionAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
    
    public void deleteSale(Long id) {
        saleRepository.deleteById(id);
    }
    
    public BigDecimal getTotalSalesByDateRange(LocalDate startDate, LocalDate endDate) {
        List<Sale> sales = saleRepository.findBySaleDateBetween(startDate, endDate);
        return sales.stream()
                   .filter(s -> "COMPLETED".equals(s.getStatus()))
                   .map(Sale::getTotalAmount)
                   .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
    
    public BigDecimal getTotalSalesByEmployee(Long employeeId, LocalDate startDate, LocalDate endDate) {
        Optional<Employee> employee = employeeRepository.findById(employeeId);
        if (employee.isEmpty()) {
            return BigDecimal.ZERO;
        }
        
        List<Sale> sales = saleRepository.findByEmployeeAndSaleDateBetween(employee.get(), startDate, endDate);
        return sales.stream()
                   .filter(s -> "COMPLETED".equals(s.getStatus()))
                   .map(Sale::getTotalAmount)
                   .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
    
    public List<Sale> getSalesByEmployeeAndDateRange(Long employeeId, LocalDate startDate, LocalDate endDate) {
        Optional<Employee> employee = employeeRepository.findById(employeeId);
        if (employee.isEmpty()) {
            return List.of();
        }
        return saleRepository.findByEmployeeAndSaleDateBetween(employee.get(), startDate, endDate);
    }
    
    /**
     * Gets revenue by product for a given date range
     * Returns a map of product name to total revenue
     */
    public java.util.Map<String, BigDecimal> getRevenueByProduct(LocalDate startDate, LocalDate endDate) {
        List<Sale> sales = saleRepository.findBySaleDateBetween(startDate, endDate);
        java.util.Map<String, BigDecimal> revenueMap = new java.util.HashMap<>();
        
        for (Sale sale : sales) {
            // Only count completed sales
            if (!"COMPLETED".equals(sale.getStatus())) {
                continue;
            }
            
            String productName = sale.getProductName();
            if (productName == null || productName.isEmpty()) {
                productName = sale.getProduct() != null ? sale.getProduct().getName() : "Unknown";
            }
            
            BigDecimal amount = sale.getTotalAmount();
            if (amount == null) {
                amount = BigDecimal.ZERO;
            }
            
            revenueMap.put(productName, revenueMap.getOrDefault(productName, BigDecimal.ZERO).add(amount));
        }
        
        return revenueMap;
    }
}

