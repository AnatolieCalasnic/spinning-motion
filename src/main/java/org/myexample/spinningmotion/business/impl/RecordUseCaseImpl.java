package org.myexample.spinningmotion.business.impl;

import lombok.RequiredArgsConstructor;
import org.myexample.spinningmotion.business.exception.InvalidInputException;
import org.myexample.spinningmotion.business.exception.RecordNotFoundException;
import org.myexample.spinningmotion.business.interfc.RecordUseCase;
import org.myexample.spinningmotion.domain.record.*;
import org.myexample.spinningmotion.persistence.GenreRepository;
import org.myexample.spinningmotion.persistence.RecordRepository;
import org.myexample.spinningmotion.persistence.entity.GenreEntity;
import org.myexample.spinningmotion.persistence.entity.RecordEntity;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RecordUseCaseImpl implements RecordUseCase {
    private final RecordRepository recordRepository;
    private final GenreRepository genreRepository;
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
        List<RecordEntity> entities = recordRepository.findAll();
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
                .build();
    }

    private GetRecordResponse convertToGetResponse(RecordEntity entity) {
        return GetRecordResponse.builder()
                .id(entity.getId())
                .title(entity.getTitle())
                .artist(entity.getArtist())
                .genreId(entity.getGenre().getId())  // Get genre name
                .price(entity.getPrice())
                .year(entity.getYear())
                .condition(entity.getCondition())
                .quantity(entity.getQuantity())
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