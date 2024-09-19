package org.myexample.spinningmotion.domain.review;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateReviewResponse {
    private Long id;
    private Integer rating;
    private String comment;
    private LocalDateTime updatedAt;
}