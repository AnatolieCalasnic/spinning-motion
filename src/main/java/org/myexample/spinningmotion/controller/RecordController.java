package org.myexample.spinningmotion.controller;

import lombok.RequiredArgsConstructor;
import org.myexample.spinningmotion.business.interfc.RecordImageUseCase;
import org.myexample.spinningmotion.business.interfc.RecordUseCase;
import org.myexample.spinningmotion.domain.record.*;
import org.myexample.spinningmotion.business.exception.*;
import org.myexample.spinningmotion.persistence.entity.RecordImageEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/records")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:3000")
public class RecordController {
    private final RecordUseCase recordUseCase;
    private final RecordImageUseCase recordImageUseCase;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<CreateRecordResponse> createRecord(
            @RequestPart("record") CreateRecordRequest request,
            @RequestPart(value = "images", required = false) List<MultipartFile> images) {
        // First create the record
        CreateRecordResponse response = recordUseCase.createRecord(request);

        // Then handle the images if any are provided
        if (images != null && !images.isEmpty()) {
            try {
                recordImageUseCase.uploadMultipleImages(response.getId(), images);
            } catch (Exception e) {
                // Log the error but don't fail the record creation
                System.err.println("Failed to upload images: " + e.getMessage());
            }
        }

        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<UpdateRecordResponse> updateRecord(
            @PathVariable Long id,
            @RequestPart("record") UpdateRecordRequest request,
            @RequestPart(value = "images", required = false) List<MultipartFile> newImages) {
        request.setId(id);

        // Delete images if specified
        if (request.getImagesToDelete() != null) {
            request.getImagesToDelete().forEach(imageId -> {
                try {
                    recordImageUseCase.deleteImage(imageId);
                } catch (Exception e) {
                    // Log error but continue with update
                    System.err.println("Failed to delete image " + imageId + ": " + e.getMessage());
                }
            });
        }

        // Update record
        UpdateRecordResponse response = recordUseCase.updateRecord(request);

        // Add new images if provided
        if (newImages != null && !newImages.isEmpty()) {
            try {
                recordImageUseCase.uploadMultipleImages(id, newImages);
            } catch (Exception e) {
                System.err.println("Failed to upload new images: " + e.getMessage());
            }
        }

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<GetRecordResponse> getRecord(@PathVariable Long id) {
        GetRecordResponse response = recordUseCase.getRecord(new GetRecordRequest(id));
        return ResponseEntity.ok(response);
    }
    @GetMapping("/{id}/images")
    public ResponseEntity<List<RecordImage>> getRecordImages(@PathVariable Long id) {
        List<RecordImageEntity> images = recordImageUseCase.getImagesByRecordId(id);
        List<RecordImage> imageDTOs = images.stream()
                .map(image -> new RecordImage(image.getId(), image.getImageType()))
                .collect(Collectors.toList());
        return ResponseEntity.ok(imageDTOs);
    }

    @GetMapping("/images/{imageId}")
    public ResponseEntity<?> getImage(@PathVariable Long imageId) {
        RecordImageEntity image = recordImageUseCase.getImage(imageId);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(image.getImageType()))
                .body(image.getImageData());
    }

    @DeleteMapping("/images/{imageId}")
    public ResponseEntity<?> deleteImage(@PathVariable Long imageId) {
        recordImageUseCase.deleteImage(imageId);
        return ResponseEntity.ok().build();
    }
    @GetMapping
    public ResponseEntity<List<GetRecordResponse>> getAllRecords() {
        return ResponseEntity.ok(recordUseCase.getAllRecords());
    }
    @GetMapping("/genre/{genreName}")
    public ResponseEntity<List<GetRecordResponse>> getRecordsByGenre(@PathVariable String genreName) {
        List<GetRecordResponse> response = recordUseCase.getRecordsByGenre(genreName);
        return ResponseEntity.ok(response);
    }


    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteRecord(@PathVariable Long id) {
        recordUseCase.deleteRecord(id);
        return ResponseEntity.ok("Record deleted successfully");
    }

    @GetMapping("/new-releases")
    public ResponseEntity<List<GetRecordResponse>> getNewReleases() {
        LocalDateTime startDate = LocalDateTime.now().minusDays(1); // Adjust timeframe as needed
        List<GetRecordResponse> response = recordUseCase.getNewReleases(startDate);
        return ResponseEntity.ok(response);
    }
    @GetMapping("/new-releases/{genre}")
    public ResponseEntity<List<GetRecordResponse>> getNewReleasesByGenre(@PathVariable String genre) {
        LocalDateTime startDate = LocalDateTime.now().minusDays(1); // Adjust timeframe as needed
        List<GetRecordResponse> response = recordUseCase.getNewReleasesByGenre(startDate, genre);
        return ResponseEntity.ok(response);
    }

    //------------------------------------------------------------------------------------------------------------------
    // Exception Handlers for Record
    @ExceptionHandler(RecordNotFoundException.class)
    public ResponseEntity<String> handleRecordNotFound(RecordNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
    }
    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handleGeneralException(Exception ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("An error occurred: " + ex.getMessage());
    }
}