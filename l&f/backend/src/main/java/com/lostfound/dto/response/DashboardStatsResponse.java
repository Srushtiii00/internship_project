package com.lostfound.dto.response;

public class DashboardStatsResponse {
    private long totalItems;
    private long totalLost;
    private long totalFound;
    private long totalRecovered;
    private long totalClaims;
    private long pendingClaims;
    private long totalUsers;

    public DashboardStatsResponse() {
    }

    public DashboardStatsResponse(long totalItems, long totalLost, long totalFound, long totalRecovered,
                                  long totalClaims, long pendingClaims, long totalUsers) {
        this.totalItems = totalItems;
        this.totalLost = totalLost;
        this.totalFound = totalFound;
        this.totalRecovered = totalRecovered;
        this.totalClaims = totalClaims;
        this.pendingClaims = pendingClaims;
        this.totalUsers = totalUsers;
    }

    public long getTotalItems() {
        return totalItems;
    }

    public void setTotalItems(long totalItems) {
        this.totalItems = totalItems;
    }

    public long getTotalLost() {
        return totalLost;
    }

    public void setTotalLost(long totalLost) {
        this.totalLost = totalLost;
    }

    public long getTotalFound() {
        return totalFound;
    }

    public void setTotalFound(long totalFound) {
        this.totalFound = totalFound;
    }

    public long getTotalRecovered() {
        return totalRecovered;
    }

    public void setTotalRecovered(long totalRecovered) {
        this.totalRecovered = totalRecovered;
    }

    public long getTotalClaims() {
        return totalClaims;
    }

    public void setTotalClaims(long totalClaims) {
        this.totalClaims = totalClaims;
    }

    public long getPendingClaims() {
        return pendingClaims;
    }

    public void setPendingClaims(long pendingClaims) {
        this.pendingClaims = pendingClaims;
    }

    public long getTotalUsers() {
        return totalUsers;
    }

    public void setTotalUsers(long totalUsers) {
        this.totalUsers = totalUsers;
    }
}
