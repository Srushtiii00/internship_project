package com.lostfound;

import com.lostfound.dto.response.ItemResponse;
import com.lostfound.entities.Item;
import com.lostfound.entities.User;
import com.lostfound.entities.enums.ItemStatus;
import com.lostfound.entities.enums.ItemType;
import com.lostfound.entities.enums.Role;
import com.lostfound.exceptions.AccessDeniedException;
import com.lostfound.exceptions.BadRequestException;
import com.lostfound.repositories.ItemRepository;
import com.lostfound.repositories.UserRepository;
import com.lostfound.services.FileStorageService;
import com.lostfound.services.ItemService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ItemServiceTest {

    @Mock
    private ItemRepository itemRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private FileStorageService fileStorageService;

    @Mock
    private com.lostfound.services.EmailService emailService;

    @InjectMocks
    private ItemService itemService;

    private User owner;
    private Item lostItem;
    private Item foundItem;

    @BeforeEach
    void setUp() {
        owner = new User("johndoe", "john@example.com", "encodedPass", "John Doe", "9876543210", Role.ROLE_USER);
        owner.setId(10L);

        lostItem = new Item(owner, "Lost Keys", "Keychain with 3 keys", "Keys", ItemType.LOST, ItemStatus.LOST, "Library", LocalDate.now(), null);
        lostItem.setId(100L);

        foundItem = new Item(owner, "Found Wallet", "Brown leather wallet", "Wallets & Bags", ItemType.FOUND, ItemStatus.FOUND, "Cafeteria", LocalDate.now(), null);
        foundItem.setId(200L);
    }

    @Test
    @DisplayName("Status update: Disallow setting status to CLAIM_PENDING directly via ItemService")
    void testUpdateStatus_DisallowDirectClaimPending() {
        BadRequestException exception = assertThrows(BadRequestException.class, () ->
                itemService.updateItemStatus(100L, ItemStatus.CLAIM_PENDING, 10L, false)
        );
        assertTrue(exception.getMessage().contains("CLAIM_PENDING directly"));
    }

    @Test
    @DisplayName("Status update: Disallow LOST item transitioning to status FOUND")
    void testUpdateStatus_DisallowLostToFoundMismatch() {
        when(itemRepository.findById(100L)).thenReturn(Optional.of(lostItem));

        BadRequestException exception = assertThrows(BadRequestException.class, () ->
                itemService.updateItemStatus(100L, ItemStatus.FOUND, 10L, false)
        );
        assertTrue(exception.getMessage().contains("A LOST item cannot be updated to status FOUND"));
    }

    @Test
    @DisplayName("Status update: Disallow FOUND item transitioning to status LOST")
    void testUpdateStatus_DisallowFoundToLostMismatch() {
        when(itemRepository.findById(200L)).thenReturn(Optional.of(foundItem));

        BadRequestException exception = assertThrows(BadRequestException.class, () ->
                itemService.updateItemStatus(200L, ItemStatus.LOST, 10L, false)
        );
        assertTrue(exception.getMessage().contains("A FOUND item cannot be updated to status LOST"));
    }

    @Test
    @DisplayName("Status update: Non-owner non-admin user cannot change item status")
    void testUpdateStatus_NonOwnerAccessDenied() {
        when(itemRepository.findById(100L)).thenReturn(Optional.of(lostItem));

        assertThrows(AccessDeniedException.class, () ->
                itemService.updateItemStatus(100L, ItemStatus.RECOVERED, 999L, false)
        );
    }

    @Test
    @DisplayName("Status update: Owner can successfully mark item as RECOVERED")
    void testUpdateStatus_OwnerCanMarkRecovered() {
        when(itemRepository.findById(100L)).thenReturn(Optional.of(lostItem));
        when(itemRepository.save(any(Item.class))).thenAnswer(i -> i.getArgument(0));

        ItemResponse response = itemService.updateItemStatus(100L, ItemStatus.RECOVERED, 10L, false);
        assertNotNull(response);
        assertEquals(ItemStatus.RECOVERED, response.getStatus());
        verify(itemRepository, times(1)).save(lostItem);
    }

    @Test
    @DisplayName("Pagination: Sanitize negative page index and enforce size bounds (max 50)")
    void testGetFilteredItems_SanitizePaginationBounds() {
        Page<Item> mockPage = new PageImpl<>(Collections.singletonList(lostItem));
        when(itemRepository.findFilteredItems(any(), any(), any(), any(), any(Pageable.class))).thenReturn(mockPage);

        // Pass negative page (-5) and excessive size (250)
        itemService.getFilteredItems(ItemType.LOST, "Keys", ItemStatus.LOST, "Keys", -5, 250);

        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        verify(itemRepository).findFilteredItems(eq(ItemType.LOST), eq("Keys"), eq(ItemStatus.LOST), eq("Keys"), pageableCaptor.capture());

        Pageable usedPageable = pageableCaptor.getValue();
        assertEquals(0, usedPageable.getPageNumber(), "Negative page should be clamped to 0");
        assertEquals(50, usedPageable.getPageSize(), "Page size should be clamped to maximum 50");
    }

    @Test
    @DisplayName("Privacy: ItemResponse must use PublicUserSummaryResponse and not leak reporter email or phone")
    void testItemResponse_PrivacySanitization() {
        ItemResponse response = ItemResponse.fromEntity(lostItem);
        assertNotNull(response.getReporter());
        assertEquals(10L, response.getReporter().getId());
        assertEquals("johndoe", response.getReporter().getUsername());
        assertEquals("John Doe", response.getReporter().getFullName());
    }
}
