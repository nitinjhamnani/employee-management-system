package com.app.employee.controller;

import com.app.model.Payment;
import com.app.model.Employee;
import com.app.service.TransactionService;
import com.app.service.EmployeeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/employee/transactions")
public class EmployeeTransactionController {
    
    @Autowired
    private TransactionService transactionService;
    
    @Autowired
    private EmployeeService employeeService;
    
    private Employee getCurrentEmployee() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        return employeeService.getEmployeeByUsername(username)
                .orElseThrow(() -> new RuntimeException("Employee not found"));
    }
    
    @GetMapping
    public String listTransactions(Model model,
                                   @RequestParam(required = false) String search,
                                   @RequestParam(required = false) String invoiceNumber) {
        Employee currentEmployee = getCurrentEmployee();

        // Get transactions for current employee and hierarchy
        List<Payment> transactions = transactionService.getTransactionsForEmployee(currentEmployee);

        // If current employee is a manager, also get settled transactions that need approval
        List<Employee> directReports = employeeService.getDirectReports(currentEmployee);
        if (!directReports.isEmpty()) {
            // Get all settled but unapproved transactions for employees in hierarchy
            List<Payment> pendingApprovals = transactionService.getAllTransactions().stream()
                    .filter(t -> Boolean.TRUE.equals(t.getSettled()) && !Boolean.TRUE.equals(t.getApproved()))
                    .filter(t -> transactionService.hasAccessToTransaction(currentEmployee, t))
                    .toList();

            // Combine transactions, removing duplicates
            transactions = java.util.stream.Stream.concat(transactions.stream(), pendingApprovals.stream())
                    .distinct()
                    .collect(java.util.stream.Collectors.toList());
        }
        
        // Filter by search term (customer name, product name, invoice number)
        if (search != null && !search.isEmpty()) {
            String searchLower = search.toLowerCase();
            transactions = transactions.stream()
                    .filter(t -> {
                        if (t.getSale() != null && t.getSale().getCustomer() != null) {
                            String customerName = t.getSale().getCustomer().getContactPerson().toLowerCase();
                            if (customerName.contains(searchLower)) return true;
                        }
                        if (t.getSale() != null && t.getSale().getProductName() != null) {
                            String productName = t.getSale().getProductName().toLowerCase();
                            if (productName.contains(searchLower)) return true;
                        }
                        if (t.getInvoiceNumber() != null) {
                            String invNum = t.getInvoiceNumber().toLowerCase();
                            if (invNum.contains(searchLower)) return true;
                        }
                        return false;
                    })
                    .toList();
        }
        
        // Filter by invoice number if provided
        if (invoiceNumber != null && !invoiceNumber.isEmpty()) {
            transactions = transactions.stream()
                    .filter(t -> t.getInvoiceNumber() != null && 
                               t.getInvoiceNumber().equalsIgnoreCase(invoiceNumber))
                    .toList();
        }
        
        model.addAttribute("transactions", transactions);
        model.addAttribute("search", search);
        model.addAttribute("invoiceNumber", invoiceNumber);
        model.addAttribute("currentEmployee", currentEmployee);
        return "employee/transactions/list";
    }
    
    @GetMapping("/view/{id}")
    public String viewTransaction(@PathVariable Long id, Model model) {
        Employee currentEmployee = getCurrentEmployee();
        Optional<Payment> transactionOpt = transactionService.getTransactionById(id);
        
        if (transactionOpt.isEmpty()) {
            return "redirect:/employee/transactions?error=Transaction not found";
        }
        
        Payment transaction = transactionOpt.get();
        
        // Verify access
        if (!transactionService.hasAccessToTransaction(currentEmployee, transaction)) {
            return "redirect:/employee/transactions?error=You do not have access to this transaction";
        }
        
        model.addAttribute("transaction", transaction);
        model.addAttribute("currentEmployee", currentEmployee);
        return "employee/transactions/view";
    }
    
    @GetMapping("/invoice/{id}")
    public String viewInvoice(@PathVariable Long id, Model model) {
        Employee currentEmployee = getCurrentEmployee();
        Optional<Payment> transactionOpt = transactionService.getTransactionById(id);
        
        if (transactionOpt.isEmpty()) {
            return "redirect:/employee/transactions?error=Transaction not found";
        }
        
        Payment transaction = transactionOpt.get();
        
        // Verify access
        if (!transactionService.hasAccessToTransaction(currentEmployee, transaction)) {
            return "redirect:/employee/transactions?error=You do not have access to this transaction";
        }
        
        model.addAttribute("transaction", transaction);
        model.addAttribute("currentEmployee", currentEmployee);
        return "employee/transactions/invoice";
    }

    @PostMapping("/approve/{id}")
    public String approveTransaction(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            Employee currentEmployee = getCurrentEmployee();
            Optional<Payment> transactionOpt = transactionService.getTransactionById(id);

            if (transactionOpt.isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "Transaction not found");
                return "redirect:/employee/transactions";
            }

            Payment transaction = transactionOpt.get();

            // Verify access to transaction
            if (!transactionService.hasAccessToTransaction(currentEmployee, transaction)) {
                redirectAttributes.addFlashAttribute("error", "You do not have access to this transaction");
                return "redirect:/employee/transactions/view/" + id;
            }

            // Check if transaction is settled first
            if (!Boolean.TRUE.equals(transaction.getSettled())) {
                redirectAttributes.addFlashAttribute("error", "Transaction must be settled before approval");
                return "redirect:/employee/transactions/view/" + id;
            }

            // Check if transaction is already approved
            if (Boolean.TRUE.equals(transaction.getApproved())) {
                redirectAttributes.addFlashAttribute("error", "Transaction is already approved");
                return "redirect:/employee/transactions/view/" + id;
            }

            // Check if current employee can approve this transaction
            if (!canApproveTransaction(transaction, currentEmployee)) {
                redirectAttributes.addFlashAttribute("error", "You are not authorized to approve this transaction");
                return "redirect:/employee/transactions/view/" + id;
            }

            // Approve the transaction
            transaction.setApproved(true);
            transaction.setApprovedBy(currentEmployee);
            transactionService.saveTransaction(transaction);

            // Calculate commission after approval
            transactionService.calculateCommissionAfterApproval(transaction);

            redirectAttributes.addFlashAttribute("message", "Transaction approved successfully");
            return "redirect:/employee/transactions/view/" + id;

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error approving transaction: " + e.getMessage());
            return "redirect:/employee/transactions";
        }
    }

    private boolean canApproveTransaction(Payment transaction, Employee approver) {
        // Admin users can approve (though they would use admin portal)
        if (approver.getUsername().startsWith("admin")) {
            return true;
        }

        // Check if approver is a manager and the transaction belongs to someone in their hierarchy
        if (transaction.getSale() != null && transaction.getSale().getEmployee() != null) {
            Employee saleEmployee = transaction.getSale().getEmployee();

            // Check if approver is in the reporting hierarchy of the sale employee
            return isInReportingHierarchy(saleEmployee, approver);
        }

        return false;
    }

    private boolean isInReportingHierarchy(Employee employee, Employee potentialManager) {
        Employee current = employee;

        // Traverse up the hierarchy
        while (current != null) {
            if (current.getReportingManager() != null && current.getReportingManager().getId().equals(potentialManager.getId())) {
                return true;
            }

            // For ASM, any higher level manager can approve
            if ("AREA_SALES_MANAGER".equals(current.getHierarchyLevel())) {
                // Any manager above ASM level can approve
                if (potentialManager.getHierarchyLevel() != null &&
                    getHierarchyLevelOrder(potentialManager.getHierarchyLevel()) < getHierarchyLevelOrder("AREA_SALES_MANAGER")) {
                    return true;
                }
            }

            current = current.getReportingManager();
        }

        return false;
    }

    private int getHierarchyLevelOrder(String hierarchyLevel) {
        switch (hierarchyLevel) {
            case "PROMOTER": return 1;
            case "ZONAL_HEAD": return 2;
            case "CLUSTER_HEAD": return 3;
            case "AREA_SALES_MANAGER": return 4;
            default: return 5;
        }
    }
}
