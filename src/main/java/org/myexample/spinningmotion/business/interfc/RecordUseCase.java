package org.myexample.spinningmotion.business.interfc;


import org.myexample.spinningmotion.domain.record.*;

import java.time.LocalDateTime;
import java.util.List;

public interface RecordUseCase {
    CreateRecordResponse createRecord(CreateRecordRequest request);
    GetRecordResponse getRecord(GetRecordRequest request);
    List<GetRecordResponse> getRecordsByGenre(String genreName);
    List<GetRecordResponse> getAllRecords();
    UpdateRecordResponse updateRecord(UpdateRecordRequest request);
    void deleteRecord(Long id);
    List<GetRecordResponse> getNewReleases(LocalDateTime startDate);
    List<GetRecordResponse> getNewReleasesByGenre(LocalDateTime startDate, String genre);

}
