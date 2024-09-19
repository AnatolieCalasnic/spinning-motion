package org.myexample.spinningmotion.persistence;

import org.myexample.spinningmotion.persistence.entity.RecordEntity;
import java.util.List;
import java.util.Optional;

public interface RecordRepository {
    RecordEntity save(RecordEntity record);
    Optional<RecordEntity> findById(Long id);
    List<RecordEntity> findAll();
    void deleteById(Long id);
    boolean existsByTitle(String title);

}
