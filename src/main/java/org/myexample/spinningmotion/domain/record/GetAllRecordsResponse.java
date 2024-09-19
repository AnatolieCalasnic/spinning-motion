package org.myexample.spinningmotion.domain.record;

import java.util.List;
import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GetAllRecordsResponse {
    private List<Record> records;
}
