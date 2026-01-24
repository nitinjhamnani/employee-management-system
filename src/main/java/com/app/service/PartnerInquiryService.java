package com.app.service;
import com.app.model.PartnerInquiry;
import com.app.repository.PartnerInquiryRepository;
import com.app.request.PartnerInquiryRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class PartnerInquiryService {

    private final PartnerInquiryRepository repo;

    public PartnerInquiryService(PartnerInquiryRepository repo) {
        this.repo = repo;
    }

    @Transactional
    public void create(PartnerInquiryRequest req) {
        PartnerInquiry e = new PartnerInquiry();
        e.setName(req.getName());
        e.setAddress(req.getAddress());
        e.setMobileNo(req.getMobileNo());
        e.setDistrict(req.getDistrict());
        e.setInterestedIn(req.getInterestedIn());
        repo.save(e);
        System.out.println("SAVED ID = " + e.getId());
    }

    public List<PartnerInquiry> findAll() {
        return repo.findAll();
    }
}

