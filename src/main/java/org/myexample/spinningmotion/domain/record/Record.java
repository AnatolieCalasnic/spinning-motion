package org.myexample.spinningmotion.domain.record;

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
public class Record {
    private Long id;
    private String title;
    private String artist;
    private Long genreId;
    private Double price;
    private Integer year;
    private String condition;
    private Integer quantity;
    private List<RecordImage> images;
    private LocalDateTime createdAt;
}