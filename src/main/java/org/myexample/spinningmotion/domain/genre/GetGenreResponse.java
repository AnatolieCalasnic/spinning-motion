package org.myexample.spinningmotion.domain.genre;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GetGenreResponse {
    private Long id;
    private String name;
    private String description;
}