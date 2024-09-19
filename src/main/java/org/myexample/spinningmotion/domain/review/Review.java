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
public class Review {
    private Long id;
    private Long userId;
    private Long recordId;
    private Integer rating;
    private String comment;
    private LocalDateTime createdAt;
}

