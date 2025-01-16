package org.myexample.spinningmotion.business.impl.genre;

import lombok.RequiredArgsConstructor;
import org.myexample.spinningmotion.business.exception.GenreNotFoundException;
import org.myexample.spinningmotion.business.interfc.GenreUseCase;
import org.myexample.spinningmotion.domain.enums.GenreEnum;
import org.myexample.spinningmotion.domain.genre.*;
import org.myexample.spinningmotion.persistence.GenreRepository;
import org.myexample.spinningmotion.persistence.entity.GenreEntity;
import org.springframework.stereotype.Service;
import jakarta.annotation.PostConstruct;

import java.util.Arrays;
import java.util.List;

@Service
@RequiredArgsConstructor
public class GenreUseCaseImpl implements GenreUseCase {
    private final GenreRepository genreRepository;

    @PostConstruct
    public void initializeGenres() {
        if (!genreRepository.existsAny()) {
            Arrays.stream(GenreEnum.values()).forEach(this::createGenre);
        }
    }
    @Override
    public GetGenreResponse getGenre(GetGenreRequest request) {
        GenreEntity genre = genreRepository.findById(request.getId())
                .orElseThrow(() -> new GenreNotFoundException("Genre not found with id: " + request.getId()));
        return convertToResponse(genre);
    }

    @Override
    public GetAllGenresResponse getAllGenres(GetAllGenresRequest request) {
        List<GenreEntity> genres = genreRepository.findAll();
        List<Genre> genreList = genres.stream()
                .map(this::convertToGenre)
                .toList();
        return new GetAllGenresResponse(genreList);
    }
    @Override
    public void createGenre(GenreEnum genreEnum) {
        if (!genreRepository.existsByName(genreEnum.getDisplayName())) {
            GenreEntity genreEntity = new GenreEntity();
            genreEntity.setName(genreEnum.getDisplayName());
            genreRepository.save(genreEntity);
        }
    }
    private GetGenreResponse convertToResponse(GenreEntity entity) {
        return GetGenreResponse.builder()
                .id(entity.getId())
                .name(entity.getName())
                .build();
    }

    private Genre convertToGenre(GenreEntity entity) {
        return Genre.builder()
                .id(entity.getId())
                .name(entity.getName())
                .build();
    }
}