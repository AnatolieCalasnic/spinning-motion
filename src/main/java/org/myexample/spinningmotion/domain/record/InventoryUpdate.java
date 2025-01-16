package org.myexample.spinningmotion.domain.record;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class InventoryUpdate {
    private Long recordId;
    private String title;
    private int quantity;
    private String updateType;
}
