package com.lostfound.services;

import com.lostfound.dto.request.ItemRequest;
import com.lostfound.dto.response.ItemResponse;
import com.lostfound.entities.Item;
import com.lostfound.entities.User;
import com.lostfound.entities.enums.ItemStatus;
import com.lostfound.entities.enums.ItemType;
import com.lostfound.exceptions.AccessDeniedException;
import com.lostfound.exceptions.BadRequestException;
import com.lostfound.exceptions.ResourceNotFoundException;
import com.lostfound.repositories.ItemRepository;
import com.lostfound.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ItemService {

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private FileStorageService fileStorageService;

    @Autowired
    private EmailService emailService;

    @Transactional
    public ItemResponse createItem(ItemRequest request, MultipartFile image, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        String imageUrl = null;
        if (image != null && !image.isEmpty()) {
            imageUrl = fileStorageService.storeFile(image);
        }

        ItemStatus initialStatus = (request.getType() == ItemType.LOST) ? ItemStatus.LOST : ItemStatus.FOUND;

        Item item = new Item(
                user,
                request.getTitle(),
                request.getDescription(),
                request.getCategory(),
                request.getType(),
                initialStatus,
                request.getLocation(),
                request.getItemDate(),
                imageUrl
        );

        Item saved = itemRepository.save(item);

        // Send confirmation email to reporter
        emailService.sendItemReportedEmail(user.getEmail(), user.getFullName(), saved);

        // Scan for potential matches of opposite type in the same category
        try {
            List<Item> matches = itemRepository.findPotentialMatches(
                    saved.getType(),
                    saved.getCategory(),
                    saved.getId(),
                    PageRequest.of(0, 5)
            );
            if (!matches.isEmpty()) {
                emailService.sendPotentialMatchAlertEmail(user.getEmail(), user.getFullName(), saved, matches);
            }
        } catch (Exception ignored) {
        }

        return ItemResponse.fromEntity(saved);
    }

    @Transactional
    public ItemResponse updateItem(Long itemId, ItemRequest request, MultipartFile image, Long userId, boolean isAdmin) {
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new ResourceNotFoundException("Item not found with id: " + itemId));

        if (!item.getUser().getId().equals(userId) && !isAdmin) {
            throw new AccessDeniedException("You do not have permission to edit this item");
        }

        item.setTitle(request.getTitle());
        item.setDescription(request.getDescription());
        item.setCategory(request.getCategory());
        item.setType(request.getType());
        item.setLocation(request.getLocation());
        item.setItemDate(request.getItemDate());

        if (image != null && !image.isEmpty()) {
            if (item.getImageUrl() != null) {
                fileStorageService.deleteFile(item.getImageUrl());
            }
            item.setImageUrl(fileStorageService.storeFile(image));
        }

        Item updated = itemRepository.save(item);
        return ItemResponse.fromEntity(updated);
    }

    @Transactional
    public void deleteItem(Long itemId, Long userId, boolean isAdmin) {
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new ResourceNotFoundException("Item not found with id: " + itemId));

        if (!item.getUser().getId().equals(userId) && !isAdmin) {
            throw new AccessDeniedException("You do not have permission to delete this item");
        }

        if (item.getImageUrl() != null) {
            fileStorageService.deleteFile(item.getImageUrl());
        }

        itemRepository.delete(item);
    }

    @Transactional
    public ItemResponse updateItemStatus(Long itemId, ItemStatus newStatus, Long userId, boolean isAdmin) {
        if (newStatus == null) {
            throw new BadRequestException("Status cannot be null");
        }

        if (newStatus == ItemStatus.CLAIM_PENDING) {
            throw new BadRequestException("Status cannot be set to CLAIM_PENDING directly. It is managed via the claim verification workflow.");
        }

        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new ResourceNotFoundException("Item not found with id: " + itemId));

        if (!item.getUser().getId().equals(userId) && !isAdmin) {
            throw new AccessDeniedException("You do not have permission to change the status of this item");
        }

        if (item.getType() == ItemType.LOST && newStatus == ItemStatus.FOUND) {
            throw new BadRequestException("A LOST item cannot be updated to status FOUND.");
        }
        if (item.getType() == ItemType.FOUND && newStatus == ItemStatus.LOST) {
            throw new BadRequestException("A FOUND item cannot be updated to status LOST.");
        }

        item.setStatus(newStatus);
        Item updated = itemRepository.save(item);

        if (newStatus == ItemStatus.RECOVERED) {
            emailService.sendItemRecoveredEmail(item.getUser().getEmail(), item.getUser().getFullName(), updated);
        }

        return ItemResponse.fromEntity(updated);
    }

    @Transactional(readOnly = true)
    public ItemResponse getItemById(Long itemId) {
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new ResourceNotFoundException("Item not found with id: " + itemId));
        return ItemResponse.fromEntity(item);
    }

    @Transactional(readOnly = true)
    public Page<ItemResponse> getFilteredItems(ItemType type, String category, ItemStatus status, String keyword, int page, int size) {
        int sanitizedPage = Math.max(0, page);
        int sanitizedSize = Math.max(1, Math.min(size, 50));
        Pageable pageable = PageRequest.of(sanitizedPage, sanitizedSize, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Item> itemPage = itemRepository.findFilteredItems(
                type,
                (category != null && !category.trim().isEmpty()) ? category.trim() : null,
                status,
                (keyword != null && !keyword.trim().isEmpty()) ? keyword.trim() : null,
                pageable
        );
        return itemPage.map(ItemResponse::fromEntity);
    }

    @Transactional(readOnly = true)
    public List<ItemResponse> getMyItems(Long userId) {
        return itemRepository.findByUserIdOrderByCreatedAtDesc(userId).stream()
                .map(ItemResponse::fromEntity)
                .collect(Collectors.toList());
    }
}
