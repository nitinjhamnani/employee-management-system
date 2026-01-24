package com.app.admin.controller;

import com.app.model.Employee;
import com.app.model.Payment;
import com.app.service.EmployeeService;
import com.app.service.TransactionService;
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
@RequestMapping("/admin/transactions")
public class TransactionController {

    @Autowired
    private TransactionService transactionService;

    @Autowired
    private EmployeeService employeeService;
    
    @GetMapping
    public String listTransactions(Model model, 
                                   @RequestParam(required = false) String search,
                                   @RequestParam(required = false) String invoiceNumber) {
        List<Payment> transactions = transactionService.getAllTransactions();
        
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
        return "admin/transactions/list";
    }
    
    @GetMapping("/view/{id}")
    public String viewTransaction(@PathVariable Long id, Model model) {
        Optional<Payment> transactionOpt = transactionService.getTransactionById(id);
        if (transactionOpt.isEmpty()) {
            return "redirect:/admin/transactions?error=Transaction not found";
        }
        
        Payment transaction = transactionOpt.get();
        model.addAttribute("transaction", transaction);
        return "admin/transactions/view";
    }
    
    @GetMapping("/invoice/{id}")
    public String viewInvoice(@PathVariable Long id, Model model) {
        Optional<Payment> transactionOpt = transactionService.getTransactionById(id);
        if (transactionOpt.isEmpty()) {
            return "redirect:/admin/transactions?error=Transaction not found";
        }

        Payment transaction = transactionOpt.get();
        model.addAttribute("transaction", transaction);
        return "admin/transactions/invoice";
    }

    @PostMapping("/settle/{id}")
    public String settleTransaction(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            Optional<Payment> transactionOpt = transactionService.getTransactionById(id);
            if (transactionOpt.isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "Transaction not found");
                return "redirect:/admin/transactions";
            }

            Payment transaction = transactionOpt.get();
            transaction.setSettled(true);
            transactionService.saveTransaction(transaction);

            redirectAttributes.addFlashAttribute("message", "Transaction marked as settled successfully");
            return "redirect:/admin/transactions/view/" + id;
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error settling transaction: " + e.getMessage());
            return "redirect:/admin/transactions";
        }
    }

    @PostMapping("/approve/{id}")
    public String approveTransaction(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            Optional<Payment> transactionOpt = transactionService.getTransactionById(id);
            if (transactionOpt.isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "Transaction not found");
                return "redirect:/admin/transactions";
            }

            Payment transaction = transactionOpt.get();

            // Check if transaction is settled first
            if (!Boolean.TRUE.equals(transaction.getSettled())) {
                redirectAttributes.addFlashAttribute("error", "Transaction must be settled before approval");
                return "redirect:/admin/transactions/view/" + id;
            }

            // Get current admin/employee who is approving
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String username = auth.getName();

            Optional<Employee> approverOpt = employeeService.getEmployeeByUsername(username);
            if (approverOpt.isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "Approver not found");
                return "redirect:/admin/transactions/view/" + id;
            }

            Employee approver = approverOpt.get();

            // Check if approver can approve this transaction
            if (!canApproveTransaction(transaction, approver)) {
                redirectAttributes.addFlashAttribute("error", "You are not authorized to approve this transaction");
                return "redirect:/admin/transactions/view/" + id;
            }

            // Approve the transaction
            transaction.setApproved(true);
            transaction.setApprovedBy(approver);
            transactionService.saveTransaction(transaction);

            // Calculate commission after approval
            transactionService.calculateCommissionAfterApproval(transaction);

            redirectAttributes.addFlashAttribute("message", "Transaction approved successfully");
            return "redirect:/admin/transactions/view/" + id;
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error approving transaction: " + e.getMessage());
            return "redirect:/admin/transactions";
        }
    }

    private boolean canApproveTransaction(Payment transaction, Employee approver) {
        // For admin users, allow approval of all transactions
        if (approver.getUsername().startsWith("admin")) {
            return true;
        }

        // For employee users, check hierarchy
        if (transaction.getSale() != null && transaction.getSale().getEmployee() != null) {
            Employee saleEmployee = transaction.getSale().getEmployee();

            // Check if approver is in the hierarchy of the sale employee
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
