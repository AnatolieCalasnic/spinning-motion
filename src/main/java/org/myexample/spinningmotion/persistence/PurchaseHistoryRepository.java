package org.myexample.spinningmotion.persistence;

import org.myexample.spinningmotion.persistence.entity.PurchaseHistoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
@Repository
public interface PurchaseHistoryRepository extends JpaRepository<PurchaseHistoryEntity, Long> {
    PurchaseHistoryEntity save(PurchaseHistoryEntity purchaseHistory);
    Optional<PurchaseHistoryEntity> findById(Long id);
    List<PurchaseHistoryEntity> findAllByUserId(Long userId);
    void deleteById(Long id);
    List<PurchaseHistoryEntity> findTop10ByOrderByPurchaseDateDesc();

    @Query("SELECT SUM(ph.totalAmount) FROM PurchaseHistoryEntity ph")
    Double calculateTotalRevenue();

    @Query("SELECT COUNT(DISTINCT ph.userId) FROM PurchaseHistoryEntity ph")
    Long countUniqueUsers();
}
