package com.lostfound.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class ClaimRequest {

    @NotNull(message = "Item ID is required")
    private Long itemId;

    @NotBlank(message = "Proof description is required")
    private String proofDescription;

    @NotBlank(message = "Contact info is required")
    private String contactInfo;

    public ClaimRequest() {
    }

    public ClaimRequest(Long itemId, String proofDescription, String contactInfo) {
        this.itemId = itemId;
        this.proofDescription = proofDescription;
        this.contactInfo = contactInfo;
    }

    public Long getItemId() {
        return itemId;
    }

    public void setItemId(Long itemId) {
        this.itemId = itemId;
    }

    public String getProofDescription() {
        return proofDescription;
    }

    public void setProofDescription(String proofDescription) {
        this.proofDescription = proofDescription;
    }

    public String getContactInfo() {
        return contactInfo;
    }

    public void setContactInfo(String contactInfo) {
        this.contactInfo = contactInfo;
    }
}
