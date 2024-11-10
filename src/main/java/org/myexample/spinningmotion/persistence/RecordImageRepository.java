package org.myexample.spinningmotion.persistence;

import org.myexample.spinningmotion.persistence.entity.RecordImageEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
@Repository
public interface RecordImageRepository extends JpaRepository<RecordImageEntity, Long> {
    List<RecordImageEntity> findByRecordId(Long recordId);
    void deleteByRecordId(Long recordId);
    boolean existsByRecordId(Long recordId);
}
