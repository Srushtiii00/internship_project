package com.lostfound.dto.response;

import com.lostfound.entities.User;

public class PublicUserSummaryResponse {
    private Long id;
    private String username;
    private String fullName;

    public PublicUserSummaryResponse() {
    }

    public PublicUserSummaryResponse(Long id, String username, String fullName) {
        this.id = id;
        this.username = username;
        this.fullName = fullName;
    }

    public static PublicUserSummaryResponse fromEntity(User user) {
        if (user == null) return null;
        return new PublicUserSummaryResponse(
                user.getId(),
                user.getUsername(),
                user.getFullName()
        );
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }
}
