package org.myexample.spinningmotion.business.interfc;

import org.myexample.spinningmotion.domain.review.*;

import java.util.List;

public interface ReviewUseCase {
    CreateReviewResponse createReview(CreateReviewRequest request);
    GetReviewResponse getReview(GetReviewRequest request);
    List<GetReviewResponse> getAllReviewsByRecordId(Long recordId);
    UpdateReviewResponse updateReview(UpdateReviewRequest request);
    void deleteReview(Long id);
}