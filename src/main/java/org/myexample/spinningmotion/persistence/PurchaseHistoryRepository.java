package org.myexample.spinningmotion.persistence;

import org.myexample.spinningmotion.persistence.entity.PurchaseHistoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
@Repository
public interface PurchaseHistoryRepository extends JpaRepository<PurchaseHistoryEntity, Long> {
    PurchaseHistoryEntity save(PurchaseHistoryEntity purchaseHistory);
    Optional<PurchaseHistoryEntity> findById(Long id);
    List<PurchaseHistoryEntity> findAllByUserId(Long userId);
    void deleteById(Long id);
    List<PurchaseHistoryEntity> findTop10ByOrderByPurchaseDateDesc();
    List<PurchaseHistoryEntity> findAllByUserIdAndPurchaseDate(Long userId, LocalDateTime purchaseDate);


    @Query("SELECT SUM(ph.totalAmount) FROM PurchaseHistoryEntity ph")
    Double calculateTotalRevenue();

    @Query("SELECT COUNT(DISTINCT ph.userId) FROM PurchaseHistoryEntity ph")
    Long countUniqueUsers();

    @Query("SELECT ph FROM PurchaseHistoryEntity ph " +
            "WHERE CAST(ph.userId AS string) LIKE CONCAT('%', :searchTerm, '%') " +
            "OR CAST(ph.recordId AS string) LIKE CONCAT('%', :searchTerm, '%') " +
            "OR LOWER(ph.status) LIKE LOWER(CONCAT('%', :searchTerm, '%')) " +
            "OR CAST(ph.totalAmount AS string) LIKE CONCAT('%', :searchTerm, '%')")
    List<PurchaseHistoryEntity> searchOrders(@Param("searchTerm") String searchTerm);
}

