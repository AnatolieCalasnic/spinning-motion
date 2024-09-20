package org.myexample.spinningmotion.business.impl;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.InjectMocks;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.junit.jupiter.MockitoExtension;
import org.myexample.spinningmotion.business.exception.GenreNotFoundException;
import org.myexample.spinningmotion.domain.enums.GenreEnum;
import org.myexample.spinningmotion.domain.genre.*;
import org.myexample.spinningmotion.persistence.GenreRepository;
import org.myexample.spinningmotion.persistence.entity.GenreEntity;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
@ExtendWith(MockitoExtension.class)
class GenreUseCaseImplTest {

    @Mock
    private GenreRepository genreRepository;

    @InjectMocks
    private GenreUseCaseImpl genreUseCase;

    private GenreEntity genreEntity;
    private GetGenreRequest getGenreRequest;

    @BeforeEach
    void setUp() {
        genreEntity = GenreEntity.builder()
                .id(1L)
                .name("Rock")
                .description("Rock music genre")
                .build();

        getGenreRequest = new GetGenreRequest(1L);
    }

    @Test
    void getGenre_Success() {
        when(genreRepository.findById(1L)).thenReturn(Optional.of(genreEntity));

        GetGenreResponse response = genreUseCase.getGenre(getGenreRequest);

        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals("Rock", response.getName());
        assertEquals("Rock music genre", response.getDescription());

        verify(genreRepository, times(1)).findById(1L);
    }

    @Test
    void getGenre_NotFound() {
        when(genreRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(GenreNotFoundException.class, () -> genreUseCase.getGenre(getGenreRequest));

        verify(genreRepository, times(1)).findById(1L);
    }

    @Test
    void getAllGenres_MultipleGenres() {
        List<GenreEntity> genreEntities = Arrays.asList(
                genreEntity,
                GenreEntity.builder().id(2L).name("Pop").description("Pop music genre").build()
        );

        when(genreRepository.findAll()).thenReturn(genreEntities);

        GetAllGenresResponse response = genreUseCase.getAllGenres(new GetAllGenresRequest());

        assertNotNull(response);
        assertEquals(2, response.getGenres().size());
        assertEquals("Rock", response.getGenres().get(0).getName());
        assertEquals("Pop", response.getGenres().get(1).getName());

        verify(genreRepository, times(1)).findAll();
    }

    @Test
    void getAllGenres_EmptyList() {
        when(genreRepository.findAll()).thenReturn(Collections.emptyList());

        GetAllGenresResponse response = genreUseCase.getAllGenres(new GetAllGenresRequest());

        assertNotNull(response);
        assertTrue(response.getGenres().isEmpty());

        verify(genreRepository, times(1)).findAll();
    }

    @Test
    void createGenre_Success() {
        GenreEnum genreEnum = GenreEnum.ROCK;
        GenreEntity expectedEntity = new GenreEntity();
        expectedEntity.setName(genreEnum.getDisplayName());

        when(genreRepository.save(any(GenreEntity.class))).thenReturn(expectedEntity);

        assertDoesNotThrow(() -> genreUseCase.createGenre(genreEnum));

        verify(genreRepository, times(1)).save(argThat(entity ->
                entity.getName().equals(genreEnum.getDisplayName())
        ));
    }

    @Test
    void createGenre_NullEnum() {
        assertThrows(NullPointerException.class, () -> genreUseCase.createGenre(null));

        verify(genreRepository, never()).save(any(GenreEntity.class));
    }

    @Test
    void getGenre_WithNullRequest() {
        assertThrows(NullPointerException.class, () -> genreUseCase.getGenre(null));

        verify(genreRepository, never()).findById(any());
    }

    @Test
    void getAllGenres_WithNullRequest() {
        when(genreRepository.findAll()).thenReturn(Collections.emptyList());

        GetAllGenresResponse response = genreUseCase.getAllGenres(null);

        assertNotNull(response);
        assertTrue(response.getGenres().isEmpty());

        verify(genreRepository, times(1)).findAll();
    }
}