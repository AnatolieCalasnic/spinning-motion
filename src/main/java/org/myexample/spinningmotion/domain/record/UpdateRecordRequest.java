package org.myexample.spinningmotion.domain.record;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateRecordRequest {
    private Long id;
    private String title;
    private String artist;
    private Long genreId;
    private Double price;
    private Integer year;
    private String condition;
    private Integer quantity;
}