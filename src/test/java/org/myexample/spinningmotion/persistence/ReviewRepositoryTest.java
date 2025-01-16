package org.myexample.spinningmotion.persistence;

import org.junit.jupiter.api.Test;
import org.myexample.spinningmotion.persistence.entity.RecordEntity;
import org.myexample.spinningmotion.persistence.entity.ReviewEntity;
import org.myexample.spinningmotion.persistence.entity.UserEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class ReviewRepositoryTest {
    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private ReviewRepository reviewRepository;

    private UserEntity createTestUser(String email) {
        return UserEntity.builder()
                .fname("John")
                .lname("Doe")
                .email(email)
                .password("password123")
                .address("Test Street")
                .postalCode("1234AB")
                .country("NL")
                .city("Eindhoven")
                .region("Noord Brabant")
                .phonenum("1234567890")
                .isAdmin(false)
                .build();
    }

    private RecordEntity createTestRecord() {
        return RecordEntity.builder()
                .title("Test Record")
                .artist("Test Artist")
                .price(19.99)
                .year(2023)
                .condition("New")
                .quantity(10)
                .build();
    }

    private ReviewEntity createTestReview(UserEntity user, RecordEntity vRecord) {
        return ReviewEntity.builder()
                .user(user)
                .record(vRecord)
                .rating(5)
                .comment("Great album!")
                .createdAt(LocalDateTime.now())
                .build();
    }

    @Test
    void findByUserIdAndRecordId_ExistingReview_ReturnsReview() {
        // Arrange
        UserEntity user = createTestUser("test@example.com");
        RecordEntity vRecord = createTestRecord();
        entityManager.persist(user);
        entityManager.persist(vRecord);

        ReviewEntity review = createTestReview(user, vRecord);
        entityManager.persist(review);
        entityManager.flush();

        // Act
        Optional<ReviewEntity> result = reviewRepository.findByUserIdAndRecordId(
                user.getId(), vRecord.getId());

        // Assert
        assertTrue(result.isPresent());
        assertEquals(5, result.get().getRating());
    }

    @Test
    void findAllByRecordId_MultipleReviews_ReturnsAllReviews() {
        // Arrange
        UserEntity user1 = createTestUser("user1@test.com");
        UserEntity user2 = createTestUser("user2@test.com");
        RecordEntity vRecord = createTestRecord();
        entityManager.persist(user1);
        entityManager.persist(user2);
        entityManager.persist(vRecord);

        entityManager.persist(createTestReview(user1, vRecord));
        entityManager.persist(createTestReview(user2, vRecord));
        entityManager.flush();

        // Act
        List<ReviewEntity> reviews = reviewRepository.findAllByRecordId(vRecord.getId());

        // Assert
        assertEquals(2, reviews.size());
    }
    @Test
    void save_ReviewWithMaxRating_SavesSuccessfully() {
        // Arrange
        UserEntity user = createTestUser("test@example.com");
        RecordEntity vRecord = createTestRecord();
        entityManager.persist(user);
        entityManager.persist(vRecord);

        ReviewEntity review = createTestReview(user, vRecord);
        review.setRating(5);

        // Act
        ReviewEntity savedReview = reviewRepository.save(review);

        // Assert
        assertEquals(5, savedReview.getRating());
    }

    @Test
    void update_ReviewComment_UpdatesSuccessfully() {
        // Arrange
        UserEntity user = createTestUser("test@example.com");
        RecordEntity vRecord = createTestRecord();
        entityManager.persist(user);
        entityManager.persist(vRecord);

        ReviewEntity review = createTestReview(user, vRecord);
        entityManager.persist(review);
        entityManager.flush();

        // Act
        review.setComment("Updated comment");
        ReviewEntity updatedReview = reviewRepository.save(review);

        // Assert
        assertEquals("Updated comment", updatedReview.getComment());
    }

    @Test
    void save_MultipleReviewsSameUser_SavesAll() {
        // Arrange
        UserEntity user = createTestUser("test@example.com");
        RecordEntity record1 = createTestRecord();
        RecordEntity record2 = createTestRecord();
        entityManager.persist(user);
        entityManager.persist(record1);
        entityManager.persist(record2);

        ReviewEntity review1 = createTestReview(user, record1);
        ReviewEntity review2 = createTestReview(user, record2);

        // Act
        entityManager.persist(review1);
        entityManager.persist(review2);
        entityManager.flush();

        // Assert
        List<ReviewEntity> reviews = reviewRepository.findAllByRecordId(record1.getId());
        assertEquals(1, reviews.size());
    }

    @Test
    void findAllByRecordId_OrderByCreatedAt_ReturnsOrderedList() {
        // Arrange
        UserEntity user = createTestUser("test@example.com");
        RecordEntity vRecord = createTestRecord();
        entityManager.persist(user);
        entityManager.persist(vRecord);

        ReviewEntity oldReview = createTestReview(user, vRecord);
        oldReview.setCreatedAt(LocalDateTime.now().minusDays(1));

        ReviewEntity newReview = createTestReview(user, vRecord);
        newReview.setCreatedAt(LocalDateTime.now());

        entityManager.persist(oldReview);
        entityManager.persist(newReview);
        entityManager.flush();

        // Act
        List<ReviewEntity> reviews = reviewRepository.findAllByRecordId(vRecord.getId());

        // Assert
        assertTrue(reviews.get(0).getCreatedAt().isBefore(reviews.get(1).getCreatedAt()));
    }

}