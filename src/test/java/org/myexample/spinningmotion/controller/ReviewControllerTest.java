package org.myexample.spinningmotion.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.junit.jupiter.api.BeforeEach;
import org.myexample.spinningmotion.business.exception.DuplicateReviewException;
import org.myexample.spinningmotion.business.exception.ReviewNotFoundException;
import org.myexample.spinningmotion.business.interfc.ReviewUseCase;
import org.myexample.spinningmotion.domain.review.*;

import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ReviewController.class)
class ReviewControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ReviewUseCase reviewUseCase;

    @Autowired
    private ObjectMapper objectMapper;

    private CreateReviewRequest createReviewRequest;
    private CreateReviewResponse createReviewResponse;
    private GetReviewResponse getReviewResponse;
    private UpdateReviewRequest updateReviewRequest;
    private UpdateReviewResponse updateReviewResponse;

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
    void createReview_Success() throws Exception {
        when(reviewUseCase.createReview(any(CreateReviewRequest.class))).thenReturn(createReviewResponse);

        mockMvc.perform(post("/reviews")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createReviewRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.userId").value(1L))
                .andExpect(jsonPath("$.recordId").value(1L))
                .andExpect(jsonPath("$.rating").value(5))
                .andExpect(jsonPath("$.comment").value("Great record!"))
                .andExpect(jsonPath("$.createdAt").exists());

        verify(reviewUseCase, times(1)).createReview(any(CreateReviewRequest.class));
    }

    @Test
    void createReview_DuplicateReview() throws Exception {
        when(reviewUseCase.createReview(any(CreateReviewRequest.class)))
                .thenThrow(new DuplicateReviewException());

        mockMvc.perform(post("/reviews")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createReviewRequest)))
                .andExpect(status().isConflict());

        verify(reviewUseCase, times(1)).createReview(any(CreateReviewRequest.class));
    }

    @Test
    void getReview_Success() throws Exception {
        when(reviewUseCase.getReview(any(GetReviewRequest.class))).thenReturn(getReviewResponse);

        mockMvc.perform(get("/reviews/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.userId").value(1L))
                .andExpect(jsonPath("$.recordId").value(1L))
                .andExpect(jsonPath("$.rating").value(5))
                .andExpect(jsonPath("$.comment").value("Great record!"))
                .andExpect(jsonPath("$.createdAt").exists());

        verify(reviewUseCase, times(1)).getReview(any(GetReviewRequest.class));
    }

    @Test
    void getReview_NotFound() throws Exception {
        when(reviewUseCase.getReview(any(GetReviewRequest.class)))
                .thenThrow(new ReviewNotFoundException("Review not found"));

        mockMvc.perform(get("/reviews/1"))
                .andExpect(status().isNotFound());

        verify(reviewUseCase, times(1)).getReview(any(GetReviewRequest.class));
    }

    @Test
    void getAllReviewsByRecordId_Success() throws Exception {
        List<GetReviewResponse> reviews = Arrays.asList(getReviewResponse);
        when(reviewUseCase.getAllReviewsByRecordId(1L)).thenReturn(reviews);

        mockMvc.perform(get("/reviews/record/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].userId").value(1L))
                .andExpect(jsonPath("$[0].recordId").value(1L))
                .andExpect(jsonPath("$[0].rating").value(5))
                .andExpect(jsonPath("$[0].comment").value("Great record!"))
                .andExpect(jsonPath("$[0].createdAt").exists());

        verify(reviewUseCase, times(1)).getAllReviewsByRecordId(1L);
    }

    @Test
    void updateReview_Success() throws Exception {
        when(reviewUseCase.updateReview(any(UpdateReviewRequest.class))).thenReturn(updateReviewResponse);

        mockMvc.perform(put("/reviews/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateReviewRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.rating").value(4))
                .andExpect(jsonPath("$.comment").value("Updated review"));

        verify(reviewUseCase, times(1)).updateReview(any(UpdateReviewRequest.class));
    }

    @Test
    void deleteReview_Success() throws Exception {
        doNothing().when(reviewUseCase).deleteReview(1L);

        mockMvc.perform(delete("/reviews/1"))
                .andExpect(status().isOk())
                .andExpect(content().string("Review deleted successfully"));

        verify(reviewUseCase, times(1)).deleteReview(1L);
    }

    @Test
    void deleteReview_NotFound() throws Exception {
        doThrow(new ReviewNotFoundException("Review not found")).when(reviewUseCase).deleteReview(1L);

        mockMvc.perform(delete("/reviews/1"))
                .andExpect(status().isNotFound())
                .andExpect(content().string("Review not found"));

        verify(reviewUseCase, times(1)).deleteReview(1L);
    }
}