package org.myexample.spinningmotion.persistence.impl;

import org.myexample.spinningmotion.persistence.GenreRepository;
import org.myexample.spinningmotion.persistence.entity.GenreEntity;
import org.springframework.stereotype.Repository;

import java.util.*;

@Repository
public class FakeGenreRepository implements GenreRepository {
    private final Map<Long, GenreEntity> genres = new HashMap<>();
    private Long nextId = 1L;

    @Override
    public GenreEntity save(GenreEntity genre) {
        if (genre.getId() == null) {
            genre.setId(nextId++);
        }
        genres.put(genre.getId(), genre);
        return genre;
    }

    @Override
    public Optional<GenreEntity> findById(Long id) {
        return Optional.ofNullable(genres.get(id));
    }

    @Override
    public List<GenreEntity> findAll() {
        return new ArrayList<>(genres.values());
    }

    @Override
    public void deleteById(Long id) {
        genres.remove(id);
    }

    @Override
    public boolean existsByName(String name) {
        return genres.values().stream().anyMatch(g -> g.getName().equals(name));
    }
    @Override
    public boolean existsAny() {
        return !genres.isEmpty();
    }
}