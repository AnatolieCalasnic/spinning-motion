package org.myexample.spinningmotion.domain.record;

import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor

public class UpdateRecordResponse {
    private Long id;
    private String title;
    private String artist;
    private String genre;
    private Double price;
    private Integer year;
    private String condition;
}
