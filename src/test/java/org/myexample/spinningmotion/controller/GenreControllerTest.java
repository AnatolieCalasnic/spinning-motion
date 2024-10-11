package org.myexample.spinningmotion.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.myexample.spinningmotion.business.exception.GenreNotFoundException;
import org.myexample.spinningmotion.business.interfc.GenreUseCase;
import org.myexample.spinningmotion.domain.enums.GenreEnum;
import org.myexample.spinningmotion.domain.genre.*;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(GenreController.class)
class GenreControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private GenreUseCase genreUseCase;

    @Autowired
    private ObjectMapper objectMapper;

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
                Genre.builder().id(1L).name("Rock").description("Rock music genre").build(),
                Genre.builder().id(2L).name("Pop").description("Pop music genre").build()
        ));
    }

    @Test
    void getGenre_Success() throws Exception {
        when(genreUseCase.getGenre(any(GetGenreRequest.class))).thenReturn(getGenreResponse);

        mockMvc.perform(get("/genres/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("Rock"))
                .andExpect(jsonPath("$.description").value("Rock music genre"));

        verify(genreUseCase, times(1)).getGenre(any(GetGenreRequest.class));
    }

    @Test
    void getGenre_NotFound() throws Exception {
        when(genreUseCase.getGenre(any(GetGenreRequest.class)))
                .thenThrow(new GenreNotFoundException("Genre not found"));

        mockMvc.perform(get("/genres/1"))
                .andExpect(status().isNotFound())
                .andExpect(content().string("Genre not found"));

        verify(genreUseCase, times(1)).getGenre(any(GetGenreRequest.class));
    }


    @Test
    void getAllGenres_Success() throws Exception {
        when(genreUseCase.getAllGenres(any(GetAllGenresRequest.class))).thenReturn(getAllGenresResponse);

        mockMvc.perform(get("/genres"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.genres[0].id").value(1L))
                .andExpect(jsonPath("$.genres[0].name").value("Rock"))
                .andExpect(jsonPath("$.genres[1].id").value(2L))
                .andExpect(jsonPath("$.genres[1].name").value("Pop"));

        verify(genreUseCase, times(1)).getAllGenres(any(GetAllGenresRequest.class));
    }
}