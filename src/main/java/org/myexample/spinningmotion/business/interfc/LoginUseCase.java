package org.myexample.spinningmotion.business.interfc;

import org.myexample.spinningmotion.persistence.entity.UserEntity;

import java.util.Optional;

public interface LoginUseCase {
    Optional<UserEntity> authUser(String email, String password);
}
