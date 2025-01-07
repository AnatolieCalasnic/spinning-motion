package org.myexample.spinningmotion.business.impl;

import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.myexample.spinningmotion.business.exception.EmailSendingException;
import org.myexample.spinningmotion.business.impl.email_confirmation.EmailUseCaseImpl;
import org.myexample.spinningmotion.domain.stripe.CheckoutRequest;
import org.springframework.context.annotation.Profile;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmailUseCaseImplTest {
    @Mock
    private JavaMailSender mailSender;

    private EmailUseCaseImpl emailUseCase;

    private static final String FROM_EMAIL = "test@spinningmotion.com";
    private static final String TO_EMAIL = "test@example.com";
    private static final String ORDER_NUMBER = "ORD-12345678";

    @BeforeEach
    void setUp() {
        emailUseCase = new EmailUseCaseImpl(mailSender);
        ReflectionTestUtils.setField(emailUseCase, "fromEmail", FROM_EMAIL);
    }

    @Test
    void sendOrderConfirmation_SuccessfulSend() {
        // Arrange
        List<CheckoutRequest.Item> items = Arrays.asList(
                createTestItem(1L, "Test Record", "Test Artist", "New", 19.99, 1),
                createTestItem(2L, "Another Record", "Another Artist", "Used", 24.99, 2)
        );
        double totalAmount = 69.97;

        MimeMessage mimeMessage = mock(MimeMessage.class);
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);

        // Act
        assertDoesNotThrow(() ->
                emailUseCase.sendOrderConfirmation(TO_EMAIL, items, totalAmount, ORDER_NUMBER)
        );

        // Assert
        verify(mailSender).createMimeMessage();
        verify(mailSender).send(any(MimeMessage.class));
    }

    @Test
    void sendOrderConfirmation_HandlesException() {
        // Arrange
        List<CheckoutRequest.Item> items = Arrays.asList(
                createTestItem(1L, "Test Record", "Test Artist", "New", 19.99, 1)
        );
        MimeMessage mimeMessage = mock(MimeMessage.class);
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
        doThrow(new RuntimeException("Failed to send email"))
                .when(mailSender).send(any(MimeMessage.class));

        // Act & Assert
        assertThrows(EmailSendingException.class, () ->
                emailUseCase.sendOrderConfirmation(TO_EMAIL, items, 19.99, ORDER_NUMBER)
        );
    }

    private CheckoutRequest.Item createTestItem(Long recordId, String title, String artist, String condition, double price, int quantity) {
        return CheckoutRequest.Item.builder()
                .recordId(recordId)
                .title(title)
                .artist(artist)
                .condition(condition)
                .price(price)
                .quantity(quantity)
                .build();
    }

    @Test
    void verifyProfileAnnotation() {
        Profile profileAnnotation = EmailUseCaseImpl.class.getAnnotation(Profile.class);
        assertNotNull(profileAnnotation, "Class should have @Profile annotation");
        assertArrayEquals(new String[]{"!test"}, profileAnnotation.value(),
                "Profile should be set to '!test'");
    }

    @Test
    void verifyServiceAnnotation() {
        assertNotNull(EmailUseCaseImpl.class.getAnnotation(Service.class),
                "Class should have @Service annotation");
    }
}