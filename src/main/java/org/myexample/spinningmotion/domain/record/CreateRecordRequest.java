package org.myexample.spinningmotion.domain.record;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateRecordRequest {
    private String title;
    private String artist;
    private String genre;
    private Double price;
    private Integer year;
    private String condition;
    private Integer quantity;
}