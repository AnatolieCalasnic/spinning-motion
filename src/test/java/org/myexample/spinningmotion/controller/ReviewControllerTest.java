package org.myexample.spinningmotion.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.junit.jupiter.api.BeforeEach;
import org.myexample.spinningmotion.business.exception.DuplicateReviewException;
import org.myexample.spinningmotion.business.exception.ReviewNotFoundException;
import org.myexample.spinningmotion.business.interfc.ReviewUseCase;
import org.myexample.spinningmotion.domain.review.*;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class ReviewControllerTest {
    @Mock
    private ReviewUseCase reviewUseCase;

    @InjectMocks
    private ReviewController controller;

    private CreateReviewRequest createReviewRequest;
    private CreateReviewResponse createReviewResponse;
    private GetReviewResponse getReviewResponse;
    private UpdateReviewRequest updateReviewRequest;
    private UpdateReviewResponse updateReviewResponse;
    private static final String DUPLICATE_REVIEW_MESSAGE = "You've already reviewed this record. You can edit your existing review instead.";


    @BeforeEach
    void setUp() {
        createReviewRequest = new CreateReviewRequest(1L, 1L, 5, "Great record!");

        createReviewResponse = CreateReviewResponse.builder()
                .id(1L)
                .userId(1L)
                .recordId(1L)
                .rating(5)
                .comment("Great record!")
                .createdAt(LocalDateTime.now())
                .build();

        getReviewResponse = GetReviewResponse.builder()
                .id(1L)
                .userId(1L)
                .recordId(1L)
                .rating(5)
                .comment("Great record!")
                .createdAt(LocalDateTime.now())
                .build();

        updateReviewRequest = new UpdateReviewRequest(1L, 4, "Updated review");

        updateReviewResponse = UpdateReviewResponse.builder()
                .id(1L)
                .rating(4)
                .comment("Updated review")
                .build();
    }

    @Test
    void createReview_Success() {
        when(reviewUseCase.createReview(any(CreateReviewRequest.class))).thenReturn(createReviewResponse);
        ResponseEntity<?> response = controller.createReview(createReviewRequest);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(createReviewResponse, response.getBody());
        verify(reviewUseCase).createReview(createReviewRequest);
    }

    @Test
    void createReview_DuplicateReview() {
        when(reviewUseCase.createReview(any(CreateReviewRequest.class)))
                .thenThrow(new DuplicateReviewException());
        ResponseEntity<?> response = controller.createReview(createReviewRequest);
        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertEquals(DUPLICATE_REVIEW_MESSAGE, response.getBody());
        verify(reviewUseCase).createReview(createReviewRequest);
    }

    @Test
    void getReview_Success() {
        when(reviewUseCase.getReview(any(GetReviewRequest.class))).thenReturn(getReviewResponse);
        ResponseEntity<?> response = controller.getReview(1L);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(getReviewResponse, response.getBody());
        verify(reviewUseCase).getReview(any(GetReviewRequest.class));
    }

    @Test
    void getReview_NotFound() {
        String errorMessage = "Review not found with id: 1";
        when(reviewUseCase.getReview(any(GetReviewRequest.class)))
                .thenThrow(new ReviewNotFoundException(errorMessage));
        ResponseEntity<?> response = controller.getReview(1L);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        @SuppressWarnings("unchecked")
        Map<String, String> errorBody = (Map<String, String>) response.getBody();
        assertEquals("Review Not Found", errorBody.get("error"));
        assertEquals(errorMessage, errorBody.get("message"));
        verify(reviewUseCase).getReview(any(GetReviewRequest.class));
    }


    @Test
    void getAllReviewsByRecordId_Success() {
        List<GetReviewResponse> reviews = Arrays.asList(getReviewResponse);
        when(reviewUseCase.getAllReviewsByRecordId(1L)).thenReturn(reviews);
        ResponseEntity<?> response = controller.getAllReviewsByRecordId(1L);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(reviews, response.getBody());
        verify(reviewUseCase).getAllReviewsByRecordId(1L);
    }


    @Test
    void updateReview_Success() {
        when(reviewUseCase.updateReview(any(UpdateReviewRequest.class))).thenReturn(updateReviewResponse);
        ResponseEntity<UpdateReviewResponse> response = controller.updateReview(updateReviewRequest, 1L);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(updateReviewResponse, response.getBody());
        verify(reviewUseCase).updateReview(any(UpdateReviewRequest.class));
    }

    @Test
    void deleteReview_Success() {
        doNothing().when(reviewUseCase).deleteReview(1L);
        ResponseEntity<String> response = controller.deleteReview(1L);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Review deleted successfully", response.getBody());
        verify(reviewUseCase).deleteReview(1L);
    }

    @Test
    void deleteReview_NotFound() {
        doThrow(new ReviewNotFoundException("Review not found"))
                .when(reviewUseCase).deleteReview(1L);
        assertThrows(ReviewNotFoundException.class,
                () -> controller.deleteReview(1L));
        verify(reviewUseCase).deleteReview(1L);
    }
}