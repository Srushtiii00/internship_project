package com.lostfound.dto.response;

import com.lostfound.entities.Claim;
import com.lostfound.entities.enums.ClaimStatus;

import java.time.LocalDateTime;

public class ClaimResponse {
    private Long id;
    private Long itemId;
    private String itemTitle;
    private String itemCategory;
    private String itemLocation;
    private UserSummaryResponse claimant;
    private String proofDescription;
    private String contactInfo;
    private ClaimStatus status;
    private String remarks;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public ClaimResponse() {
    }

    public static ClaimResponse fromEntity(Claim claim) {
        if (claim == null) return null;
        ClaimResponse dto = new ClaimResponse();
        dto.setId(claim.getId());
        if (claim.getItem() != null) {
            dto.setItemId(claim.getItem().getId());
            dto.setItemTitle(claim.getItem().getTitle());
            dto.setItemCategory(claim.getItem().getCategory());
            dto.setItemLocation(claim.getItem().getLocation());
        }
        dto.setClaimant(UserSummaryResponse.fromEntity(claim.getClaimant()));
        dto.setProofDescription(claim.getProofDescription());
        dto.setContactInfo(claim.getContactInfo());
        dto.setStatus(claim.getStatus());
        dto.setRemarks(claim.getRemarks());
        dto.setCreatedAt(claim.getCreatedAt());
        dto.setUpdatedAt(claim.getUpdatedAt());
        return dto;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getItemId() {
        return itemId;
    }

    public void setItemId(Long itemId) {
        this.itemId = itemId;
    }

    public String getItemTitle() {
        return itemTitle;
    }

    public void setItemTitle(String itemTitle) {
        this.itemTitle = itemTitle;
    }

    public String getItemCategory() {
        return itemCategory;
    }

    public void setItemCategory(String itemCategory) {
        this.itemCategory = itemCategory;
    }

    public String getItemLocation() {
        return itemLocation;
    }

    public void setItemLocation(String itemLocation) {
        this.itemLocation = itemLocation;
    }

    public UserSummaryResponse getClaimant() {
        return claimant;
    }

    public void setClaimant(UserSummaryResponse claimant) {
        this.claimant = claimant;
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

    public ClaimStatus getStatus() {
        return status;
    }

    public void setStatus(ClaimStatus status) {
        this.status = status;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
