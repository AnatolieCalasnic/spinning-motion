package org.myexample.spinningmotion.business.interfc;

import org.myexample.spinningmotion.persistence.entity.RecordEntity;
import org.myexample.spinningmotion.persistence.entity.RecordImageEntity;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface RecordImageUseCase {
    RecordImageEntity uploadImage(Long recordId, MultipartFile file);

    RecordImageEntity getImage(Long imageId);

    List<RecordImageEntity> getImagesByRecordId(Long recordId);

    void deleteImage(Long imageId);

    void deleteAllImagesForRecord(Long recordId);
}
