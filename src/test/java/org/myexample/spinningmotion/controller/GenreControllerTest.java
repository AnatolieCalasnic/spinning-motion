package org.myexample.spinningmotion.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.myexample.spinningmotion.business.exception.GenreNotFoundException;
import org.myexample.spinningmotion.business.interfc.GenreUseCase;
import org.myexample.spinningmotion.domain.genre.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GenreControllerTest {
    @Mock
    private GenreUseCase genreUseCase;

    @InjectMocks
    private GenreController genreController;

    private GetGenreResponse getGenreResponse;
    private GetAllGenresResponse getAllGenresResponse;

    @BeforeEach
    void setUp() {
        getGenreResponse = GetGenreResponse.builder()
                .id(1L)
                .name("Rock")
                .description("Rock music genre")
                .build();

        getAllGenresResponse = new GetAllGenresResponse(Arrays.asList(
                Genre.builder().id(1L).name("Rock").build(),
                Genre.builder().id(2L).name("Pop").build()
        ));
    }

    @Test
    void getGenre_Success() {
        when(genreUseCase.getGenre(any(GetGenreRequest.class))).thenReturn(getGenreResponse);
        ResponseEntity<GetGenreResponse> response = genreController.getGenre(1L);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(getGenreResponse, response.getBody());
        verify(genreUseCase).getGenre(any(GetGenreRequest.class));
    }

    @Test
    void getGenre_NotFound() {
        when(genreUseCase.getGenre(any(GetGenreRequest.class)))
                .thenThrow(new GenreNotFoundException("Genre not found"));
        assertThrows(GenreNotFoundException.class, () -> genreController.getGenre(1L));
        verify(genreUseCase).getGenre(any(GetGenreRequest.class));
    }

    @Test
    void getAllGenres_Success() {
        when(genreUseCase.getAllGenres(any(GetAllGenresRequest.class))).thenReturn(getAllGenresResponse);
        ResponseEntity<GetAllGenresResponse> response = genreController.getAllGenres();
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(getAllGenresResponse, response.getBody());
        verify(genreUseCase).getAllGenres(any(GetAllGenresRequest.class));
    }
}