package com.app.controller;

import com.app.enums.InterestedIn;
import com.app.request.PartnerInquiryRequest;
import com.app.service.PartnerInquiryService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class HomeController {


    private final PartnerInquiryService service;

    public HomeController(PartnerInquiryService service) {
        this.service = service;
    }
    
    @GetMapping("/")
    public String home() {
        return "index";
    }


    // form submit
    @PostMapping("/partner-with-us")
    public String submitPartnerWithUs(
            @Valid @ModelAttribute("partnerReq") PartnerInquiryRequest req,
            BindingResult result,
            RedirectAttributes ra,
            Model model
    ) {
        System.out.println("REQ = " + req);   //  add this

        if (result.hasErrors()) {
            System.out.println("ERRORS = " + result); //  add this
            model.addAttribute("interests", InterestedIn.values());
            return "index";
        }

        service.create(req);
        ra.addFlashAttribute("message", "Thank you! We will contact you soon.");
        return "redirect:/#contact";
    }

    @ModelAttribute("partnerReq")
    public PartnerInquiryRequest partnerReq() {
        return new PartnerInquiryRequest();
    }

    @ModelAttribute("interests")
    public InterestedIn[] interests() {
        return InterestedIn.values();
    }

}
