package com.lostfound;

import com.lostfound.dto.request.ClaimRequest;
import com.lostfound.dto.response.ClaimResponse;
import com.lostfound.entities.Claim;
import com.lostfound.entities.Item;
import com.lostfound.entities.User;
import com.lostfound.entities.enums.ClaimStatus;
import com.lostfound.entities.enums.ItemStatus;
import com.lostfound.entities.enums.ItemType;
import com.lostfound.entities.enums.Role;
import com.lostfound.exceptions.BadRequestException;
import com.lostfound.repositories.ClaimRepository;
import com.lostfound.repositories.ItemRepository;
import com.lostfound.repositories.UserRepository;
import com.lostfound.services.ClaimService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ClaimWorkflowTest {

    @Mock
    private ClaimRepository claimRepository;

    @Mock
    private ItemRepository itemRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private com.lostfound.services.EmailService emailService;

    @InjectMocks
    private ClaimService claimService;

    private User reporter;
    private User claimantA;
    private User claimantB;
    private Item foundItem;

    @BeforeEach
    void setUp() {
        reporter = new User("finder1", "finder@example.com", "pass", "Finder One", "1111111111", Role.ROLE_USER);
        reporter.setId(1L);

        claimantA = new User("claimantA", "a@example.com", "pass", "Claimant A", "2222222222", Role.ROLE_USER);
        claimantA.setId(2L);

        claimantB = new User("claimantB", "b@example.com", "pass", "Claimant B", "3333333333", Role.ROLE_USER);
        claimantB.setId(3L);

        foundItem = new Item(reporter, "Found Watch", "Silver wristwatch", "Electronics", ItemType.FOUND, ItemStatus.FOUND, "Library Floor 2", LocalDate.now(), null);
        foundItem.setId(50L);
    }

    @Test
    @DisplayName("Submit Claim: Self-claim prevention - reporter cannot claim their own item")
    void testSubmitClaim_SelfClaimForbidden() {
        when(itemRepository.findById(50L)).thenReturn(Optional.of(foundItem));

        ClaimRequest request = new ClaimRequest();
        request.setItemId(50L);
        request.setProofDescription("It is mine");
        request.setContactInfo("contact@example.com");

        BadRequestException ex = assertThrows(BadRequestException.class, () ->
                claimService.submitClaim(request, 1L)
        );
        assertTrue(ex.getMessage().contains("cannot submit a claim for an item you reported yourself"));
    }

    @Test
    @DisplayName("Submit Claim: Disallow claiming an item that is already RECOVERED")
    void testSubmitClaim_DisallowWhenRecovered() {
        foundItem.setStatus(ItemStatus.RECOVERED);
        when(itemRepository.findById(50L)).thenReturn(Optional.of(foundItem));

        ClaimRequest request = new ClaimRequest();
        request.setItemId(50L);

        BadRequestException ex = assertThrows(BadRequestException.class, () ->
                claimService.submitClaim(request, 2L)
        );
        assertTrue(ex.getMessage().contains("already been recovered"));
    }

    @Test
    @DisplayName("Submit Claim: Disallow duplicate claims from the same claimant")
    void testSubmitClaim_DisallowDuplicateClaim() {
        when(itemRepository.findById(50L)).thenReturn(Optional.of(foundItem));
        when(claimRepository.existsByItemIdAndClaimantId(50L, 2L)).thenReturn(true);

        ClaimRequest request = new ClaimRequest();
        request.setItemId(50L);

        BadRequestException ex = assertThrows(BadRequestException.class, () ->
                claimService.submitClaim(request, 2L)
        );
        assertTrue(ex.getMessage().contains("already submitted a claim for this item"));
    }

    @Test
    @DisplayName("Update Claim Status: Reject reviewing already resolved claims (transition integrity)")
    void testUpdateClaimStatus_DisallowReReviewingResolvedClaim() {
        Claim resolvedClaim = new Claim(foundItem, claimantA, "Proof A", "contactA");
        resolvedClaim.setId(101L);
        resolvedClaim.setStatus(ClaimStatus.APPROVED);

        when(claimRepository.findById(101L)).thenReturn(Optional.of(resolvedClaim));

        BadRequestException ex = assertThrows(BadRequestException.class, () ->
                claimService.updateClaimStatus(101L, ClaimStatus.REJECTED, "Rejected later", 1L, false)
        );
        assertTrue(ex.getMessage().contains("Only PENDING claims can be reviewed"));
    }

    @Test
    @DisplayName("Update Claim Status: Approving a claim sets item to RECOVERED and rejects other pending claims")
    void testUpdateClaimStatus_ApproveClaimAtomically() {
        Claim pendingClaimA = new Claim(foundItem, claimantA, "Proof A", "contactA");
        pendingClaimA.setId(101L);
        pendingClaimA.setStatus(ClaimStatus.PENDING);

        Claim pendingClaimB = new Claim(foundItem, claimantB, "Proof B", "contactB");
        pendingClaimB.setId(102L);
        pendingClaimB.setStatus(ClaimStatus.PENDING);

        when(claimRepository.findById(101L)).thenReturn(Optional.of(pendingClaimA));
        when(itemRepository.findByIdWithLock(50L)).thenReturn(Optional.of(foundItem));
        when(claimRepository.save(any(Claim.class))).thenAnswer(i -> i.getArgument(0));
        when(claimRepository.findByItemIdOrderByCreatedAtDesc(50L)).thenReturn(Arrays.asList(pendingClaimA, pendingClaimB));

        ClaimResponse response = claimService.updateClaimStatus(101L, ClaimStatus.APPROVED, "Verified ownership", 1L, false);

        assertNotNull(response);
        assertEquals(ClaimStatus.APPROVED, response.getStatus());
        assertEquals(ItemStatus.RECOVERED, foundItem.getStatus());
        assertEquals(ClaimStatus.REJECTED, pendingClaimB.getStatus());
        assertTrue(pendingClaimB.getRemarks().contains("recovered by another verified claimant"));

        verify(itemRepository).save(foundItem);
        verify(claimRepository).save(pendingClaimB);
    }
}
