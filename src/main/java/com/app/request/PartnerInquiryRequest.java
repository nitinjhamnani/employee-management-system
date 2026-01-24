package com.app.request;

import com.app.enums.InterestedIn;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class PartnerInquiryRequest {

    @NotBlank @Size(max = 150)
    private String name;

    @NotBlank @Size(max = 500)
    private String address;

    @NotBlank
    @Pattern(regexp = "^[0-9]{10}$", message = "Mobile must be 10 digits")
    private String mobileNo;

    @NotBlank @Size(max = 100)
    private String district;

    @NotNull
    private InterestedIn interestedIn;

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
}

