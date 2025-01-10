package org.myexample.spinningmotion.persistence;

import org.myexample.spinningmotion.persistence.entity.CouponEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface CouponRepository extends JpaRepository<CouponEntity, Long> {
    Optional<CouponEntity> findByCouponCode(String couponCode);
    List<CouponEntity> findByUserId(Long userId);
    boolean existsByUserIdAndIsUsedFalseAndValidUntilAfter(Long userId, LocalDateTime now);
    @Query("SELECT c FROM CouponEntity c WHERE c.userId = :userId AND c.validUntil > :date AND c.isUsed = false")
    List<CouponEntity> findValidCouponsForUser(@Param("userId") Long userId, @Param("date") LocalDateTime date);
}