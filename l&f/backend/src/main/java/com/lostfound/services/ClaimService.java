package com.lostfound.services;

import com.lostfound.dto.request.ClaimRequest;
import com.lostfound.dto.response.ClaimResponse;
import com.lostfound.entities.Claim;
import com.lostfound.entities.Item;
import com.lostfound.entities.User;
import com.lostfound.entities.enums.ClaimStatus;
import com.lostfound.entities.enums.ItemStatus;
import com.lostfound.exceptions.AccessDeniedException;
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
public class ClaimService {

    @Autowired
    private ClaimRepository claimRepository;

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EmailService emailService;

    @Transactional
    public ClaimResponse submitClaim(ClaimRequest request, Long claimantId) {
        Item item = itemRepository.findById(request.getItemId())
                .orElseThrow(() -> new ResourceNotFoundException("Item not found with id: " + request.getItemId()));

        if (item.getUser().getId().equals(claimantId)) {
            throw new BadRequestException("You cannot submit a claim for an item you reported yourself");
        }

        if (item.getStatus() == ItemStatus.RECOVERED) {
            throw new BadRequestException("This item has already been recovered and closed");
        }

        if (claimRepository.existsByItemIdAndClaimantId(item.getId(), claimantId)) {
            throw new BadRequestException("You have already submitted a claim for this item");
        }

        User claimant = userRepository.findById(claimantId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + claimantId));

        try {
            Claim claim = new Claim(item, claimant, request.getProofDescription(), request.getContactInfo());
            Claim saved = claimRepository.save(claim);

            // Update item status to CLAIM_PENDING
            if (item.getStatus() != ItemStatus.CLAIM_PENDING) {
                item.setStatus(ItemStatus.CLAIM_PENDING);
                itemRepository.save(item);
            }

            // Notify item reporter about the new claim
            emailService.sendNewClaimReceivedEmail(item.getUser().getEmail(), item.getUser().getFullName(), item, saved);

            return ClaimResponse.fromEntity(saved);
        } catch (org.springframework.dao.DataIntegrityViolationException ex) {
            throw new BadRequestException("You have already submitted a claim for this item");
        }
    }

    @Transactional(readOnly = true)
    public List<ClaimResponse> getMyClaims(Long claimantId) {
        return claimRepository.findByClaimantIdOrderByCreatedAtDesc(claimantId).stream()
                .map(ClaimResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ClaimResponse> getClaimsForItem(Long itemId, Long userId, boolean isAdmin) {
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new ResourceNotFoundException("Item not found with id: " + itemId));

        if (!item.getUser().getId().equals(userId) && !isAdmin) {
            throw new AccessDeniedException("You do not have permission to view claims for this item");
        }

        return claimRepository.findByItemIdOrderByCreatedAtDesc(itemId).stream()
                .map(ClaimResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ClaimResponse> getClaimsReceived(Long ownerId) {
        return claimRepository.findByItemUserIdOrderByCreatedAtDesc(ownerId).stream()
                .map(ClaimResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional
    public ClaimResponse updateClaimStatus(Long claimId, ClaimStatus newStatus, String remarks, Long userId, boolean isAdmin) {
        if (newStatus != ClaimStatus.APPROVED && newStatus != ClaimStatus.REJECTED) {
            throw new BadRequestException("Invalid status update: Claim can only be APPROVED or REJECTED");
        }

        Claim claim = claimRepository.findById(claimId)
                .orElseThrow(() -> new ResourceNotFoundException("Claim not found with id: " + claimId));

        if (claim.getStatus() != ClaimStatus.PENDING) {
            throw new BadRequestException("Claim cannot be updated. Only PENDING claims can be reviewed, but this claim is already " + claim.getStatus());
        }

        // Lock item with pessimistic write lock to prevent race conditions during concurrent approval
        Item item = itemRepository.findByIdWithLock(claim.getItem().getId())
                .orElseThrow(() -> new ResourceNotFoundException("Item not found with id: " + claim.getItem().getId()));

        if (!item.getUser().getId().equals(userId) && !isAdmin) {
            throw new AccessDeniedException("Only the item reporter or an admin can review this claim");
        }

        if (newStatus == ClaimStatus.APPROVED && item.getStatus() == ItemStatus.RECOVERED) {
            throw new BadRequestException("This item has already been marked as RECOVERED and closed");
        }

        claim.setStatus(newStatus);
        claim.setRemarks(remarks);
        Claim updated = claimRepository.save(claim);

        if (newStatus == ClaimStatus.APPROVED) {
            item.setStatus(ItemStatus.RECOVERED);
            itemRepository.save(item);

            // Notify verified claimant
            emailService.sendClaimApprovedEmail(claim.getClaimant().getEmail(), claim.getClaimant().getFullName(), item, remarks);

            // Automatically reject other pending claims for this item
            List<Claim> otherClaims = claimRepository.findByItemIdOrderByCreatedAtDesc(item.getId());
            for (Claim other : otherClaims) {
                if (!other.getId().equals(claim.getId()) && other.getStatus() == ClaimStatus.PENDING) {
                    other.setStatus(ClaimStatus.REJECTED);
                    other.setRemarks("Item was recovered by another verified claimant");
                    claimRepository.save(other);

                    // Notify claimant of competing rejected claim
                    emailService.sendClaimRejectedEmail(other.getClaimant().getEmail(), other.getClaimant().getFullName(), item, other.getRemarks());
                }
            }
        } else if (newStatus == ClaimStatus.REJECTED) {
            // Notify claimant of rejection
            emailService.sendClaimRejectedEmail(claim.getClaimant().getEmail(), claim.getClaimant().getFullName(), item, remarks);

            // Check if there are remaining pending claims
            List<Claim> allClaims = claimRepository.findByItemIdOrderByCreatedAtDesc(item.getId());
            boolean hasPending = allClaims.stream().anyMatch(c -> c.getStatus() == ClaimStatus.PENDING);
            if (!hasPending && item.getStatus() == ItemStatus.CLAIM_PENDING) {
                item.setStatus(item.getType() == com.lostfound.entities.enums.ItemType.LOST ? ItemStatus.LOST : ItemStatus.FOUND);
                itemRepository.save(item);
            }
        }

        return ClaimResponse.fromEntity(updated);
    }
}
