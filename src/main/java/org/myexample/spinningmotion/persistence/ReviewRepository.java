package org.myexample.spinningmotion.persistence;

import org.myexample.spinningmotion.persistence.entity.ReviewEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
@Repository
public interface ReviewRepository extends JpaRepository<ReviewEntity, Long> {
//    ReviewEntity save(ReviewEntity review);
//    Optional<ReviewEntity> findById(Long id);
    List<ReviewEntity> findAllByRecordId(Long recordId);
//    void deleteById(Long id);
    Optional<ReviewEntity> findByUserIdAndRecordId(Long userId, Long recordId);

}