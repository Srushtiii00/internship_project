package com.lostfound.repositories;

import com.lostfound.entities.Item;
import com.lostfound.entities.enums.ItemStatus;
import com.lostfound.entities.enums.ItemType;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ItemRepository extends JpaRepository<Item, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT i FROM Item i WHERE i.id = :id")
    Optional<Item> findByIdWithLock(@Param("id") Long id);

    List<Item> findByUserIdOrderByCreatedAtDesc(Long userId);

    @Query("SELECT i FROM Item i " +
           "WHERE (:type IS NULL OR i.type = :type) " +
           "AND (:category IS NULL OR LOWER(i.category) = LOWER(:category)) " +
           "AND (:status IS NULL OR i.status = :status) " +
           "AND (:keyword IS NULL OR LOWER(i.title) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "     OR LOWER(i.description) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "     OR LOWER(i.location) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<Item> findFilteredItems(
            @Param("type") ItemType type,
            @Param("category") String category,
            @Param("status") ItemStatus status,
            @Param("keyword") String keyword,
            Pageable pageable
    );

    long countByType(ItemType type);
    long countByStatus(ItemStatus status);

    @Query("SELECT i FROM Item i " +
           "WHERE i.type != :type " +
           "AND i.status != com.lostfound.entities.enums.ItemStatus.RECOVERED " +
           "AND LOWER(i.category) = LOWER(:category) " +
           "AND i.id != :excludeId " +
           "ORDER BY i.createdAt DESC")
    List<Item> findPotentialMatches(
            @Param("type") ItemType type,
            @Param("category") String category,
            @Param("excludeId") Long excludeId,
            Pageable pageable
    );
}
