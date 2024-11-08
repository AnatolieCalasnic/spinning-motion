package org.myexample.spinningmotion.business.impl;

import lombok.RequiredArgsConstructor;
import org.myexample.spinningmotion.business.exception.InvalidEmailFormatException;
import org.myexample.spinningmotion.business.exception.UserNotFoundException;
import org.myexample.spinningmotion.business.exception.EmailAlreadyExistsException;
import org.myexample.spinningmotion.business.interfc.UserUseCase;
import org.myexample.spinningmotion.domain.user.*;
import org.myexample.spinningmotion.persistence.UserRepository;
import org.myexample.spinningmotion.persistence.entity.UserEntity;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserUseCaseImpl implements UserUseCase {
    private final UserRepository userRepository;

    @Override
    public CreateUserResponse createUser(CreateUserRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new EmailAlreadyExistsException();
        }
        if (request.getEmail() == null || !request.getEmail().matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            throw new InvalidEmailFormatException();
        }
        UserEntity entity = convertToEntity(request);
        UserEntity savedEntity = userRepository.save(entity);
        return convertToCreateResponse(savedEntity);
    }

    @Override
    public GetUserResponse getUser(GetUserRequest request) {
        UserEntity entity = userRepository.findById(request.getId())
                .orElseThrow(() -> new UserNotFoundException("User not found with id: " + request.getId()));
        return convertToGetResponse(entity);
    }

    @Override
    public List<GetUserResponse> getAllUsers() {
        List<UserEntity> entities = userRepository.findAll();
        return entities.stream()
                .map(this::convertToGetResponse)
                .collect(Collectors.toList());
    }

    @Override
    public UpdateUserResponse updateUser(UpdateUserRequest request) {
        UserEntity entity = userRepository.findById(request.getId())
                .orElseThrow(() -> new UserNotFoundException("User not found with id: " + request.getId()));

        updateEntityFromRequest(entity, request);
        UserEntity updatedEntity = userRepository.save(entity);
        return convertToUpdateResponse(updatedEntity);
    }

    @Override
    public void deleteUser(Long id) {
        if (!userRepository.findById(id).isPresent()) {
            throw new UserNotFoundException("User not found with id: " + id);
        }
        userRepository.deleteById(id);
    }

    private UserEntity convertToEntity(CreateUserRequest request) {
        return UserEntity.builder()
                .fname(request.getFname())
                .lname(request.getLname())
                .email(request.getEmail())
                .password(request.getPassword())
                .address(request.getAddress())
                .postalCode(request.getPostalCode())
                .country(request.getCountry())
                .city(request.getCity())
                .region(request.getRegion())
                .phonenum(request.getPhonenum())
                .isAdmin(false)  // well, at first new users are not admins by default... later might diverse to a newer logic
                .build();
    }

    private CreateUserResponse convertToCreateResponse(UserEntity entity) {
        return CreateUserResponse.builder()
                .id(entity.getId())
                .fname(entity.getFname())
                .lname(entity.getLname())
                .email(entity.getEmail())
                .address(entity.getAddress())
                .postalCode(entity.getPostalCode())
                .country(entity.getCountry())
                .city(entity.getCity())
                .region(entity.getRegion())
                .phonenum(entity.getPhonenum())
                .isAdmin(entity.getIsAdmin())
                .build();
    }

    private GetUserResponse convertToGetResponse(UserEntity entity) {
        return GetUserResponse.builder()
                .id(entity.getId())
                .fname(entity.getFname())
                .lname(entity.getLname())
                .email(entity.getEmail())
                .address(entity.getAddress())
                .postalCode(entity.getPostalCode())
                .country(entity.getCountry())
                .city(entity.getCity())
                .region(entity.getRegion())
                .phonenum(entity.getPhonenum())
                .isAdmin(entity.getIsAdmin())
                .build();
    }

    private UpdateUserResponse convertToUpdateResponse(UserEntity entity) {
        return UpdateUserResponse.builder()
                .id(entity.getId())
                .fname(entity.getFname())
                .lname(entity.getLname())
                .email(entity.getEmail())
                .address(entity.getAddress())
                .postalCode(entity.getPostalCode())
                .country(entity.getCountry())
                .city(entity.getCity())
                .region(entity.getRegion())
                .phonenum(entity.getPhonenum())
                .isAdmin(entity.getIsAdmin())
                .build();
    }

    private void updateEntityFromRequest(UserEntity entity, UpdateUserRequest request) {
        entity.setFname(request.getFname());
        entity.setLname(request.getLname());
        entity.setEmail(request.getEmail());
        entity.setAddress(request.getAddress());
        entity.setPostalCode(request.getPostalCode());
        entity.setCountry(request.getCountry());
        entity.setCity(request.getCity());
        entity.setRegion(request.getRegion());
        entity.setPhonenum(request.getPhonenum());
        // Note: We're not updating isAdmin or password here. These might require separate use cases.
    }
}