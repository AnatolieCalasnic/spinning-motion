package org.myexample.spinningmotion.persistence.impl;

import org.myexample.spinningmotion.persistence.ReviewRepository;
import org.myexample.spinningmotion.persistence.entity.ReviewEntity;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
public class FakeReviewRepository implements ReviewRepository {
    private final Map<Long, ReviewEntity> reviews = new HashMap<>();
    private Long nextId = 1L;

    @Override
    public ReviewEntity save(ReviewEntity review) {
        if (review.getId() == null) {
            review.setId(nextId++);
        }
        reviews.put(review.getId(), review);
        return review;
    }

    @Override
    public Optional<ReviewEntity> findById(Long id) {
        return Optional.ofNullable(reviews.get(id));
    }

    @Override
    public List<ReviewEntity> findAllByRecordId(Long recordId) {
        return reviews.values().stream()
                .filter(r -> r.getRecordId().equals(recordId))
                .collect(Collectors.toList());
    }

    @Override
    public void deleteById(Long id) {
        reviews.remove(id);
    }
    @Override
    public Optional<ReviewEntity> findByUserIdAndRecordId(Long userId, Long recordId) {
        return reviews.values().stream()
                .filter(r -> r.getUserId().equals(userId) && r.getRecordId().equals(recordId))
                .findFirst();

    }
    //filters the reviews map for a ReviewEntity that matches both the userId and recordId and returns an Optional of the result

}
