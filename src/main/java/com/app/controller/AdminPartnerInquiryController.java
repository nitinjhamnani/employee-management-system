package com.app.controller;

import com.app.service.PartnerInquiryService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin/inquiries")
public class AdminPartnerInquiryController {

    private final PartnerInquiryService service;

    public AdminPartnerInquiryController(PartnerInquiryService service) {
        this.service = service;
    }

    @GetMapping
    public String list(Model model) {
        model.addAttribute("inquiries", service.findAll());
        return "admin/inquiries/list";
    }
}