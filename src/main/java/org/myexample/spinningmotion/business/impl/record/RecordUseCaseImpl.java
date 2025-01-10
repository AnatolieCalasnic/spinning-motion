package org.myexample.spinningmotion.business.impl.record;

import lombok.RequiredArgsConstructor;
import lombok.Value;
import org.myexample.spinningmotion.business.exception.InvalidInputException;
import org.myexample.spinningmotion.business.exception.RecordNotFoundException;
import org.myexample.spinningmotion.business.interfc.RecordUseCase;
import org.myexample.spinningmotion.domain.record.*;
import org.myexample.spinningmotion.persistence.GenreRepository;
import org.myexample.spinningmotion.persistence.PurchaseHistoryRepository;
import org.myexample.spinningmotion.persistence.RecordRepository;
import org.myexample.spinningmotion.persistence.entity.GenreEntity;
import org.myexample.spinningmotion.persistence.entity.PurchaseHistoryEntity;
import org.myexample.spinningmotion.persistence.entity.RecordEntity;
import org.springframework.stereotype.Service;
import org.myexample.spinningmotion.domain.record.RecordImage;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RecordUseCaseImpl implements RecordUseCase {
    private final RecordRepository recordRepository;
    private final GenreRepository genreRepository;
    private final PurchaseHistoryRepository purchaseHistoryRepository;
    @Override
    public CreateRecordResponse createRecord(CreateRecordRequest request) {
        if (request.getPrice() <= 0) {
            throw new InvalidInputException("Price must be greater than zero");
        }
        if (recordRepository.existsByTitle(request.getTitle())) {
            throw new IllegalArgumentException("Record with this title already exists");
        }
        GenreEntity genre = genreRepository.findById(request.getGenreId())
                .orElseThrow(() -> new InvalidInputException("Genre not found: " + request.getGenreId()));

        RecordEntity entity = convertToEntity(request, genre);
        RecordEntity savedEntity = recordRepository.save(entity);
        return convertToCreateResponse(savedEntity);
    }

    @Override
    public GetRecordResponse getRecord(GetRecordRequest request) {
        RecordEntity entity = recordRepository.findById(request.getId())
                .orElseThrow(() -> new RecordNotFoundException("Record not found with id: " + request.getId()));
        return convertToGetResponse(entity);
    }

    @Override
    public List<GetRecordResponse> getAllRecords() {
        List<RecordEntity> entities = recordRepository.findAllWithImages();
        return entities.stream()
                .map(this::convertToGetResponse)
                .collect(Collectors.toList());
    }

    @Override
    public UpdateRecordResponse updateRecord(UpdateRecordRequest request) {
        RecordEntity entity = recordRepository.findById(request.getId())
                .orElseThrow(() -> new RecordNotFoundException("Record not found with id: " + request.getId()));

        updateEntityFromRequest(entity, request);
        RecordEntity updatedEntity = recordRepository.save(entity);
        return convertToUpdateResponse(updatedEntity);
    }

    @Override
    public void deleteRecord(Long id) {
        if (!recordRepository.findById(id).isPresent()) {
            throw new RecordNotFoundException("Record not found with id: " + id);
        }
        recordRepository.deleteById(id);
    }
    @Override
    public List<GetRecordResponse> getNewReleases(LocalDateTime startDate) {
        List<RecordEntity> newReleases = recordRepository.findNewReleases(startDate);
        return newReleases.stream()
                .map(this::convertToGetResponse)
                .collect(Collectors.toList());
    }
    @Override
    public List<GetRecordResponse> getTopThreeArtists() {
        // Get all records
        List<RecordEntity> allRecords = recordRepository.findAll();

        // Get all purchase histories
        List<PurchaseHistoryEntity> allPurchases = purchaseHistoryRepository.findAll();

        // Group purchases by record ID and sum quantities
        Map<Long, Integer> recordSales = allPurchases.stream()
                .collect(Collectors.groupingBy(
                        PurchaseHistoryEntity::getRecordId,
                        Collectors.summingInt(PurchaseHistoryEntity::getQuantity)
                ));

        // Map records to their sales and group by artist
        Map<String, Integer> artistTotalSales = allRecords.stream()
                .collect(Collectors.groupingBy(
                        RecordEntity::getArtist,
                        Collectors.summingInt(record ->
                                recordSales.getOrDefault(record.getId(), 0)
                        )
                ));

        // Get top 3 artists and their best-selling records
        return artistTotalSales.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .limit(3)
                .map(entry -> {
                    // Find the best-selling record for this artist
                    RecordEntity bestSellingRecord = allRecords.stream()
                            .filter(record -> record.getArtist().equals(entry.getKey()))
                            .max(Comparator.comparingInt(record ->
                                    recordSales.getOrDefault(record.getId(), 0)
                            ))
                            .orElseThrow();

                    return convertToGetResponse(bestSellingRecord);
                })
                .collect(Collectors.toList());
    }
    @Override
    public List<GetRecordResponse> getNewReleasesByGenre(LocalDateTime startDate, String genre) {
        List<RecordEntity> newReleases = recordRepository.findNewReleasesByGenre(startDate, genre.toLowerCase());
        return newReleases.stream()
                .map(this::convertToGetResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<GetRecordResponse> getRecordsByGenre(String genreName) {
        return recordRepository.findByGenreName(genreName.toLowerCase())
                .stream()
                .map(recordEntity -> GetRecordResponse.builder()
                        .id(recordEntity.getId())
                        .title(recordEntity.getTitle())
                        .artist(recordEntity.getArtist())
                        .price(recordEntity.getPrice())
                        .year(recordEntity.getYear())
                        .condition(recordEntity.getCondition())
                        .quantity(recordEntity.getQuantity())
                        .build())
                .collect(Collectors.toList());
    }
    @Override
    public List<GetRecordResponse> getRecordsByArtist(String artistName) {
        return recordRepository.findByArtist(artistName.replace("-", " "))
                .stream()
                .map(this::convertToGetResponse)
                .collect(Collectors.toList());
    }

    private RecordEntity convertToEntity(CreateRecordRequest request, GenreEntity genre) {
        return RecordEntity.builder()
                .title(request.getTitle())
                .artist(request.getArtist())
                .genre(genre)
                .price(request.getPrice())
                .year(request.getYear())
                .condition(request.getCondition())
                .quantity(request.getQuantity())
                .build();
    }

    private CreateRecordResponse convertToCreateResponse(RecordEntity entity) {
        return CreateRecordResponse.builder()
                .id(entity.getId())
                .title(entity.getTitle())
                .artist(entity.getArtist())
                .genreId(entity.getGenre().getId())
                .price(entity.getPrice())
                .year(entity.getYear())
                .condition(entity.getCondition())
                .quantity(entity.getQuantity())
                .createdAt(entity.getCreatedAt())
                .build();
    }

    private GetRecordResponse convertToGetResponse(RecordEntity entity) {
        List<RecordImage> recordImages = entity.getImages().stream()
                .map(imageEntity -> new RecordImage(
                        imageEntity.getId(),
                        imageEntity.getImageType()
                ))
                .collect(Collectors.toList());

        return GetRecordResponse.builder()
                .id(entity.getId())
                .title(entity.getTitle())
                .artist(entity.getArtist())
                .genreId(entity.getGenre().getId())  // Get genre name
                .price(entity.getPrice())
                .year(entity.getYear())
                .condition(entity.getCondition())
                .quantity(entity.getQuantity())
                .images(recordImages)
                .createdAt(entity.getCreatedAt())
                .build();
    }

    private UpdateRecordResponse convertToUpdateResponse(RecordEntity entity) {
        return UpdateRecordResponse.builder()
                .id(entity.getId())
                .title(entity.getTitle())
                .artist(entity.getArtist())
                .genreId(entity.getGenre().getId())
                .price(entity.getPrice())
                .year(entity.getYear())
                .condition(entity.getCondition())
                .quantity(entity.getQuantity())
                .createdAt(entity.getCreatedAt())
                .build();
    }

    private void updateEntityFromRequest(RecordEntity entity, UpdateRecordRequest request) {
        GenreEntity genre = genreRepository.findById(request.getGenreId())
                .orElseThrow(() -> new InvalidInputException("Genre not found: " + request.getId()));

        entity.setTitle(request.getTitle());
        entity.setArtist(request.getArtist());
        entity.setGenre(genre);  // Set GenreEntity
        entity.setPrice(request.getPrice());
        entity.setYear(request.getYear());
        entity.setCondition(request.getCondition());
        entity.setQuantity(request.getQuantity());
    }
}