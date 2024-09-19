package org.myexample.spinningmotion.persistence.impl;

import org.myexample.spinningmotion.persistence.BasketRepository;
import org.myexample.spinningmotion.persistence.entity.BasketEntity;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Repository
public class FakeBasketRepository implements BasketRepository {
    private final Map<Long, BasketEntity> baskets = new HashMap<>();

    @Override
    public BasketEntity save(BasketEntity basket) {
        baskets.put(basket.getUserId(), basket);
        return basket;
    }

    @Override
    public Optional<BasketEntity> findByUserId(Long userId) {
        return Optional.ofNullable(baskets.get(userId));
    }

    @Override
    public void deleteByUserId(Long userId) {
        baskets.remove(userId);
    }
}