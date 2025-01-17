package org.myexample.spinningmotion.business.impl.usertest;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.myexample.spinningmotion.business.exception.InvalidInputException;
import org.myexample.spinningmotion.business.impl.user.GuestUseCaseImpl;
import org.myexample.spinningmotion.business.interfc.GuestUseCase;
import org.myexample.spinningmotion.domain.guest_user.GuestDetails;
import org.myexample.spinningmotion.persistence.GuestOrderRepository;
import org.myexample.spinningmotion.persistence.PurchaseHistoryRepository;
import org.myexample.spinningmotion.persistence.entity.GuestDetailsEntity;
import org.myexample.spinningmotion.persistence.entity.PurchaseHistoryEntity;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
        import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GuestUseCaseImplTest {

    @Mock
    private GuestOrderRepository guestOrderRepository;

    @Mock
    private PurchaseHistoryRepository purchaseHistoryRepository;

    private GuestUseCase guestUseCase;

    private GuestDetails validGuestDetails;
    private GuestDetailsEntity validGuestDetailsEntity;

    @BeforeEach
    void setUp() {
        guestUseCase = new GuestUseCaseImpl(guestOrderRepository, purchaseHistoryRepository);

        // Set up valid guest details for testing
        validGuestDetails = GuestDetails.builder()
                .purchaseHistoryId(1L)
                .fname("John")
                .lname("Doe")
                .email("john.doe@example.com")
                .address("address")
                .postalCode("12345")
                .country("NL")
                .city("Eindhoven")
                .region("NB")
                .phonenum("2141412412")
                .build();

        validGuestDetailsEntity = GuestDetailsEntity.builder()
                .purchaseHistoryId(1L)
                .fname("John")
                .lname("Doe")
                .email("john.doe@example.com")
                .address("address")
                .postalCode("12345")
                .country("NL")
                .city("Eindhoven")
                .region("NB")
                .phonenum("2141412412")
                .build();

    }

    @Test
    void createGuestOrder_ValidDetails_ShouldSucceed() {
        // Arrange
        when(purchaseHistoryRepository.findById(1L)).thenReturn(Optional.of(new PurchaseHistoryEntity()));
        when(guestOrderRepository.save(any(GuestDetailsEntity.class))).thenReturn(validGuestDetailsEntity);

        // Act
        GuestDetailsEntity result = guestUseCase.createGuestOrder(validGuestDetails);

        // Assert
        assertNotNull(result);
        assertEquals(validGuestDetails.getEmail(), result.getEmail());
        assertEquals(validGuestDetails.getFname(), result.getFname());
    }

    @Test
    void createGuestOrder_InvalidPurchaseId_ShouldThrowException() {
        // Arrange
        when(purchaseHistoryRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(InvalidInputException.class, () ->
                guestUseCase.createGuestOrder(validGuestDetails)
        );
    }

    @Test
    void createGuestOrder_InvalidEmail_ShouldThrowException() {
        // Arrange
        GuestDetails invalidDetails = GuestDetails.builder()
                .purchaseHistoryId(1L)
                .fname("John")
                .lname("Doe")
                .email("johnexampleom")
                .address("address")
                .postalCode("12345")
                .country("NL")
                .city("Eindhoven")
                .region("NB")
                .phonenum("2141412412")
                .build();

        // Act & Assert
        InvalidInputException exception = assertThrows(InvalidInputException.class,
                () -> guestUseCase.createGuestOrder(invalidDetails));
        assertEquals("Invalid email format", exception.getMessage());
    }

    @Test
    void createGuestOrder_InvalidPhone_ShouldThrowException() {
        // Arrange
        GuestDetails invalidDetails = GuestDetails.builder()
                .purchaseHistoryId(1L)
                .fname("John")
                .lname("Doe")
                .email("john.doe@example.com")
                .address("address")
                .postalCode("12345")
                .country("NL")
                .city("Eindhoven")
                .region("NB")
                .phonenum("z")
                .build();

        // Act & Assert
        InvalidInputException exception = assertThrows(InvalidInputException.class,
                () -> guestUseCase.createGuestOrder(invalidDetails));
        assertEquals("Invalid phone number format", exception.getMessage());
    }

    @Test
    void getGuestOrderByPurchaseId_ExistingOrder_ShouldReturnOrder() {
        // Arrange
        when(guestOrderRepository.findFirstByPurchaseHistoryId(1L)).thenReturn(validGuestDetailsEntity);

        // Act
        GuestDetailsEntity result = guestUseCase.getGuestOrderByPurchaseId(1L);

        // Assert
        assertNotNull(result);
        assertEquals(validGuestDetailsEntity.getEmail(), result.getEmail());
    }

    @Test
    void getGuestOrderByPurchaseId_NonExistingOrder_ShouldThrowException() {
        // Arrange
        when(guestOrderRepository.findFirstByPurchaseHistoryId(1L)).thenReturn(null);

        // Act & Assert
        assertThrows(InvalidInputException.class, () ->
                guestUseCase.getGuestOrderByPurchaseId(1L)
        );
    }

    @Test
    void getGuestOrdersByOrderId_ExistingOrders_ShouldReturnList() {
        // Arrange
        when(guestOrderRepository.findAllByPurchaseHistoryId(1L))
                .thenReturn(List.of(validGuestDetailsEntity));

        // Act
        List<GuestDetailsEntity> results = guestUseCase.getGuestOrdersByOrderId(1L);

        // Assert
        assertFalse(results.isEmpty());
        assertEquals(1, results.size());
        assertEquals(validGuestDetailsEntity.getEmail(), results.get(0).getEmail());
    }

    @Test
    void getGuestOrdersByOrderId_NoOrders_ShouldThrowException() {
        // Arrange
        when(guestOrderRepository.findAllByPurchaseHistoryId(1L))
                .thenReturn(Collections.emptyList());

        // Act & Assert
        assertThrows(InvalidInputException.class, () ->
                guestUseCase.getGuestOrdersByOrderId(1L)
        );
    }

    @Test
    void createGuestOrder_InvalidPostalCode_ShouldThrowException() {
        // Arrange
        GuestDetails invalidDetails = GuestDetails.builder()
                .purchaseHistoryId(1L)
                .fname("John")
                .lname("Doe")
                .email("john.doe@example.com")
                .address("address")
                .postalCode("@")
                .country("NL")
                .city("Eindhoven")
                .region("NB")
                .phonenum("2141412412")
                .build();

        // Act & Assert
        InvalidInputException exception = assertThrows(InvalidInputException.class,
                () -> guestUseCase.createGuestOrder(invalidDetails));
        assertEquals("Invalid postal code format", exception.getMessage());
    }

    @Test
    void createGuestOrder_EmptyRequiredField_ShouldThrowException() {
        // Arrange
        GuestDetails invalidDetails = GuestDetails.builder()
                .purchaseHistoryId(1L)
                .fname("John")
                .lname("Doe")
                .email("john.doe@example.com")
                .address("address")
                .postalCode("12345")
                .country("NL")
                .city("")
                .region("NB")
                .phonenum("2141412412")
                .build();

        // Act & Assert
        assertThrows(InvalidInputException.class, () ->
                guestUseCase.createGuestOrder(invalidDetails)
        );
    }
    @Test
    void createGuestOrder_InvalidFirstName_ShouldThrowException() {
        // Arrange
        GuestDetails invalidDetails = GuestDetails.builder()
                .purchaseHistoryId(validGuestDetails.getPurchaseHistoryId())
                .fname("1")  // Invalid - too short and contains numbers
                .lname(validGuestDetails.getLname())
                .email(validGuestDetails.getEmail())
                .phonenum(validGuestDetails.getPhonenum())
                .address(validGuestDetails.getAddress())
                .postalCode(validGuestDetails.getPostalCode())
                .country(validGuestDetails.getCountry())
                .city(validGuestDetails.getCity())
                .region(validGuestDetails.getRegion())
                .build();

        // Act & Assert
        InvalidInputException exception = assertThrows(InvalidInputException.class,
                () -> guestUseCase.createGuestOrder(invalidDetails));
        assertEquals("First name must be between 2 and 50 characters and contain only letters",
                exception.getMessage());
    }
    @Test
    void createGuestOrder_InvalidLastName_ShouldThrowException() {
        // Arrange
        GuestDetails invalidDetails = GuestDetails.builder()
                .purchaseHistoryId(validGuestDetails.getPurchaseHistoryId())
                .fname(validGuestDetails.getFname())
                .lname("123")  // Invalid - contains numbers
                .email(validGuestDetails.getEmail())
                .phonenum(validGuestDetails.getPhonenum())
                .address(validGuestDetails.getAddress())
                .postalCode(validGuestDetails.getPostalCode())
                .country(validGuestDetails.getCountry())
                .city(validGuestDetails.getCity())
                .region(validGuestDetails.getRegion())
                .build();

        // Act & Assert
        InvalidInputException exception = assertThrows(InvalidInputException.class,
                () -> guestUseCase.createGuestOrder(invalidDetails));
        assertEquals("Last name must be between 2 and 50 characters and contain only letters",
                exception.getMessage());
    }
    @Test
    void createGuestOrder_MissingAddress_ShouldThrowException() {
        // Arrange
        GuestDetails invalidDetails = GuestDetails.builder()
                .purchaseHistoryId(validGuestDetails.getPurchaseHistoryId())
                .fname(validGuestDetails.getFname())
                .lname(validGuestDetails.getLname())
                .email(validGuestDetails.getEmail())
                .phonenum(validGuestDetails.getPhonenum())
                .address("")  // Empty address
                .postalCode(validGuestDetails.getPostalCode())
                .country(validGuestDetails.getCountry())
                .city(validGuestDetails.getCity())
                .region(validGuestDetails.getRegion())
                .build();

        // Act & Assert
        InvalidInputException exception = assertThrows(InvalidInputException.class,
                () -> guestUseCase.createGuestOrder(invalidDetails));
        assertEquals("Address is required", exception.getMessage());
    }

    @Test
    void createGuestOrder_MissingCity_ShouldThrowException() {
        // Arrange
        GuestDetails invalidDetails = GuestDetails.builder()
                .purchaseHistoryId(validGuestDetails.getPurchaseHistoryId())
                .fname(validGuestDetails.getFname())
                .lname(validGuestDetails.getLname())
                .email(validGuestDetails.getEmail())
                .phonenum(validGuestDetails.getPhonenum())
                .address(validGuestDetails.getAddress())
                .postalCode(validGuestDetails.getPostalCode())
                .country(validGuestDetails.getCountry())
                .city("")  // Empty city
                .region(validGuestDetails.getRegion())
                .build();

        // Act & Assert
        InvalidInputException exception = assertThrows(InvalidInputException.class,
                () -> guestUseCase.createGuestOrder(invalidDetails));
        assertEquals("City is required", exception.getMessage());
    }

}