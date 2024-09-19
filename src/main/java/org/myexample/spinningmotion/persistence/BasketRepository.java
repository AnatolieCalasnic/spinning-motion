package org.myexample.spinningmotion.persistence;

import org.myexample.spinningmotion.persistence.entity.BasketEntity;
import java.util.Optional;

public interface BasketRepository {
    BasketEntity save(BasketEntity basket);
    Optional<BasketEntity> findByUserId(Long userId);
    void deleteByUserId(Long userId);
}