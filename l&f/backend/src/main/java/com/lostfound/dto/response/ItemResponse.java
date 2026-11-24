package com.lostfound.dto.response;

import com.lostfound.entities.Item;
import com.lostfound.entities.enums.ItemStatus;
import com.lostfound.entities.enums.ItemType;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class ItemResponse {
    private Long id;
    private String title;
    private String description;
    private String category;
    private ItemType type;
    private ItemStatus status;
    private String location;
    private LocalDate itemDate;
    private String imageUrl;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private PublicUserSummaryResponse reporter;

    public ItemResponse() {
    }

    public static ItemResponse fromEntity(Item item) {
        if (item == null) return null;
        ItemResponse dto = new ItemResponse();
        dto.setId(item.getId());
        dto.setTitle(item.getTitle());
        dto.setDescription(item.getDescription());
        dto.setCategory(item.getCategory());
        dto.setType(item.getType());
        dto.setStatus(item.getStatus());
        dto.setLocation(item.getLocation());
        dto.setItemDate(item.getItemDate());
        dto.setImageUrl(item.getImageUrl());
        dto.setCreatedAt(item.getCreatedAt());
        dto.setUpdatedAt(item.getUpdatedAt());
        dto.setReporter(PublicUserSummaryResponse.fromEntity(item.getUser()));
        return dto;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public ItemType getType() {
        return type;
    }

    public void setType(ItemType type) {
        this.type = type;
    }

    public ItemStatus getStatus() {
        return status;
    }

    public void setStatus(ItemStatus status) {
        this.status = status;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public LocalDate getItemDate() {
        return itemDate;
    }

    public void setItemDate(LocalDate itemDate) {
        this.itemDate = itemDate;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
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

    public PublicUserSummaryResponse getReporter() {
        return reporter;
    }

    public void setReporter(PublicUserSummaryResponse reporter) {
        this.reporter = reporter;
    }
}
