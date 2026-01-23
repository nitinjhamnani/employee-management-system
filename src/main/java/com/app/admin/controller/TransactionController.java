package com.app.admin.controller;

import com.app.model.Payment;
import com.app.service.TransactionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/admin/transactions")
public class TransactionController {
    
    @Autowired
    private TransactionService transactionService;
    
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
}
