package org.myexample.spinningmotion.business.interfc;

import org.myexample.spinningmotion.domain.enums.GenreEnum;
import org.myexample.spinningmotion.domain.genre.*;


public interface GenreUseCase {
    GetGenreResponse getGenre(GetGenreRequest request);
    GetAllGenresResponse getAllGenres(GetAllGenresRequest request);
    void createGenre(GenreEnum genreEnum);
}