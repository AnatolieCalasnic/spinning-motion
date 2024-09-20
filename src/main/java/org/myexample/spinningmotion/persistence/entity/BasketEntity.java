package org.myexample.spinningmotion.persistence.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BasketEntity {
    private Long id;
    private Long userId;
    private List<BasketItemEntity> items;
}
