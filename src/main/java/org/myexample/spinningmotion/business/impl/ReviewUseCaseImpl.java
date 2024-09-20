package org.myexample.spinningmotion.business.impl;

import lombok.RequiredArgsConstructor;
import org.myexample.spinningmotion.business.exception.DuplicateReviewException;
import org.myexample.spinningmotion.business.exception.ReviewNotFoundException;
import org.myexample.spinningmotion.business.interfc.ReviewUseCase;
import org.myexample.spinningmotion.domain.review.*;
import org.myexample.spinningmotion.persistence.ReviewRepository;
import org.myexample.spinningmotion.persistence.entity.ReviewEntity;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReviewUseCaseImpl implements ReviewUseCase {
    private final ReviewRepository reviewRepository;

    @Override
    public CreateReviewResponse createReview(CreateReviewRequest request) {
        List<ReviewEntity> existingReviews = reviewRepository.findAllByRecordId(request.getRecordId());
        boolean reviewExists = existingReviews.stream()
                .anyMatch(review -> review.getUserId().equals(request.getUserId()));

        if (reviewExists) {
            throw new DuplicateReviewException();
        }
        ReviewEntity entity = convertToEntity(request);
        ReviewEntity savedEntity = reviewRepository.save(entity);
        return convertToCreateResponse(savedEntity);
    }

    @Override
    public GetReviewResponse getReview(GetReviewRequest request) {
        ReviewEntity entity = reviewRepository.findById(request.getId())
                .orElseThrow(() -> new ReviewNotFoundException("Review not found with id: " + request.getId()));
        return convertToGetResponse(entity);
    }

    @Override
    public List<GetReviewResponse> getAllReviewsByRecordId(Long recordId) {
        List<ReviewEntity> entities = reviewRepository.findAllByRecordId(recordId);
        return entities.stream()
                .map(this::convertToGetResponse)
                .collect(Collectors.toList());
    }

    @Override
    public UpdateReviewResponse updateReview(UpdateReviewRequest request) {
        ReviewEntity entity = reviewRepository.findById(request.getId())
                .orElseThrow(() -> new ReviewNotFoundException("Review not found with id: " + request.getId()));

        updateEntityFromRequest(entity, request);
        ReviewEntity updatedEntity = reviewRepository.save(entity);
        return convertToUpdateResponse(updatedEntity);
    }

    @Override
    public void deleteReview(Long id) {
        if (!reviewRepository.findById(id).isPresent()) {
            throw new ReviewNotFoundException("Review not found with id: " + id);
        }
        reviewRepository.deleteById(id);
    }

    private ReviewEntity convertToEntity(CreateReviewRequest request) {
        return ReviewEntity.builder()
                .userId(request.getUserId())
                .recordId(request.getRecordId())
                .rating(request.getRating())
                .comment(request.getComment())
                .createdAt(LocalDateTime.now())
                .build();
    }

    private CreateReviewResponse convertToCreateResponse(ReviewEntity entity) {
        return CreateReviewResponse.builder()
                .id(entity.getId())
                .userId(entity.getUserId())
                .recordId(entity.getRecordId())
                .rating(entity.getRating())
                .comment(entity.getComment())
                .createdAt(entity.getCreatedAt())
                .build();
    }

    private GetReviewResponse convertToGetResponse(ReviewEntity entity) {
        return GetReviewResponse.builder()
                .id(entity.getId())
                .userId(entity.getUserId())
                .recordId(entity.getRecordId())
                .rating(entity.getRating())
                .comment(entity.getComment())
                .createdAt(entity.getCreatedAt())
                .build();
    }

    private UpdateReviewResponse convertToUpdateResponse(ReviewEntity entity) {
        return UpdateReviewResponse.builder()
                .id(entity.getId())
                .rating(entity.getRating())
                .comment(entity.getComment())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    private void updateEntityFromRequest(ReviewEntity entity, UpdateReviewRequest request) {
        entity.setRating(request.getRating());
        entity.setComment(request.getComment());
        // no update for userId, recordId, or createdAt as these shouldn't change
    }
}
