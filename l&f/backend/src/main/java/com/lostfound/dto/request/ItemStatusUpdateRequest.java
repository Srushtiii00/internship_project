package com.lostfound.dto.request;

import com.lostfound.entities.enums.ItemStatus;
import jakarta.validation.constraints.NotNull;

public class ItemStatusUpdateRequest {

    @NotNull(message = "Status cannot be null")
    private ItemStatus status;

    public ItemStatusUpdateRequest() {
    }

    public ItemStatusUpdateRequest(ItemStatus status) {
        this.status = status;
    }

    public ItemStatus getStatus() {
        return status;
    }

    public void setStatus(ItemStatus status) {
        this.status = status;
    }
}
