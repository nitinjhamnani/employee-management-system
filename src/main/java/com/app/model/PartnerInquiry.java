package com.app.model;

import com.app.enums.InterestedIn;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "partner_inquiries")
public class PartnerInquiry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 150)
    private String name;

    @Column(nullable = false, length = 500)
    private String address;

    @Column(nullable = false, length = 20)
    private String mobileNo;

    @Column(nullable = false, length = 100)
    private String district;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private InterestedIn interestedIn;

    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();


    public Long getId() { return id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getMobileNo() { return mobileNo; }
    public void setMobileNo(String mobileNo) { this.mobileNo = mobileNo; }

    public String getDistrict() { return district; }
    public void setDistrict(String district) { this.district = district; }

    public InterestedIn getInterestedIn() { return interestedIn; }
    public void setInterestedIn(InterestedIn interestedIn) { this.interestedIn = interestedIn; }

    public LocalDateTime getCreatedAt() { return createdAt; }
}