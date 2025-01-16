package org.myexample.spinningmotion.persistence;

import org.myexample.spinningmotion.persistence.entity.RecordEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
@Repository
public interface RecordRepository extends JpaRepository<RecordEntity, Long> {
    //    RecordEntity save(RecordEntity record);
    Optional<RecordEntity> findById(Long id);
//
    List<RecordEntity> findAll();

    //    void deleteById(Long id);
    boolean existsByTitle(String title);

    @Query("SELECT DISTINCT r FROM RecordEntity r " +
            "LEFT JOIN r.genre g " +
            "WHERE LOWER(g.name) = LOWER(:genreName)")
    List<RecordEntity> findByGenreName(@Param("genreName") String genreName);

    @Query(value = """
    SELECT DISTINCT r.* FROM record r 
    LEFT JOIN genre g ON r.genre_id = g.id 
    WHERE 
        COALESCE(LOWER(TRIM(r.title)), '') LIKE LOWER(CONCAT('%', TRIM(:searchTerm), '%'))
        OR COALESCE(LOWER(TRIM(r.artist)), '') LIKE LOWER(CONCAT('%', TRIM(:searchTerm), '%'))
        OR COALESCE(LOWER(CONCAT(TRIM(r.artist), ' ', TRIM(r.title))), '') LIKE LOWER(CONCAT('%', TRIM(:searchTerm), '%'))
    ORDER BY r.artist, r.title
    """, nativeQuery = true)
    List<RecordEntity> searchRecords(@Param("searchTerm") String searchTerm);

    @Query(value = "SELECT r.* FROM record r " +
            "LEFT JOIN record_images ri ON r.id = ri.record_id " +
            "GROUP BY r.id", nativeQuery = true)
    List<RecordEntity> findAllWithImages();

    @Query("SELECT r FROM RecordEntity r WHERE r.createdAt >= :startDate ORDER BY r.createdAt DESC")
    List<RecordEntity> findNewReleases(@Param("startDate") LocalDateTime startDate);

    @Query("SELECT r FROM RecordEntity r " +
            "WHERE r.createdAt >= :startDate " +
            "AND LOWER(r.genre.name) = LOWER(:genre) " +
            "ORDER BY r.createdAt DESC")
    List<RecordEntity> findNewReleasesByGenre(
            @Param("startDate") LocalDateTime startDate,
            @Param("genre") String genre
    );
    List<RecordEntity> findByArtist(String artistName);


}
