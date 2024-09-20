package org.myexample.spinningmotion.persistence;

import org.myexample.spinningmotion.persistence.entity.ReviewEntity;
import java.util.List;
import java.util.Optional;

public interface ReviewRepository {
    ReviewEntity save(ReviewEntity review);
    Optional<ReviewEntity> findById(Long id);
    List<ReviewEntity> findAllByRecordId(Long recordId);
    void deleteById(Long id);
    Optional<ReviewEntity> findByUserIdAndRecordId(Long userId, Long recordId);

}