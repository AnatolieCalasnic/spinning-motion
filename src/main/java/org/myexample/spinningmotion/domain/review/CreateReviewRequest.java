package org.myexample.spinningmotion.domain.review;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateReviewRequest {
    private Long userId;
    private Long recordId;
    private Integer rating;
    private String comment;
}
