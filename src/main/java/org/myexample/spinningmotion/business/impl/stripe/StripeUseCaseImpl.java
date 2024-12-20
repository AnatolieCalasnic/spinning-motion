package org.myexample.spinningmotion.business.impl.stripe;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.Event;
import com.stripe.model.checkout.Session;
import com.stripe.net.Webhook;
import com.stripe.param.checkout.SessionCreateParams;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.myexample.spinningmotion.business.exception.InsufficientQuantityException;
import org.myexample.spinningmotion.business.exception.PurchaseProcessingException;
import org.myexample.spinningmotion.business.exception.StripeProcessingException;
import org.myexample.spinningmotion.business.interfc.PurchaseHistoryUseCase;
import org.myexample.spinningmotion.business.interfc.RecordUseCase;
import org.myexample.spinningmotion.business.interfc.StripeUseCase;
import org.myexample.spinningmotion.domain.guest_user.GuestDetails;
import org.myexample.spinningmotion.domain.purchase_history.CreatePurchaseHistoryRequest;
import org.myexample.spinningmotion.domain.purchase_history.CreatePurchaseHistoryResponse;
import org.myexample.spinningmotion.domain.record.GetRecordRequest;
import org.myexample.spinningmotion.domain.record.GetRecordResponse;
import org.myexample.spinningmotion.domain.record.UpdateRecordRequest;
import org.myexample.spinningmotion.domain.stripe.CheckoutRequest;
import org.myexample.spinningmotion.domain.stripe.CheckoutResponse;
import org.myexample.spinningmotion.persistence.GuestOrderRepository;
import org.myexample.spinningmotion.persistence.entity.GuestDetailsEntity;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
@Profile("!test")
@RequiredArgsConstructor
public class StripeUseCaseImpl implements StripeUseCase {
    private static final String IS_GUEST = "isGuest";
    private static final String USER_ID = "userId";
    private static final String GUEST_DETAILS = "guestDetails";
    private static final String ITEMS = "items";
    private static String stripeSecretKey;

    @Value("${stripe.secret.key}")
    public void setStripeSecretKey(String key) {
        this.stripeSecretKey = key;
    }

    @Value("${stripe.webhook.secret}")
    private String webhookSecret;

    private final PurchaseHistoryUseCase purchaseHistoryUseCase;
    private final RecordUseCase recordUseCase;
    private final GuestOrderRepository guestOrderRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public CheckoutResponse createCheckoutSession(CheckoutRequest request, String origin) {
        try {
            synchronized (StripeUseCaseImpl.class) {
                Stripe.apiKey = stripeSecretKey;
            }
            List<SessionCreateParams.LineItem> lineItems = createLineItems(request);
            log.info("Creating checkout session. Guest details: {}", request.getGuestDetails());

            SessionCreateParams.Builder paramsBuilder = SessionCreateParams.builder()
                    .setMode(SessionCreateParams.Mode.PAYMENT)
                    .setUiMode(SessionCreateParams.UiMode.EMBEDDED)
                    .setReturnUrl(origin + "/success?session_id={CHECKOUT_SESSION_ID}")
                    .addAllLineItem(lineItems);

            // Store comprehensive metadata
            String itemsJson = objectMapper.writeValueAsString(request.getItems());
            paramsBuilder.putMetadata(ITEMS, itemsJson);

            // Add user and guest metadata
            paramsBuilder.putMetadata(IS_GUEST,
                    request.getMetadata().getOrDefault(IS_GUEST, "true"));
            paramsBuilder.putMetadata(USER_ID,
                    request.getMetadata().getOrDefault(USER_ID, ""));

            if (request.getGuestDetails() != null) {
                String guestDetailsJson = objectMapper.writeValueAsString(request.getGuestDetails());
                log.info("Adding guest details to metadata: {}", guestDetailsJson);
                paramsBuilder.putMetadata(GUEST_DETAILS, guestDetailsJson);
            }

            Session session = Session.create(paramsBuilder.build());
            return new CheckoutResponse(session.getId(), session.getClientSecret());
        } catch (StripeException | JsonProcessingException e) {
            log.error("Error creating Stripe session", e);
            throw new StripeProcessingException("Payment session creation failed", e);
        }
    }

    private List<SessionCreateParams.LineItem> createLineItems(CheckoutRequest request) {
        return request.getItems().stream()
                .map(item -> {
                    SessionCreateParams.LineItem.PriceData priceData =
                            SessionCreateParams.LineItem.PriceData.builder()
                                    .setCurrency("eur")
                                    .setUnitAmount((long) (item.getPrice() * 100))
                                    .setProductData(
                                            SessionCreateParams.LineItem.PriceData.ProductData.builder()
                                                    .setName(item.getTitle())
                                                    .setDescription(item.getArtist() + " - " + item.getCondition())
                                                    .build()
                                    )
                                    .build();

                    return SessionCreateParams.LineItem.builder()
                            .setPriceData(priceData)
                            .setQuantity((long) item.getQuantity())
                            .build();
                })
                .toList();
    }

    @Override
    public ResponseEntity<String> handleWebhook(String payload, String sigHeader) {
        try {
            Event event = Webhook.constructEvent(payload, sigHeader, webhookSecret);
            log.info("Webhook event type: {}", event.getType());

            if ("checkout.session.completed".equals(event.getType())) {
                processCheckoutSession(payload);
                return ResponseEntity.ok("Checkout session processed successfully");
            }

            return ResponseEntity.ok("Webhook processed successfully");
        } catch (Exception e) {
            log.error("Webhook processing error", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Webhook processing failed: " + e.getMessage());
        }
    }

    private void processCheckoutSession(String payload) throws Exception {
        JsonNode jsonNode = objectMapper.readTree(payload);
        JsonNode sessionNode = jsonNode.path("data").path("object");

        Map<String, String> metadata = parseMetadata(sessionNode);
        log.info("Parsed metadata: {}", metadata);

        List<CheckoutRequest.Item> items = parseItems(sessionNode);
        log.info("Parsed items: {}", items);

        boolean isGuest = parseIsGuest(metadata);
        log.info("Is guest order: {}", isGuest);

        if (isGuest && metadata.containsKey(GUEST_DETAILS)) {
            log.info("Guest details found in metadata");
            String guestDetailsJson = metadata.get(GUEST_DETAILS);
            log.info("Guest details JSON: {}", guestDetailsJson);

            try {
                GuestDetails guestDetails = objectMapper.readValue(guestDetailsJson, GuestDetails.class);
                log.info("Successfully parsed guest details: {}", guestDetails);

                for (CheckoutRequest.Item item : items) {
                    // Create purchase history first
                    CreatePurchaseHistoryRequest purchaseRequest = CreatePurchaseHistoryRequest.builder()
                            .userId(null)
                            .isGuest(true)
                            .recordId(item.getRecordId())
                            .quantity(item.getQuantity())
                            .price(item.getPrice())
                            .totalAmount(item.getPrice() * item.getQuantity())
                            .build();

                    log.info("Creating purchase history for guest order: {}", purchaseRequest);
                    CreatePurchaseHistoryResponse purchaseResponse = purchaseHistoryUseCase.createPurchaseHistory(purchaseRequest);
                    log.info("Created purchase history with ID: {}", purchaseResponse.getId());

                    try {
                        GuestDetailsEntity guestOrder = GuestDetailsEntity.builder()
                                .purchaseHistoryId(purchaseResponse.getId())
                                .fname(guestDetails.getFname())
                                .lname(guestDetails.getLname())
                                .email(guestDetails.getEmail())
                                .address(guestDetails.getAddress())
                                .postalCode(guestDetails.getPostalCode())
                                .country(guestDetails.getCountry())
                                .city(guestDetails.getCity())
                                .region(guestDetails.getRegion())
                                .phonenum(guestDetails.getPhonenum())
                                .build();

                        log.info("Attempting to save guest order: {}", guestOrder);
                        GuestDetailsEntity savedGuest = guestOrderRepository.save(guestOrder);
                        log.info("Successfully saved guest order with ID: {}", savedGuest.getId());
                    } catch (Exception e) {
                        log.error("Failed to save guest order for purchase history ID: {}", purchaseResponse.getId(), e);
                        throw e;
                    }
                }
            } catch (Exception e) {
                log.error("Error processing guest details: {}", e.getMessage(), e);
                throw e;
            }
        } else {
            log.info("No guest details found in metadata or not a guest order");
        }

        // Process the purchase items
        Long userId = parseUserId(metadata, isGuest);
        for (CheckoutRequest.Item item : items) {
            try {
                processPurchaseItem(item, userId, isGuest);
            } catch (Exception e) {
                log.error("Failed to process item during checkout: {}", item, e);
                throw new PurchaseProcessingException("Purchase processing failed", e);
            }
        }
    }


    private Map<String, String> parseMetadata(JsonNode sessionNode) {
        JsonNode metadataNode = sessionNode.path("metadata");
        Map<String, String> metadata = new HashMap<>();
        metadataNode.fields().forEachRemaining(entry ->
                metadata.put(entry.getKey(), entry.getValue().asText())
        );
        return metadata;
    }

    private List<CheckoutRequest.Item> parseItems(JsonNode sessionNode) throws JsonProcessingException {
        String itemsJson = sessionNode.path("metadata").path(ITEMS).asText();
        return objectMapper.readValue(
                itemsJson,
                new TypeReference<List<CheckoutRequest.Item>>() {}
        );
    }

    private boolean parseIsGuest(Map<String, String> metadata) {
        return Optional.ofNullable(metadata.get(IS_GUEST))
                .map(Boolean::parseBoolean)
                .orElse(true);
    }

    private Long parseUserId(Map<String, String> metadata, boolean isGuest) {
        if (isGuest) return null;

        String userIdStr = metadata.get(USER_ID);
        try {
            return userIdStr != null && !userIdStr.isEmpty()
                    ? Long.parseLong(userIdStr)
                    : null;
        } catch (NumberFormatException e) {
            log.warn("Invalid userId in metadata: {}", userIdStr);
            return null;
        }
    }

    private void processPurchaseItem(CheckoutRequest.Item item, Long userId, boolean isGuest) {
        try {
            // Create purchase history first
            CreatePurchaseHistoryRequest purchaseRequest = CreatePurchaseHistoryRequest.builder()
                    .userId(userId)
                    .isGuest(isGuest)
                    .recordId(item.getRecordId())
                    .quantity(item.getQuantity())
                    .price(item.getPrice())
                    .totalAmount(item.getPrice() * item.getQuantity())
                    .build();

            purchaseHistoryUseCase.createPurchaseHistory(purchaseRequest);

            // Prepare update request with full record details
            GetRecordRequest getRecordRequest = new GetRecordRequest(item.getRecordId());
            GetRecordResponse recordResponse = recordUseCase.getRecord(getRecordRequest);

            // Check quantity before update
            if (recordResponse.getQuantity() < item.getQuantity()) {
                throw new InsufficientQuantityException(
                        "Insufficient quantity for record: " + item.getTitle()
                );
            }

            // Prepare update request with full details
            UpdateRecordRequest updateRequest = UpdateRecordRequest.builder()
                    .id(item.getRecordId())
                    .title(recordResponse.getTitle())
                    .artist(recordResponse.getArtist())
                    .condition(recordResponse.getCondition())
                    .price(recordResponse.getPrice())
                    .genreId(recordResponse.getGenreId())
                    .year(recordResponse.getYear())
                    .quantity(recordResponse.getQuantity() - item.getQuantity())
                    .build();


            recordUseCase.updateRecord(updateRequest);

            log.info("Processed purchase - Record: {}, Quantity: {}, User: {}, Guest: {}",
                    item.getTitle(), item.getQuantity(), userId, isGuest);

        } catch (Exception e) {
            log.error("Failed to process purchase item: {}", item, e);
            throw new PurchaseProcessingException("Purchase processing failed", e);
        }
    }

    @Override
    public boolean verifySession(String sessionId) {
        try {
            Stripe.apiKey = stripeSecretKey;
            Session session = Session.retrieve(sessionId);
            return "complete".equals(session.getStatus());
        } catch (StripeException e) {
            log.error("Session verification error", e);
            return false;
        }
    }
}