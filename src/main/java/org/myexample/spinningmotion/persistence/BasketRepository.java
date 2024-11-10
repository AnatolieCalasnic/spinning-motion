package org.myexample.spinningmotion.persistence;

import org.myexample.spinningmotion.persistence.entity.BasketEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
@Repository
public interface BasketRepository extends JpaRepository<BasketEntity, Long> {
    BasketEntity save(BasketEntity basket);
    Optional<BasketEntity> findByUserId(Long userId);
    void deleteByUserId(Long userId);
}