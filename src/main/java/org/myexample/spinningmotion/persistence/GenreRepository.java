package org.myexample.spinningmotion.persistence;

import org.myexample.spinningmotion.persistence.entity.GenreEntity;

import java.util.List;
import java.util.Optional;

public interface GenreRepository {
    GenreEntity save(GenreEntity genre);
    Optional<GenreEntity> findById(Long id);
    List<GenreEntity> findAll();
    void deleteById(Long id);
    boolean existsByName(String name);
    boolean existsAny();
}