package com.lostfound.repositories;

import com.lostfound.entities.Claim;
import com.lostfound.entities.enums.ClaimStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ClaimRepository extends JpaRepository<Claim, Long> {
    List<Claim> findByClaimantIdOrderByCreatedAtDesc(Long claimantId);
    List<Claim> findByItemIdOrderByCreatedAtDesc(Long itemId);
    List<Claim> findByItemUserIdOrderByCreatedAtDesc(Long ownerId);
    boolean existsByItemIdAndClaimantId(Long itemId, Long claimantId);
    long countByStatus(ClaimStatus status);
}
