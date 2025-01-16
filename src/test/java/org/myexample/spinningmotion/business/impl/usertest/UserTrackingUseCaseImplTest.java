package org.myexample.spinningmotion.business.impl.usertest;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.myexample.spinningmotion.business.impl.user.UserTrackingUseCaseImpl;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class UserTrackingUseCaseImplTest {

    private UserTrackingUseCaseImpl userTrackingUseCase;

    @BeforeEach
    void setUp() {
        userTrackingUseCase = new UserTrackingUseCaseImpl();
    }

    @Test
    void addUser_ShouldIncreaseActiveUserCount() {
        // When adding a new user
        userTrackingUseCase.addUser("session1", "user1@example.com");

        // Then the active user count should be 1
        assertEquals(1, userTrackingUseCase.getActiveUsersCount());
    }

    @Test
    void addUser_WithSameSessionId_ShouldUpdateExistingUser() {
        // Given a user is already added
        userTrackingUseCase.addUser("session1", "user1@example.com");

        // When adding another user with the same session ID
        userTrackingUseCase.addUser("session1", "user2@example.com");

        // Then the count should remain 1 but the user should be updated
        assertEquals(1, userTrackingUseCase.getActiveUsersCount());
    }

    @Test
    void removeUser_ShouldDecreaseActiveUserCount() {
        // Given a user is added
        userTrackingUseCase.addUser("session1", "user1@example.com");

        // When removing the user
        userTrackingUseCase.removeUser("session1");

        // Then the active user count should be 0
        assertEquals(0, userTrackingUseCase.getActiveUsersCount());
    }

    @Test
    void removeUser_NonExistentSession_ShouldNotAffectCount() {
        // Given a user is added
        userTrackingUseCase.addUser("session1", "user1@example.com");

        // When removing a non-existent session
        userTrackingUseCase.removeUser("nonexistent");

        // Then the count should remain unchanged
        assertEquals(1, userTrackingUseCase.getActiveUsersCount());
    }

    @Test
    void getActiveUsersCount_WithMultipleUsers_ShouldReturnCorrectCount() {
        // Given multiple users are added
        userTrackingUseCase.addUser("session1", "user1@example.com");
        userTrackingUseCase.addUser("session2", "user2@example.com");
        userTrackingUseCase.addUser("session3", "user3@example.com");

        // Then the count should reflect all added users
        assertEquals(3, userTrackingUseCase.getActiveUsersCount());
    }
}
