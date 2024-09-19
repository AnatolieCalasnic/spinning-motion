package org.myexample.spinningmotion.business.interfc;

import org.myexample.spinningmotion.domain.user.*;

import java.util.List;
public interface UserUseCase {
    CreateUserResponse createUser(CreateUserRequest request);
    GetUserResponse getUser(GetUserRequest request);
    List<GetUserResponse> getAllUsers();
    UpdateUserResponse updateUser(UpdateUserRequest request);
    void deleteUser(Long id);
}