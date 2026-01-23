package com.app.admin.controller;

import com.app.config.AuditHelper;
import com.app.model.Product;
import com.app.service.ProductService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/admin/products")
public class ProductController {
    
    @Autowired
    private ProductService productService;
    
    @GetMapping
    public String listProducts(Model model, @RequestParam(required = false) String search) {
        List<Product> products;
        if (search != null && !search.isEmpty()) {
            products = productService.searchProducts(search);
        } else {
            products = productService.getAllProducts();
        }
        model.addAttribute("products", products);
        model.addAttribute("search", search);
        return "admin/products/list";
    }
    
    @GetMapping("/new")
    public String showProductForm(Model model) {
        model.addAttribute("product", new Product());
        return "admin/products/form";
    }
    
    @GetMapping("/edit/{id}")
    public String editProduct(@PathVariable Long id, Model model) {
        Product product = productService.getProductById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid product ID: " + id));
        model.addAttribute("product", product);
        return "admin/products/form";
    }
    
    @PostMapping("/save")
    public String saveProduct(@Valid @ModelAttribute Product product,
                              BindingResult result,
                              Model model,
                              RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            return "admin/products/form";
        }
        
        // Check for duplicate name
        if (product.getId() == null) {
            if (productService.nameExists(product.getName())) {
                result.rejectValue("name", "error.product", "Product name already exists");
                return "admin/products/form";
            }
        } else {
            if (productService.nameExistsForOtherProduct(product.getName(), product.getId())) {
                result.rejectValue("name", "error.product", "Product name already exists");
                return "admin/products/form";
            }
        }
        
        // Validate commission value based on type
        if ("PERCENTAGE".equals(product.getCommissionType()) && 
            product.getCommissionValue() != null && 
            product.getCommissionValue().compareTo(java.math.BigDecimal.valueOf(100)) > 0) {
            result.rejectValue("commissionValue", "error.product", "Percentage commission cannot exceed 100%");
            return "admin/products/form";
        }
        
        String audit = AuditHelper.currentUserAuditString();
        if (product.getId() == null) {
            product.setCreatedBy(audit);
        } else {
            productService.getProductById(product.getId())
                    .ifPresent(existing -> product.setCreatedBy(existing.getCreatedBy()));
            product.setLastUpdatedBy(audit);
        }
        
        productService.saveProduct(product);
        redirectAttributes.addFlashAttribute("message", "Product saved successfully!");
        return "redirect:/admin/products";
    }
    
    @GetMapping("/delete/{id}")
    public String deleteProduct(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        productService.deleteProduct(id);
        redirectAttributes.addFlashAttribute("message", "Product deleted successfully!");
        return "redirect:/admin/products";
    }
    
    @GetMapping("/view/{id}")
    public String viewProduct(@PathVariable Long id, Model model) {
        Product product = productService.getProductById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid product ID: " + id));
        model.addAttribute("product", product);
        model.addAttribute("createdByDisplay", AuditHelper.formatAuditForDisplay(product.getCreatedBy()));
        model.addAttribute("lastUpdatedByDisplay", AuditHelper.formatAuditForDisplay(product.getLastUpdatedBy()));
        return "admin/products/view";
    }
}
