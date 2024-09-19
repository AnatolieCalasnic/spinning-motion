package org.myexample.spinningmotion.persistence.impl;
import org.myexample.spinningmotion.persistence.RecordRepository;
import org.myexample.spinningmotion.persistence.entity.RecordEntity;
import org.springframework.stereotype.Repository;
import java.util.*;
import java.util.stream.Collectors;

@Repository
public class FakeRecordRepository implements RecordRepository {
    private final Map<Long, RecordEntity> records = new HashMap<>();
    private Long nextId = 1L;

    @Override
    public RecordEntity save(RecordEntity record) {
        if (record.getId() == null) {
            record.setId(nextId++);
        }
        records.put(record.getId(), record);
        return record;
    }

    @Override
    public Optional<RecordEntity> findById(Long id) {
        return Optional.ofNullable(records.get(id));
    }

    @Override
    public List<RecordEntity> findAll() {
        return new ArrayList<>(records.values());
    }

    @Override
    public void deleteById(Long id) {
        records.remove(id);
    }

    @Override
    public boolean existsByTitle(String title) {
        return records.values().stream().anyMatch(r -> r.getTitle().equals(title));
    }
}
