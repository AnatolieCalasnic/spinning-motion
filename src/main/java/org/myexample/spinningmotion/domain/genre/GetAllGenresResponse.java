package org.myexample.spinningmotion.domain.genre;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GetAllGenresResponse {
    private List<Genre> genres;
}