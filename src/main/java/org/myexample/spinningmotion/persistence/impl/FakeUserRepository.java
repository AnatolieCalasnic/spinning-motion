package org.myexample.spinningmotion.persistence.impl;

import org.myexample.spinningmotion.persistence.UserRepository;
import org.myexample.spinningmotion.persistence.entity.UserEntity;
import org.springframework.stereotype.Repository;

import java.util.*;

@Repository
public class FakeUserRepository implements UserRepository {
    private final Map<Long, UserEntity> users = new HashMap<>();
    private Long nextId = 1L;

    @Override
    public UserEntity save(UserEntity user) {
        if (user.getId() == null) {
            user.setId(nextId++);
        }
        users.put(user.getId(), user);
        return user;
    }

    @Override
    public Optional<UserEntity> findById(Long id) {
        return Optional.ofNullable(users.get(id));
    }

    @Override
    public List<UserEntity> findAll() {
        return new ArrayList<>(users.values());
    }

    @Override
    public void deleteById(Long id) {
        users.remove(id);
    }

    @Override
    public boolean existsByEmail(String email) {
        return users.values().stream().anyMatch(u -> u.getEmail().equals(email));
    }
}