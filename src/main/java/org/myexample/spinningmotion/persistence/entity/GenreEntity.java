package org.myexample.spinningmotion.persistence.entity;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class GenreEntity {
    private Long id;
    private String name;
    private String description;
}
