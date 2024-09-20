package org.myexample.spinningmotion.persistence.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecordEntity {
    private Long id;
    private String title;
    private String artist;
    private String genre;
    private Double price;
    private Integer year;
    private String condition;
    private Integer quantity;
}
