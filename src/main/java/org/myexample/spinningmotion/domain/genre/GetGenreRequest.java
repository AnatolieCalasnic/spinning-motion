package org.myexample.spinningmotion.domain.genre;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor

public class GetGenreRequest {
    private Long id;

}
