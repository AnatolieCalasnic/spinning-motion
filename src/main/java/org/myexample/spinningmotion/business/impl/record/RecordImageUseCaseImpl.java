package org.myexample.spinningmotion.business.impl.record;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.myexample.spinningmotion.business.exception.ImageProcessingException;
import org.myexample.spinningmotion.business.interfc.RecordImageUseCase;
import org.myexample.spinningmotion.persistence.RecordImageRepository;
import org.myexample.spinningmotion.persistence.RecordRepository;
import org.myexample.spinningmotion.persistence.entity.RecordEntity;
import org.myexample.spinningmotion.persistence.entity.RecordImageEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RecordImageUseCaseImpl implements RecordImageUseCase {
    private final RecordImageRepository recordImageRepository;
    private final RecordRepository recordRepository;
    private static final int MAX_IMAGES = 4;
    
    @Override
    @Transactional
    public RecordImageEntity uploadImage(Long recordId, MultipartFile file) {
        try {
            RecordEntity vinylRecord = recordRepository.findById(recordId)
                    .orElseThrow(() -> new RuntimeException("Record not found"));

            RecordImageEntity image = RecordImageEntity.builder()
                    .record(vinylRecord)
                    .imageData(file.getBytes())
                    .imageType(file.getContentType())
                    .build();

            return recordImageRepository.save(image);
        } catch (IOException e) {
            throw new ImageProcessingException("Failed to upload image", e);
        }

    }
    @Override
    @Transactional
    public List<RecordImageEntity> uploadMultipleImages(Long recordId, List<MultipartFile> files) {
        RecordEntity vinylRecord = recordRepository.findById(recordId)
                .orElseThrow(() -> new ImageProcessingException("Record not found"));

        // Check existing images count
        List<RecordImageEntity> existingImages = recordImageRepository.findByRecordId(recordId);
        if (existingImages.size() + files.size() > MAX_IMAGES) {
            throw new ImageProcessingException(
                    String.format("Maximum number of images (%d) would be exceeded", MAX_IMAGES)
            );
        }

        List<RecordImageEntity> newImages = new ArrayList<>();

        for (MultipartFile file : files) {
            validateImage(file);
            try {
                byte[] imageBytes = file.getBytes();

                RecordImageEntity image = RecordImageEntity.builder()
                        .record(vinylRecord)
                        .imageData(imageBytes)
                        .imageType(file.getContentType())
                        .build();

                newImages.add(recordImageRepository.save(image));
            } catch (IOException e) {
                throw new ImageProcessingException("Failed to process image", e);
            }
        }

        // Add images to record and update
        vinylRecord.getImages().addAll(newImages);
        recordRepository.save(vinylRecord);

        return newImages;
    }


    @Override
    @Transactional
    public RecordImageEntity getImage(Long imageId) {
        return recordImageRepository.findById(imageId)
                .orElseThrow(() -> new RuntimeException("Image not found"));
    }

    @Override
    @Transactional
    public List<RecordImageEntity> getImagesByRecordId(Long recordId) {
        return recordImageRepository.findByRecordId(recordId);
    }

    @Override
    @Transactional
    public void deleteImage(Long imageId) {
        RecordImageEntity image = recordImageRepository.findById(imageId)
                .orElseThrow(() -> new RuntimeException("Image not found"));

        // Remove image from record's image list
        RecordEntity vRecord = image.getRecord();
        vRecord.getImages().remove(image);
        recordRepository.save(vRecord);

        // Delete the image
        recordImageRepository.deleteById(imageId);
    }

    @Override
    @Transactional
    public void deleteAllImagesForRecord(Long recordId) {
        recordImageRepository.deleteByRecordId(recordId);
    }

    private void validateImage(MultipartFile file) {
        if (file.isEmpty()) {
            throw new ImageProcessingException("File is empty");
        }

        if (file.getSize() > 5_000_000) { // 5MB limit
            throw new ImageProcessingException(
                    String.format("File too large. Maximum size allowed is %d MB", 5)
            );
        }

        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new ImageProcessingException("Invalid file type. Only image files are allowed");        }
    }
}