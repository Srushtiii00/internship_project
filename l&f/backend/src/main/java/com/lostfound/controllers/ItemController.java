package com.lostfound.controllers;

import com.lostfound.config.UserDetailsImpl;
import com.lostfound.dto.request.ItemRequest;
import com.lostfound.dto.request.ItemStatusUpdateRequest;
import com.lostfound.dto.response.ApiResponse;
import com.lostfound.dto.response.ItemResponse;
import com.lostfound.entities.enums.ItemStatus;
import com.lostfound.entities.enums.ItemType;
import com.lostfound.services.ItemService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/items")
public class ItemController {

    @Autowired
    private ItemService itemService;

    @GetMapping
    public ResponseEntity<ApiResponse<Page<ItemResponse>>> getItems(
            @RequestParam(required = false) ItemType type,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) ItemStatus status,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size
    ) {
        Page<ItemResponse> items = itemService.getFilteredItems(type, category, status, keyword, page, size);
        return ResponseEntity.ok(ApiResponse.success("Items retrieved successfully", items));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ItemResponse>> getItemById(@PathVariable Long id) {
        ItemResponse item = itemService.getItemById(id);
        return ResponseEntity.ok(ApiResponse.success("Item details retrieved", item));
    }

    @GetMapping("/my-items")
    public ResponseEntity<ApiResponse<List<ItemResponse>>> getMyItems(@AuthenticationPrincipal UserDetailsImpl userPrincipal) {
        List<ItemResponse> items = itemService.getMyItems(userPrincipal.getId());
        return ResponseEntity.ok(ApiResponse.success("Personal listings retrieved", items));
    }

    @PostMapping(consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    public ResponseEntity<ApiResponse<ItemResponse>> createItem(
            @ModelAttribute @Valid ItemRequest request,
            @RequestParam(value = "image", required = false) MultipartFile image,
            @AuthenticationPrincipal UserDetailsImpl userPrincipal
    ) {
        ItemResponse created = itemService.createItem(request, image, userPrincipal.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("Item reported successfully", created));
    }

    @PutMapping(value = "/{id}", consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    public ResponseEntity<ApiResponse<ItemResponse>> updateItem(
            @PathVariable Long id,
            @ModelAttribute @Valid ItemRequest request,
            @RequestParam(value = "image", required = false) MultipartFile image,
            @AuthenticationPrincipal UserDetailsImpl userPrincipal
    ) {
        boolean isAdmin = userPrincipal.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        ItemResponse updated = itemService.updateItem(id, request, image, userPrincipal.getId(), isAdmin);
        return ResponseEntity.ok(ApiResponse.success("Item updated successfully", updated));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteItem(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetailsImpl userPrincipal
    ) {
        boolean isAdmin = userPrincipal.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        itemService.deleteItem(id, userPrincipal.getId(), isAdmin);
        return ResponseEntity.ok(ApiResponse.success("Item deleted successfully"));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<ApiResponse<ItemResponse>> updateStatus(
            @PathVariable Long id,
            @Valid @RequestBody ItemStatusUpdateRequest request,
            @AuthenticationPrincipal UserDetailsImpl userPrincipal
    ) {
        boolean isAdmin = userPrincipal.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        ItemResponse updated = itemService.updateItemStatus(id, request.getStatus(), userPrincipal.getId(), isAdmin);
        return ResponseEntity.ok(ApiResponse.success("Item status updated", updated));
    }
}
