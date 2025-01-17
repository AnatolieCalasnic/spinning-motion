package org.myexample.spinningmotion.business.impl.record;

import lombok.RequiredArgsConstructor;
import org.myexample.spinningmotion.business.exception.InvalidInputException;
import org.myexample.spinningmotion.business.exception.RecordNotFoundException;
import org.myexample.spinningmotion.business.interfc.InventoryTrackingUseCase;
import org.myexample.spinningmotion.business.interfc.RecordUseCase;
import org.myexample.spinningmotion.domain.record.*;
import org.myexample.spinningmotion.persistence.GenreRepository;
import org.myexample.spinningmotion.persistence.PurchaseHistoryRepository;
import org.myexample.spinningmotion.persistence.RecordRepository;
import org.myexample.spinningmotion.persistence.entity.GenreEntity;
import org.myexample.spinningmotion.persistence.entity.PurchaseHistoryEntity;
import org.myexample.spinningmotion.persistence.entity.RecordEntity;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.myexample.spinningmotion.domain.record.RecordImage;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;



@Service
@RequiredArgsConstructor
public class RecordUseCaseImpl implements RecordUseCase {
    private static final String RECORD_NOT_FOUND_MESSAGE = "Record not found with id: ";
    private static final String PRICE_VALIDATION_MESSAGE = "Price must be greater than zero";
    private static final String TITLE_EXISTS_MESSAGE = "Record with this title already exists";
    private static final String NULL_REQUEST_MESSAGE = "Request cannot be null";
    private static final String TITLE_VALIDATION_MESSAGE = "Title cannot be null or empty";
    private static final String GENRE_NOT_FOUND_MESSAGE = "Genre not found: ";
    private static final String ID_VALIDATION_MESSAGE = "Record ID cannot be null";
    private static final String ARTIST_VALIDATION_MESSAGE = "Artist name is required";
    private static final String YEAR_VALIDATION_MESSAGE = "Release year must be 1900 or later";
    private static final String CONDITION_VALIDATION_MESSAGE = "Condition must be one of: Mint, Near Mint, Very Good, Good";
    private static final String GENRE_VALIDATION_MESSAGE = "Genre must be selected";
    private static final Set<String> VALID_CONDITIONS = Set.of("Mint", "Near Mint", "Very Good", "Good");
    private static final int MIN_TITLE_LENGTH = 1;
    private static final int MAX_TITLE_LENGTH = 255;
    private static final int MIN_ARTIST_LENGTH = 1;
    private static final int MAX_ARTIST_LENGTH = 255;
    private static final int MIN_YEAR = 1900;
    private static final int MIN_QUANTITY = 1;
    private static final double MIN_PRICE = 0.01;
    private final RecordRepository recordRepository;
    private final GenreRepository genreRepository;
    private final PurchaseHistoryRepository purchaseHistoryRepository;
    private final InventoryTrackingUseCase inventoryTrackingUseCase;
    private final ApplicationEventPublisher eventPublisher;



    private void validateRecordFields(String title, String artist, Long genreId, double price,
                                      Integer year, String condition, int quantity) {
        List<String> errors = new ArrayList<>();

        validateTitle(title, errors);
        validateArtist(artist, errors);
        validateBasicFields(genreId, price, year, condition, quantity, errors);

        if (!errors.isEmpty()) {
            throw new InvalidInputException(String.join(", ", errors));
        }
    }

    private void validateTitle(String title, List<String> errors) {
        if (title == null || title.trim().isEmpty()) {
            errors.add(TITLE_VALIDATION_MESSAGE);
            return;
        }

        if (title.length() < MIN_TITLE_LENGTH || title.length() > MAX_TITLE_LENGTH) {
            errors.add("Title must be between " + MIN_TITLE_LENGTH + " and " + MAX_TITLE_LENGTH + " characters");
        }
    }

    private void validateArtist(String artist, List<String> errors) {
        if (artist == null || artist.trim().isEmpty()) {
            errors.add(ARTIST_VALIDATION_MESSAGE);
            return;
        }

        if (artist.length() < MIN_ARTIST_LENGTH || artist.length() > MAX_ARTIST_LENGTH) {
            errors.add("Artist name must be between " + MIN_ARTIST_LENGTH + " and " + MAX_ARTIST_LENGTH + " characters");
        }
    }

    private void validateBasicFields(Long genreId, double price, Integer year,
                                     String condition, int quantity, List<String> errors) {
        // Genre validation
        if (genreId == null) {
            errors.add(GENRE_VALIDATION_MESSAGE);
        }

        // Price validation
        if (price < MIN_PRICE) {
            errors.add(PRICE_VALIDATION_MESSAGE);
        }

        // Year validation
        if (year != null && year < MIN_YEAR) {
            errors.add(YEAR_VALIDATION_MESSAGE);
        }

        // Condition validation
        if (condition == null || !VALID_CONDITIONS.contains(condition)) {
            errors.add(CONDITION_VALIDATION_MESSAGE);
        }

        // Quantity validation
        if (quantity < MIN_QUANTITY) {
            errors.add("Quantity must be at least " + MIN_QUANTITY);
        }
    }

    private void validateCreateRecordRequest(CreateRecordRequest request) {
        if (request == null) {
            throw new InvalidInputException(NULL_REQUEST_MESSAGE);
        }

        validateRecordFields(
                request.getTitle(),
                request.getArtist(),
                request.getGenreId(),
                request.getPrice(),
                request.getYear(),
                request.getCondition(),
                request.getQuantity()
        );

        // Check for duplicate title
        if (recordRepository.existsByTitle(request.getTitle().trim())) {
            throw new InvalidInputException(TITLE_EXISTS_MESSAGE);
        }
    }

    private void validateUpdateRecordRequest(UpdateRecordRequest request) {
        if (request == null) {
            throw new InvalidInputException(NULL_REQUEST_MESSAGE);
        }
        if (request.getId() == null) {
            throw new InvalidInputException(ID_VALIDATION_MESSAGE);
        }

        validateRecordFields(
                request.getTitle(),
                request.getArtist(),
                request.getGenreId(),
                request.getPrice(),
                request.getYear(),
                request.getCondition(),
                request.getQuantity()
        );

        // Check for duplicate title only if title is changed
        RecordEntity existingRecord = recordRepository.findById(request.getId())
                .orElseThrow(() -> new RecordNotFoundException(RECORD_NOT_FOUND_MESSAGE + request.getId()));

        if (!existingRecord.getTitle().equals(request.getTitle()) &&
                recordRepository.existsByTitle(request.getTitle().trim())) {
            throw new InvalidInputException(TITLE_EXISTS_MESSAGE);
        }
    }


    @Override
    public CreateRecordResponse createRecord(CreateRecordRequest request) {
        validateCreateRecordRequest(request);

        if (request.getPrice() <= 0) {
            throw new InvalidInputException(PRICE_VALIDATION_MESSAGE);
        }
        if (recordRepository.existsByTitle(request.getTitle())) {
            throw new IllegalArgumentException(TITLE_EXISTS_MESSAGE);
        }
        GenreEntity genre = genreRepository.findById(request.getGenreId())
                .orElseThrow(() -> new InvalidInputException(GENRE_NOT_FOUND_MESSAGE + request.getGenreId()));

        RecordEntity entity = convertToEntity(request, genre);
        RecordEntity savedEntity = recordRepository.save(entity);
        inventoryTrackingUseCase.broadcastInventoryUpdate(
                InventoryUpdate.builder()
                        .recordId(savedEntity.getId())
                        .title(savedEntity.getTitle())
                        .quantity(savedEntity.getQuantity())
                        .updateType("CREATE")
                        .build()
        );
        return convertToCreateResponse(savedEntity);
    }

    @Override
    public GetRecordResponse getRecord(GetRecordRequest request) {
        RecordEntity entity = recordRepository.findById(request.getId())
                .orElseThrow(() -> new RecordNotFoundException( RECORD_NOT_FOUND_MESSAGE + request.getId()));
        return convertToGetResponse(entity);
    }

    @Override
    public List<GetRecordResponse> getAllRecords() {
        List<RecordEntity> entities = recordRepository.findAllWithImages();
        return entities.stream()
                .map(this::convertToGetResponse)
                .toList();
    }

    @Override
    public UpdateRecordResponse updateRecord(UpdateRecordRequest request) {
        validateUpdateRecordRequest(request);
        RecordEntity entity = recordRepository.findById(request.getId())
                .orElseThrow(() -> new RecordNotFoundException( RECORD_NOT_FOUND_MESSAGE + request.getId()));

        updateEntityFromRequest(entity, request);
        RecordEntity updatedEntity = recordRepository.save(entity);
        inventoryTrackingUseCase.broadcastInventoryUpdate(
                InventoryUpdate.builder()
                        .recordId(updatedEntity.getId())
                        .title(updatedEntity.getTitle())
                        .quantity(updatedEntity.getQuantity())
                        .updateType("UPDATE")
                        .build()
        );

        return convertToUpdateResponse(updatedEntity);

    }

    @Override
    public void deleteRecord(Long id) {
        RecordEntity entity = recordRepository.findById(id)
                .orElseThrow(() -> new RecordNotFoundException(RECORD_NOT_FOUND_MESSAGE + id));
        inventoryTrackingUseCase.broadcastInventoryUpdate(
                InventoryUpdate.builder()
                        .recordId(id)
                        .title(entity.getTitle())
                        .quantity(0)
                        .updateType("DELETE")
                        .build()
        );
        recordRepository.deleteById(id);
    }
    @Override
    public List<GetRecordResponse> getNewReleases(LocalDateTime startDate) {
        List<RecordEntity> newReleases = recordRepository.findNewReleases(startDate);
        return newReleases.stream()
                .map(this::convertToGetResponse)
                .toList();
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
                        Collectors.summingInt(recordEnt ->
                                recordSales.getOrDefault(recordEnt.getId(), 0)
                        )
                ));

        // Get top 3 artists and their best-selling records
        return artistTotalSales.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .limit(3)
                .map(entry -> {
                    // Find the best-selling record for this artist
                    RecordEntity bestSellingRecord = allRecords.stream()
                            .filter(recordEnt -> recordEnt.getArtist().equals(entry.getKey()))
                            .max(Comparator.comparingInt(recordEnt ->
                                    recordSales.getOrDefault(recordEnt.getId(), 0)
                            ))
                            .orElseThrow();

                    return convertToGetResponse(bestSellingRecord);
                })
                .toList();
    }
    @Override
    public List<GetRecordResponse> getNewReleasesByGenre(LocalDateTime startDate, String genre) {
        List<RecordEntity> newReleases = recordRepository.findNewReleasesByGenre(startDate, genre.toLowerCase());
        return newReleases.stream()
                .map(this::convertToGetResponse)
                .toList();
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
                .toList();
    }
    @Override
    public List<GetRecordResponse> getRecordsByArtist(String artistName) {
        return recordRepository.findByArtist(artistName.replace("-", " "))
                .stream()
                .map(this::convertToGetResponse)
                .toList();
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
                .toList();

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
                .orElseThrow(() -> new InvalidInputException(GENRE_NOT_FOUND_MESSAGE + request.getId()));

        entity.setTitle(request.getTitle());
        entity.setArtist(request.getArtist());
        entity.setGenre(genre);  // Set GenreEntity
        entity.setPrice(request.getPrice());
        entity.setYear(request.getYear());
        entity.setCondition(request.getCondition());
        entity.setQuantity(request.getQuantity());
    }
}