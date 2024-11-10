package org.myexample.spinningmotion.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.myexample.spinningmotion.business.exception.DuplicateReviewException;
import org.myexample.spinningmotion.business.exception.ReviewNotFoundException;
import org.myexample.spinningmotion.business.interfc.ReviewUseCase;
import org.myexample.spinningmotion.domain.review.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/reviews")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:3000")

public class ReviewController {
    private final ReviewUseCase reviewUseCase;

    @PostMapping
    public ResponseEntity<?> createReview(@Valid @RequestBody CreateReviewRequest request) {
        try {
            CreateReviewResponse response = reviewUseCase.createReview(request);
            return new ResponseEntity<>(response, HttpStatus.CREATED);
        }
        catch (DuplicateReviewException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<GetReviewResponse> getReview(@PathVariable Long id) {
        try {
            GetReviewResponse response = reviewUseCase.getReview(new GetReviewRequest(id));
            return ResponseEntity.ok(response);
        }
        catch (ReviewNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
    }

    @GetMapping("/record/{recordId}")
    public ResponseEntity<List<GetReviewResponse>> getAllReviewsByRecordId(@PathVariable Long recordId) {
        return ResponseEntity.ok(reviewUseCase.getAllReviewsByRecordId(recordId));
    }

    @PutMapping("/{id}")
    public ResponseEntity<UpdateReviewResponse> updateReview(@Valid @RequestBody UpdateReviewRequest request, @PathVariable Long id) {
        request.setId(id);
        UpdateReviewResponse response = reviewUseCase.updateReview(request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteReview(@PathVariable Long id) {
        reviewUseCase.deleteReview(id);
        return ResponseEntity.ok("Review deleted successfully");
    }
    //------------------------------------------------------------------------------------------------------------------
    // Exception Handlers for Review
    @ExceptionHandler(DuplicateReviewException.class)
    public ResponseEntity<String> handleDuplicateReview(DuplicateReviewException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(ex.getMessage());
    }

    @ExceptionHandler(ReviewNotFoundException.class)
    public ResponseEntity<String> handleReviewNotFound(ReviewNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
    }
}
