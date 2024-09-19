package org.myexample.spinningmotion.persistence.impl;

import org.myexample.spinningmotion.persistence.PurchaseHistoryRepository;
import org.myexample.spinningmotion.persistence.entity.PurchaseHistoryEntity;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
public class FakePurchaseHistoryRepository implements PurchaseHistoryRepository {
    private final Map<Long, PurchaseHistoryEntity> purchaseHistories = new HashMap<>();
    private Long nextId = 1L;

    @Override
    public PurchaseHistoryEntity save(PurchaseHistoryEntity purchaseHistory) {
        if (purchaseHistory.getId() == null) {
            purchaseHistory.setId(nextId++);
        }
        purchaseHistories.put(purchaseHistory.getId(), purchaseHistory);
        return purchaseHistory;
    }

    @Override
    public Optional<PurchaseHistoryEntity> findById(Long id) {
        return Optional.ofNullable(purchaseHistories.get(id));
    }

    @Override
    public List<PurchaseHistoryEntity> findAllByUserId(Long userId) {
        return purchaseHistories.values().stream()
                .filter(ph -> ph.getUserId().equals(userId))
                .collect(Collectors.toList());
    }

    @Override
    public void deleteById(Long id) {
        purchaseHistories.remove(id);
    }
}