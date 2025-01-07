package org.myexample.spinningmotion.domain.record;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RecordImage {
    private Long id;
    private String imageType;
}
