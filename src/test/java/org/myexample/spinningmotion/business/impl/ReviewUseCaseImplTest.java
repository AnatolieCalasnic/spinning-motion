package org.myexample.spinningmotion.business.impl;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.InjectMocks;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.junit.jupiter.MockitoExtension;
import org.myexample.spinningmotion.business.exception.ReviewNotFoundException;
import org.myexample.spinningmotion.business.impl.review.ReviewUseCaseImpl;
import org.myexample.spinningmotion.domain.review.*;
import org.myexample.spinningmotion.persistence.RecordRepository;
import org.myexample.spinningmotion.persistence.ReviewRepository;
import org.myexample.spinningmotion.persistence.UserRepository;
import org.myexample.spinningmotion.persistence.entity.RecordEntity;
import org.myexample.spinningmotion.persistence.entity.ReviewEntity;
import org.myexample.spinningmotion.persistence.entity.UserEntity;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
@ExtendWith(MockitoExtension.class)
class ReviewUseCaseImplTest {

    @Mock
    private ReviewRepository reviewRepository;
    @Mock
    private UserRepository userRepository;

    @Mock
    private RecordRepository recordRepository;
    @InjectMocks
    private ReviewUseCaseImpl reviewUseCase;

    private CreateReviewRequest createReviewRequest;
    private ReviewEntity reviewEntity;

    @BeforeEach
    void setUp() {

        createReviewRequest = CreateReviewRequest.builder()
                .userId(1L)
                .recordId(1L)
                .rating(5)
                .comment("Great album!")
                .build();

        reviewEntity = ReviewEntity.builder()
                .id(1L)
                .user(UserEntity.builder().id(1L).build())
                .record(RecordEntity.builder().id(1L).build())
                .rating(5)
                .comment("Great album!")
                .createdAt(LocalDateTime.now())
                .build();


    }

    @Test
    void createReview_Success() {
        UserEntity userEntity = UserEntity.builder()
                .id(createReviewRequest.getUserId())
                .fname("User Name")
                .build();

        RecordEntity recordEntity = RecordEntity.builder()
                .id(createReviewRequest.getRecordId())
                .title("Some Record Title")
                .build();

        // Stub repository methods
        when(userRepository.findById(createReviewRequest.getUserId())).thenReturn(Optional.of(userEntity));
        when(recordRepository.findById(createReviewRequest.getRecordId())).thenReturn(Optional.of(recordEntity));
        when(reviewRepository.findAllByRecordId(createReviewRequest.getRecordId())).thenReturn(Arrays.asList());
        when(reviewRepository.save(any(ReviewEntity.class))).thenReturn(reviewEntity);

        // Execute and verify the response
        CreateReviewResponse response = reviewUseCase.createReview(createReviewRequest);

        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals(createReviewRequest.getUserId(), response.getUserId());
        assertEquals(createReviewRequest.getRecordId(), response.getRecordId());
        assertEquals(createReviewRequest.getRating(), response.getRating());

        verify(reviewRepository).findAllByRecordId(createReviewRequest.getRecordId());
        verify(reviewRepository).save(any(ReviewEntity.class));
    }


    @Test
    void getReview_Success() {
        when(reviewRepository.findById(1L)).thenReturn(Optional.of(reviewEntity));

        GetReviewResponse response = reviewUseCase.getReview(new GetReviewRequest(1L));

        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals(reviewEntity.getUser().getId(), response.getUserId());
        assertEquals(reviewEntity.getRecord().getId(), response.getRecordId());
        assertEquals(reviewEntity.getRating(), response.getRating());

        verify(reviewRepository).findById(1L);
    }

    @Test
    void getReview_ReviewNotFound() {
        GetReviewRequest request = new GetReviewRequest(1L);

        when(reviewRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ReviewNotFoundException.class, () -> { reviewUseCase.getReview(request);
        });

        verify(reviewRepository).findById(1L);
    }

    @Test
    void getAllReviewsByRecordId_Success() {
        when(reviewRepository.findAllByRecordId(1L)).thenReturn(Arrays.asList(reviewEntity));

        List<GetReviewResponse> responses = reviewUseCase.getAllReviewsByRecordId(1L);

        assertNotNull(responses);
        assertEquals(1, responses.size());
        assertEquals(reviewEntity.getId(), responses.get(0).getId());

        verify(reviewRepository).findAllByRecordId(1L);
    }

    @Test
    void updateReview_Success() {
        UpdateReviewRequest updateRequest = UpdateReviewRequest.builder()
                .id(1L)
                .rating(4)
                .comment("Good album!")
                .build();

        when(reviewRepository.findById(1L)).thenReturn(Optional.of(reviewEntity));
        when(reviewRepository.save(any(ReviewEntity.class))).thenReturn(reviewEntity);

        UpdateReviewResponse response = reviewUseCase.updateReview(updateRequest);

        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals(updateRequest.getRating(), response.getRating());
        assertEquals(updateRequest.getComment(), response.getComment());

        verify(reviewRepository).findById(1L);
        verify(reviewRepository).save(any(ReviewEntity.class));
    }

    @Test
    void updateReview_ReviewNotFound() {
        UpdateReviewRequest updateRequest = UpdateReviewRequest.builder().id(1L).build();
        when(reviewRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ReviewNotFoundException.class, () -> reviewUseCase.updateReview(updateRequest));

        verify(reviewRepository).findById(1L);
        verify(reviewRepository, never()).save(any(ReviewEntity.class));
    }

    @Test
    void deleteReview_Success() {
        when(reviewRepository.findById(1L)).thenReturn(Optional.of(reviewEntity));

        assertDoesNotThrow(() -> reviewUseCase.deleteReview(1L));

        verify(reviewRepository).findById(1L);
        verify(reviewRepository).deleteById(1L);
    }

    @Test
    void deleteReview_ReviewNotFound() {
        when(reviewRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ReviewNotFoundException.class, () -> reviewUseCase.deleteReview(1L));

        verify(reviewRepository).findById(1L);
        verify(reviewRepository, never()).deleteById(anyLong());
    }
}