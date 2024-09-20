package org.myexample.spinningmotion.persistence;

import org.myexample.spinningmotion.persistence.entity.PurchaseHistoryEntity;
import java.util.List;
import java.util.Optional;

public interface PurchaseHistoryRepository {
    PurchaseHistoryEntity save(PurchaseHistoryEntity purchaseHistory);
    Optional<PurchaseHistoryEntity> findById(Long id);
    List<PurchaseHistoryEntity> findAllByUserId(Long userId);
    void deleteById(Long id);

}
