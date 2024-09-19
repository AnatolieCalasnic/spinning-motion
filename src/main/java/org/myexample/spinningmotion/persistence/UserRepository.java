package org.myexample.spinningmotion.persistence;


import org.myexample.spinningmotion.persistence.entity.UserEntity;
import java.util.List;
import java.util.Optional;

public interface UserRepository {
    UserEntity save(UserEntity user);
    Optional<UserEntity> findById(Long id);
    List<UserEntity> findAll();
    void deleteById(Long id);
    boolean existsByEmail(String email);
}