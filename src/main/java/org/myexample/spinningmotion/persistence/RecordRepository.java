package org.myexample.spinningmotion.persistence;

import org.myexample.spinningmotion.persistence.entity.RecordEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
@Repository
public interface RecordRepository extends JpaRepository<RecordEntity, Long> {
//    RecordEntity save(RecordEntity record);
    Optional<RecordEntity> findById(Long id);
    List<RecordEntity> findAll();
//    void deleteById(Long id);
    boolean existsByTitle(String title);
    @Query("SELECT r FROM RecordEntity r JOIN r.genre g WHERE LOWER(g.name) = LOWER(:genreName)")
    List<RecordEntity> findByGenreName(@Param("genreName") String genreName);
}
