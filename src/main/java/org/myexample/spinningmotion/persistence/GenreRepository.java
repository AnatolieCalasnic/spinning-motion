package org.myexample.spinningmotion.persistence;

import org.myexample.spinningmotion.persistence.entity.GenreEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
@Repository

public interface GenreRepository extends JpaRepository<GenreEntity, Long> {
    GenreEntity save(GenreEntity genre);
    Optional<GenreEntity> findByName(String name);

    Optional<GenreEntity> findById(Long id);
    List<GenreEntity> findAll();
    void deleteById(Long id);
    boolean existsByName(String name);
    @Query("SELECT CASE WHEN COUNT(g) > 0 THEN true ELSE false END FROM GenreEntity g")
    boolean existsAny();
}