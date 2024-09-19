package org.myexample.spinningmotion.persistence.entity;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Builder
public class ReviewEntity {
    private Long id;
    private Long userId;
    private Long recordId;
    private Integer rating;
    private String comment;
    private LocalDateTime createdAt;
}