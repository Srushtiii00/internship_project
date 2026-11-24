package com.lostfound.services;

import com.lostfound.dto.response.DashboardStatsResponse;
import com.lostfound.dto.response.UserSummaryResponse;
import com.lostfound.entities.enums.ClaimStatus;
import com.lostfound.entities.enums.ItemStatus;
import com.lostfound.entities.enums.ItemType;
import com.lostfound.exceptions.BadRequestException;
import com.lostfound.exceptions.ResourceNotFoundException;
import com.lostfound.repositories.ClaimRepository;
import com.lostfound.repositories.ItemRepository;
import com.lostfound.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class AdminService {

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private ClaimRepository claimRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private FileStorageService fileStorageService;

    @Transactional(readOnly = true)
    public DashboardStatsResponse getDashboardStats() {
        long totalItems = itemRepository.count();
        long totalLost = itemRepository.countByType(ItemType.LOST);
        long totalFound = itemRepository.countByType(ItemType.FOUND);
        long totalRecovered = itemRepository.countByStatus(ItemStatus.RECOVERED);
        long totalClaims = claimRepository.count();
        long pendingClaims = claimRepository.countByStatus(ClaimStatus.PENDING);
        long totalUsers = userRepository.count();

        return new DashboardStatsResponse(
                totalItems,
                totalLost,
                totalFound,
                totalRecovered,
                totalClaims,
                pendingClaims,
                totalUsers
        );
    }

    @Transactional(readOnly = true)
    public List<UserSummaryResponse> getAllUsers() {
        return userRepository.findAll().stream()
                .map(UserSummaryResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional
    public String deleteUser(Long userId, Long currentAdminId) {
        if (userId.equals(currentAdminId)) {
            throw new BadRequestException("You cannot delete or deactivate your own administrator account");
        }

        var user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        if (user.getRole() == com.lostfound.entities.enums.Role.ROLE_ADMIN || "admin".equalsIgnoreCase(user.getUsername())) {
            throw new BadRequestException("Administrator accounts cannot be deleted or deactivated via this action");
        }

        boolean hasItems = !itemRepository.findByUserIdOrderByCreatedAtDesc(userId).isEmpty();
        boolean hasClaims = !claimRepository.findByClaimantIdOrderByCreatedAtDesc(userId).isEmpty();

        if (hasItems || hasClaims) {
            user.setActive(false);
            userRepository.save(user);
            return "User has existing listings or claims; account has been safely deactivated to preserve record integrity.";
        } else {
            userRepository.delete(user);
            return "User account deleted successfully.";
        }
    }
}
