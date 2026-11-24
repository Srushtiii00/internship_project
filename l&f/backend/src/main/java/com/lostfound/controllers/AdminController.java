package com.lostfound.controllers;

import com.lostfound.config.UserDetailsImpl;
import com.lostfound.dto.response.ApiResponse;
import com.lostfound.dto.response.DashboardStatsResponse;
import com.lostfound.dto.response.UserSummaryResponse;
import com.lostfound.services.AdminService;
import com.lostfound.services.ItemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    @Autowired
    private AdminService adminService;

    @Autowired
    private ItemService itemService;

    @GetMapping("/stats")
    public ResponseEntity<ApiResponse<DashboardStatsResponse>> getStats() {
        DashboardStatsResponse stats = adminService.getDashboardStats();
        return ResponseEntity.ok(ApiResponse.success("Dashboard metrics retrieved", stats));
    }

    @GetMapping("/users")
    public ResponseEntity<ApiResponse<List<UserSummaryResponse>>> getAllUsers() {
        List<UserSummaryResponse> users = adminService.getAllUsers();
        return ResponseEntity.ok(ApiResponse.success("Users retrieved", users));
    }

    @DeleteMapping("/users/{id}")
    public ResponseEntity<ApiResponse<String>> deleteUser(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetailsImpl userPrincipal
    ) {
        String message = adminService.deleteUser(id, userPrincipal.getId());
        return ResponseEntity.ok(ApiResponse.success(message));
    }

    @DeleteMapping("/items/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteItem(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetailsImpl userPrincipal
    ) {
        itemService.deleteItem(id, userPrincipal.getId(), true);
        return ResponseEntity.ok(ApiResponse.success("Item removed by administrator"));
    }
}
