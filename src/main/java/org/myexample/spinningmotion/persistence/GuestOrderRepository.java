package org.myexample.spinningmotion.persistence;

import org.myexample.spinningmotion.persistence.entity.GuestDetailsEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface GuestOrderRepository  extends JpaRepository<GuestDetailsEntity, Long> {
    @Query("SELECT g FROM GuestDetailsEntity g WHERE g.purchaseHistoryId = :purchaseHistoryId")
    GuestDetailsEntity findFirstByPurchaseHistoryId(Long purchaseHistoryId);
    List<GuestDetailsEntity> findAllByPurchaseHistoryId(Long purchaseHistoryId);
}
