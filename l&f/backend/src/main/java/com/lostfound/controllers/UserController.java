package com.lostfound.controllers;

import com.lostfound.config.UserDetailsImpl;
import com.lostfound.dto.response.ApiResponse;
import com.lostfound.dto.response.UserSummaryResponse;
import com.lostfound.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserService userService;

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserSummaryResponse>> getMyProfile(@AuthenticationPrincipal UserDetailsImpl userPrincipal) {
        UserSummaryResponse profile = userService.getUserProfile(userPrincipal.getId());
        return ResponseEntity.ok(ApiResponse.success("Profile retrieved", profile));
    }

    @PutMapping("/me")
    public ResponseEntity<ApiResponse<UserSummaryResponse>> updateProfile(
            @RequestBody Map<String, String> body,
            @AuthenticationPrincipal UserDetailsImpl userPrincipal
    ) {
        String fullName = body.get("fullName");
        String phoneNumber = body.get("phoneNumber");
        UserSummaryResponse updated = userService.updateProfile(userPrincipal.getId(), fullName, phoneNumber);
        return ResponseEntity.ok(ApiResponse.success("Profile updated successfully", updated));
    }
}
