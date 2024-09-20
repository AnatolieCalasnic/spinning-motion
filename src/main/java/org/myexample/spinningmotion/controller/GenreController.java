package org.myexample.spinningmotion.controller;

import lombok.RequiredArgsConstructor;
import org.myexample.spinningmotion.business.interfc.GenreUseCase;
import org.myexample.spinningmotion.domain.enums.GenreEnum;
import org.myexample.spinningmotion.domain.genre.*;
import org.myexample.spinningmotion.business.exception.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/genres")
@RequiredArgsConstructor
public class GenreController {

    private final GenreUseCase genreUseCase;

    @GetMapping("/{id}")
    public ResponseEntity<GetGenreResponse> getGenre(@PathVariable Long id) {
        GetGenreResponse response = genreUseCase.getGenre(new GetGenreRequest(id));
        return ResponseEntity.ok(response);
    }
    @PostMapping("/{genreName}")
    public ResponseEntity<String> createGenre(@PathVariable String genreName) {
        try {
            GenreEnum genreEnum = GenreEnum.valueOf(genreName.toUpperCase());
            genreUseCase.createGenre(genreEnum);
            return ResponseEntity.status(201).body("Genre created: " + genreEnum.getDisplayName());
        }
        catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("Invalid genre name: " + genreName);
        }
    }
    @GetMapping
    public ResponseEntity<GetAllGenresResponse> getAllGenres() {
        GetAllGenresResponse response = genreUseCase.getAllGenres(new GetAllGenresRequest());
        return ResponseEntity.ok(response);
    }
    //------------------------------------------------------------------------------------------------------------------
    // Exception Handlers for Genre
    @ExceptionHandler(GenreNotFoundException.class)
    public ResponseEntity<String> handleGenreNotFound(GenreNotFoundException ex) {
        return ResponseEntity.status(404).body(ex.getMessage());
    }

}