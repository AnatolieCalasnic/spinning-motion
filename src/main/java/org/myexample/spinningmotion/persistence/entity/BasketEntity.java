package org.myexample.spinningmotion.persistence.entity;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class BasketEntity {
    private Long id;
    private Long userId;
    private List<BasketItemEntity> items;
}
