package com.lostfound.controllers;

import com.lostfound.config.UserDetailsImpl;
import com.lostfound.dto.request.ClaimRequest;
import com.lostfound.dto.response.ApiResponse;
import com.lostfound.dto.response.ClaimResponse;
import com.lostfound.entities.enums.ClaimStatus;
import com.lostfound.services.ClaimService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/claims")
public class ClaimController {

    @Autowired
    private ClaimService claimService;

    @PostMapping
    public ResponseEntity<ApiResponse<ClaimResponse>> submitClaim(
            @Valid @RequestBody ClaimRequest request,
            @AuthenticationPrincipal UserDetailsImpl userPrincipal
    ) {
        ClaimResponse claim = claimService.submitClaim(request, userPrincipal.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("Claim submitted successfully", claim));
    }

    @GetMapping("/my-claims")
    public ResponseEntity<ApiResponse<List<ClaimResponse>>> getMyClaims(@AuthenticationPrincipal UserDetailsImpl userPrincipal) {
        List<ClaimResponse> claims = claimService.getMyClaims(userPrincipal.getId());
        return ResponseEntity.ok(ApiResponse.success("Claims retrieved successfully", claims));
    }

    @GetMapping("/received")
    public ResponseEntity<ApiResponse<List<ClaimResponse>>> getClaimsReceived(@AuthenticationPrincipal UserDetailsImpl userPrincipal) {
        List<ClaimResponse> claims = claimService.getClaimsReceived(userPrincipal.getId());
        return ResponseEntity.ok(ApiResponse.success("Received claims retrieved successfully", claims));
    }

    @GetMapping("/item/{itemId}")
    public ResponseEntity<ApiResponse<List<ClaimResponse>>> getClaimsForItem(
            @PathVariable Long itemId,
            @AuthenticationPrincipal UserDetailsImpl userPrincipal
    ) {
        boolean isAdmin = userPrincipal.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        List<ClaimResponse> claims = claimService.getClaimsForItem(itemId, userPrincipal.getId(), isAdmin);
        return ResponseEntity.ok(ApiResponse.success("Item claims retrieved", claims));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<ApiResponse<ClaimResponse>> updateClaimStatus(
            @PathVariable Long id,
            @RequestBody Map<String, String> body,
            @AuthenticationPrincipal UserDetailsImpl userPrincipal
    ) {
        String statusStr = body.get("status");
        String remarks = body.get("remarks");
        ClaimStatus status = ClaimStatus.valueOf(statusStr.toUpperCase());
        boolean isAdmin = userPrincipal.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        ClaimResponse updated = claimService.updateClaimStatus(id, status, remarks, userPrincipal.getId(), isAdmin);
        return ResponseEntity.ok(ApiResponse.success("Claim status updated", updated));
    }
}
