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
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.myexample.spinningmotion.business.exception.*;
import org.myexample.spinningmotion.business.interfc.*;
import org.myexample.spinningmotion.domain.coupon.GenerateCouponRequest;
import org.myexample.spinningmotion.domain.guest_user.GuestDetails;
import org.myexample.spinningmotion.domain.purchase_history.CreatePurchaseHistoryRequest;
import org.myexample.spinningmotion.domain.record.GetRecordRequest;
import org.myexample.spinningmotion.domain.record.GetRecordResponse;
import org.myexample.spinningmotion.domain.record.UpdateRecordRequest;
import org.myexample.spinningmotion.domain.stripe.CheckoutRequest;
import org.myexample.spinningmotion.domain.stripe.CheckoutResponse;
import org.myexample.spinningmotion.domain.user.GetUserRequest;
import org.myexample.spinningmotion.domain.user.GetUserResponse;
import org.myexample.spinningmotion.persistence.GuestOrderRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.retry.annotation.Retryable;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.EnableRetry;

import java.util.*;

@EnableRetry

@Service
@Slf4j
@Profile("!test")
@RequiredArgsConstructor
public class StripeUseCaseImpl implements StripeUseCase {
    private static final String IS_GUEST = "isGuest";
    private static final String USER_ID = "userId";
    private static final String GUEST_DETAILS = "guestDetails";
    private static final String ITEMS = "items";
    private static final String COUPON_CODE = "couponCode";
    private static final String COUPON_DISCOUNT = "couponDiscount";
    private static final String OBJECT = "object";
    private static final String METADATA = "metadata";
    private static final String DATA = "data";
    private String stripeSecretKey;
    private final CouponUseCase couponUseCase;
    private final PurchaseHistoryUseCase purchaseHistoryUseCase;
    private final RecordUseCase recordUseCase;
    private final GuestOrderRepository guestOrderRepository;
    private final EmailUseCase emailUseCase;
    private final UserUseCase userUseCase;
    private final ObjectMapper objectMapper = new ObjectMapper();
    @Value("${stripe.secret.key}")
    public void setStripeSecretKey(String key) {
        this.stripeSecretKey = key;
    }

    @Value("${stripe.webhook.secret}")
    private String webhookSecret;
    private record ValidationContext(CheckoutRequest request, String origin) {}

    private void validateCheckoutRequest(CheckoutRequest request, String origin) {
        ValidationContext context = new ValidationContext(request, origin);
        validateBasicRequirements(context);
        validateItems(request.getItems());
        validateCoupon(request.getCoupon());
    }

    private void validateBasicRequirements(ValidationContext context) {
        if (context.request() == null) {
            throw new InvalidInputException("CheckoutRequest cannot be null");
        }
        if (context.origin() == null || context.origin().trim().isEmpty()) {
            throw new InvalidInputException("Origin cannot be null or empty");
        }
        if (context.request().getItems() == null || context.request().getItems().isEmpty()) {
            throw new InvalidInputException("At least one item must be present in the request");
        }
    }

    private void validateItems(List<CheckoutRequest.Item> items) {
        for (CheckoutRequest.Item item : items) {
            validateSingleItem(item);
        }
    }

    private void validateSingleItem(CheckoutRequest.Item item) {
        if (item.getTitle() == null || item.getTitle().trim().isEmpty()) {
            throw new InvalidInputException("Item title cannot be null or empty");
        }
        if (item.getPrice() <= 0) {
            throw new InvalidInputException("Item price must be greater than zero");
        }
        if (item.getQuantity() <= 0) {
            throw new InvalidInputException("Item quantity must be greater than zero");
        }
    }

    private void validateCoupon(CheckoutRequest.CouponInfo coupon) {
        if (coupon == null) {
            return;
        }

        if (coupon.getCode() == null || coupon.getCode().trim().isEmpty()) {
            throw new InvalidInputException("Coupon code cannot be null or empty");
        }
        if (coupon.getDiscountPercentage() < 0 || coupon.getDiscountPercentage() > 100) {
            throw new InvalidInputException("Coupon discount must be between 0 and 100");
        }
    }

    private void validateProcessCheckoutRequest(String payload) throws PurchaseProcessingException {
        try {
            JsonNode payloadNode = parseAndValidatePayload(payload);
            JsonNode sessionNode = payloadNode.get(DATA).get(OBJECT);
            Map<String, String> metadata = validateAndExtractMetadata(sessionNode);
            validateCheckoutMetadata(metadata);
        } catch (JsonProcessingException e) {
            throw new PurchaseProcessingException("Invalid payload format.", e);
        }
    }

    private JsonNode parseAndValidatePayload(String payload) throws JsonProcessingException, PurchaseProcessingException {
        JsonNode payloadNode = objectMapper.readTree(payload);
        if (!payloadNode.has(DATA) || !payloadNode.get(DATA).has(OBJECT)) {
            throw new PurchaseProcessingException("Invalid payload: Missing 'data' or 'object' structure.");
        }
        return payloadNode;
    }

    private Map<String, String> validateAndExtractMetadata(JsonNode sessionNode) throws PurchaseProcessingException {
        if (!sessionNode.has(METADATA)) {
            throw new PurchaseProcessingException("Invalid session: Metadata is missing.");
        }

        JsonNode metadataNode = sessionNode.get(METADATA);
        Map<String, String> metadata = objectMapper.convertValue(metadataNode, new TypeReference<>() {});

        if (metadata.isEmpty()) {
            throw new PurchaseProcessingException("Invalid session: Metadata is empty.");
        }

        return metadata;
    }

    private void validateCheckoutMetadata(Map<String, String> metadata) throws JsonProcessingException, PurchaseProcessingException {
        validateUserInformation(metadata);
        validateItemsInformation(metadata);
        validateGuestInformation(metadata);
        validateCouponInformation(metadata);
    }

    private void validateUserInformation(Map<String, String> metadata) throws PurchaseProcessingException {
        boolean isGuest = parseIsGuest(metadata);
        if (!isGuest && (!metadata.containsKey(USER_ID) || metadata.get(USER_ID).isEmpty())) {
            throw new PurchaseProcessingException("Invalid session: User ID is missing for non-guest checkout.");
        }
    }

    private void validateItemsInformation(Map<String, String> metadata) throws JsonProcessingException, PurchaseProcessingException {
        if (!metadata.containsKey(ITEMS)) {
            throw new PurchaseProcessingException("Invalid session: Items are missing.");
        }

        List<CheckoutRequest.Item> items = objectMapper.readValue(
                metadata.get(ITEMS),
                new TypeReference<>() {}
        );

        if (items.isEmpty()) {
            throw new PurchaseProcessingException("Invalid session: No items found.");
        }

        for (CheckoutRequest.Item item : items) {
            validateItemDetails(item);
        }
    }

    private void validateItemDetails(CheckoutRequest.Item item) throws PurchaseProcessingException {
        if (item.getRecordId() == null || item.getRecordId() <= 0) {
            throw new PurchaseProcessingException("Invalid item: Record ID is missing or invalid.");
        }
        if (item.getPrice() <= 0) {
            throw new PurchaseProcessingException("Invalid item: Price must be greater than zero.");
        }
        if (item.getQuantity() <= 0) {
            throw new PurchaseProcessingException("Invalid item: Quantity must be greater than zero.");
        }
    }

    private void validateGuestInformation(Map<String, String> metadata) throws JsonProcessingException, PurchaseProcessingException {
        if (parseIsGuest(metadata) && metadata.containsKey(GUEST_DETAILS)) {
            GuestDetails guestDetails = objectMapper.readValue(metadata.get(GUEST_DETAILS), GuestDetails.class);
            if (guestDetails.getEmail() == null || guestDetails.getEmail().isEmpty()) {
                throw new PurchaseProcessingException("Invalid guest details: Email is missing.");
            }
        }
    }

    private void validateCouponInformation(Map<String, String> metadata) throws PurchaseProcessingException {
        if (metadata.containsKey(COUPON_CODE)) {
            String couponCode = metadata.get(COUPON_CODE);
            Integer couponDiscount = metadata.containsKey(COUPON_DISCOUNT)
                    ? Integer.parseInt(metadata.get(COUPON_DISCOUNT))
                    : null;

            if (couponDiscount != null && (couponDiscount < 0 || couponDiscount > 100)) {
                throw new PurchaseProcessingException("Invalid coupon discount: Must be between 0 and 100.");
            }
            if (!couponUseCase.validateCoupon(couponCode)) {
                throw new PurchaseProcessingException("Invalid coupon: The coupon code is not valid.");
            }
        }
    }


    @Override
    public CheckoutResponse createCheckoutSession(CheckoutRequest request, String origin) {
        validateCheckoutRequest(request, origin);
        try {
            synchronized (StripeUseCaseImpl.class) {
                Stripe.apiKey = stripeSecretKey;
            }
            List<SessionCreateParams.LineItem> lineItems = createLineItems(request);

            boolean couponApplied = false;
            if (request.getCoupon() != null) {
                String couponCode = request.getCoupon().getCode();
                if (couponUseCase.validateCoupon(couponCode)) {
                    // Adjust the amount based on the coupon discount
                    int discountPercentage = request.getCoupon().getDiscountPercentage();

                    // Modify the line items to apply discount
                    lineItems = lineItems.stream()
                            .map(item -> {
                                long originalAmount = item.getPriceData().getUnitAmount();
                                long discountedAmount = originalAmount - (originalAmount * discountPercentage / 100);

                                return SessionCreateParams.LineItem.builder()
                                        .setPriceData(
                                                SessionCreateParams.LineItem.PriceData.builder()
                                                        .setCurrency(item.getPriceData().getCurrency())
                                                        .setUnitAmount(discountedAmount)
                                                        .setProductData(item.getPriceData().getProductData())
                                                        .build()
                                        )
                                        .setQuantity(item.getQuantity())
                                        .build();
                            })
                            .toList();

                    couponApplied = true;
                }

            }
            SessionCreateParams.Builder paramsBuilder = SessionCreateParams.builder()
                    .setMode(SessionCreateParams.Mode.PAYMENT)
                    .setUiMode(SessionCreateParams.UiMode.EMBEDDED)
                    .setReturnUrl(origin + "/success?session_id={CHECKOUT_SESSION_ID}")
                    .addAllLineItem(lineItems);
            // Store comprehensive metadata with explicit user status
            String itemsJson = objectMapper.writeValueAsString(request.getItems());
            paramsBuilder.putMetadata(ITEMS, itemsJson);

            // Enhanced user metadata handling
            boolean isGuest = Boolean.parseBoolean(request.getMetadata().getOrDefault(IS_GUEST, "true"));
            if  (!isGuest && request.getMetadata().get(USER_ID) != null) {
                String userId = request.getMetadata().get(USER_ID);
                if (userId != null && !userId.isEmpty()) {
                    paramsBuilder.putMetadata(USER_ID, userId);
                    paramsBuilder.putMetadata(IS_GUEST, "false");
                    log.info("Processing as registered user with ID: {}", userId);
                }
            } else {
                paramsBuilder.putMetadata(IS_GUEST, "true");
                log.info("Processing as guest user");
            }

            if (couponApplied && request.getCoupon() != null) {
                paramsBuilder.putMetadata(COUPON_CODE, request.getCoupon().getCode());
                paramsBuilder.putMetadata(COUPON_DISCOUNT,
                        String.valueOf(request.getCoupon().getDiscountPercentage()));
            }

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
    @Transactional
    public ResponseEntity<String> handleWebhook(String payload, String sigHeader) {
        try {
            Event event = Webhook.constructEvent(payload, sigHeader, webhookSecret);
            log.info("Webhook event type: {}", event.getType());

            if ("checkout.session.completed".equals(event.getType())) {
                return processCheckoutSessionWithRetry(payload);
            }

            return ResponseEntity.ok("Webhook processed successfully");
        } catch (Exception e) {
            log.error("Webhook processing error", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Webhook processing failed: " + e.getMessage());
        }
    }
    @Retryable(maxAttempts = 3, backoff = @Backoff(delay = 1000))
    private ResponseEntity<String> processCheckoutSessionWithRetry(String payload) {
        try {
            processCheckoutSession(payload);
            return ResponseEntity.ok("Checkout session processed successfully");
        } catch (Exception e) {
            log.error("Failed to process checkout session after retries", e);
            throw new PurchaseProcessingException("Failed to process checkout session", e);
        }
    }

    private void processCheckoutSession(String payload) {
        try {
            validateProcessCheckoutRequest(payload);
            JsonNode sessionNode = extractSessionNode(payload);
            Map<String, String> metadata = parseMetadata(sessionNode);
            logMetadataDetails(metadata);

            List<CheckoutRequest.Item> items = parseItems(sessionNode);
            boolean isGuest = parseIsGuest(metadata);
            Long userId = parseUserId(metadata, isGuest);
            String orderNumber = generateOrderNumber();

            items = processCouponAndDiscount(metadata, items);
            double totalOrderAmount = calculateTotalAmount(items);

            processAllItems(items, userId, isGuest, metadata);
            String recipientEmail = getRecipientEmail(isGuest, userId, metadata);

            sendOrderConfirmationWithRetry(recipientEmail, items, totalOrderAmount, orderNumber);
            handleFrequentShopperCoupon(userId);
        } catch (JsonProcessingException e) {
            throw new CheckoutProcessingException(
                    "Failed to process checkout payload: " + e.getMessage(),
                    CheckoutProcessingException.CheckoutErrorType.INVALID_PAYLOAD,
                    e
            );
        } catch (InsufficientQuantityException e) {
            throw new CheckoutProcessingException(
                    "Insufficient inventory during checkout: " + e.getMessage(),
                    CheckoutProcessingException.CheckoutErrorType.INSUFFICIENT_INVENTORY,
                    e
            );
        } catch (InvalidInputException e) {
            throw new CheckoutProcessingException(
                    "Invalid checkout input: " + e.getMessage(),
                    CheckoutProcessingException.CheckoutErrorType.INVALID_METADATA,
                    e
            );
        } catch (Exception e) {
            throw new CheckoutProcessingException(
                    "Unexpected error during checkout processing: " + e.getMessage(),
                    CheckoutProcessingException.CheckoutErrorType.PROCESSING_ERROR,
                    e
            );
        }
    }

    private JsonNode extractSessionNode(String payload) throws JsonProcessingException {
        JsonNode jsonNode = objectMapper.readTree(payload);
        JsonNode sessionNode = jsonNode.path(DATA).path(OBJECT);
        log.debug("Processing session node: {}", sessionNode);
        return sessionNode;
    }

    private void logMetadataDetails(Map<String, String> metadata) {
        log.debug("Parsed metadata: {}", metadata);
        if (metadata.containsKey(GUEST_DETAILS)) {
            log.debug("Raw guest details from metadata: {}", metadata.get(GUEST_DETAILS));
        }
    }

    private List<CheckoutRequest.Item> processCouponAndDiscount(Map<String, String> metadata,
                                                                List<CheckoutRequest.Item> items) {
        String couponCode = metadata.get(COUPON_CODE);
        Integer couponDiscount = metadata.containsKey(COUPON_DISCOUNT)
                ? Integer.parseInt(metadata.get(COUPON_DISCOUNT))
                : null;

        if (couponCode != null && couponDiscount != null) {
            log.info("Coupon applied - Code: {}, Discount: {}%", couponCode, couponDiscount);
            couponUseCase.markCouponAsUsed(couponCode);
        }

        return applyDiscount(items, couponDiscount);
    }

    private List<CheckoutRequest.Item> applyDiscount(List<CheckoutRequest.Item> items, Integer couponDiscount) {
        if (couponDiscount != null && couponDiscount > 0) {
            return items.stream()
                    .map(item -> {
                        double originalPrice = item.getPrice();
                        double discountedPrice = originalPrice * (1 - (couponDiscount / 100.0));
                        item.setDiscountedPrice(discountedPrice);
                        return item;
                    })
                    .toList();
        }
        return items;
    }

    private double calculateTotalAmount(List<CheckoutRequest.Item> items) {
        return items.stream()
                .mapToDouble(item -> (item.getDiscountedPrice() != null
                        ? item.getDiscountedPrice()
                        : item.getPrice()) * item.getQuantity())
                .sum();
    }

    private void processAllItems(List<CheckoutRequest.Item> items, Long userId,
                                 boolean isGuest, Map<String, String> metadata) {
        for (CheckoutRequest.Item item : items) {
            processPurchaseItem(item, userId, isGuest, metadata);
        }
    }

    private String getRecipientEmail(boolean isGuest, Long userId, Map<String, String> metadata)
            throws JsonProcessingException {
        if (isGuest) {
            GuestDetails guestDetails = objectMapper.readValue(
                    metadata.get(GUEST_DETAILS), GuestDetails.class);
            return guestDetails.getEmail();
        } else {
            GetUserResponse user = userUseCase.getUser(new GetUserRequest(userId));
            return user.getEmail();
        }
    }

    private void handleFrequentShopperCoupon(Long userId) {
        if (userId != null) {
            try {
                GenerateCouponRequest couponRequest = GenerateCouponRequest.builder()
                        .userId(userId)
                        .timeFrameDays(30L)
                        .requiredPurchases(3)
                        .discountPercentage(30)
                        .build();

                if (couponUseCase.generateFrequentShopperCoupon(couponRequest)) {
                    log.info("Generated new coupon for user {}", userId);
                }
            } catch (Exception e) {
                log.error("Failed to generate coupon for user {}", userId, e);
            }
        }
    }

    private String generateOrderNumber() {
        return "ORD-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    private Map<String, String> parseMetadata(JsonNode sessionNode) {
        JsonNode metadataNode = sessionNode.path(METADATA);
        Map<String, String> metadata = new HashMap<>();
        metadataNode.fields().forEachRemaining(entry ->
                metadata.put(entry.getKey(), entry.getValue().asText())
        );
        return metadata;
    }

    private List<CheckoutRequest.Item> parseItems(JsonNode sessionNode) throws JsonProcessingException {
        String itemsJson = sessionNode.path(METADATA).path(ITEMS).asText();
        return objectMapper.readValue(
                itemsJson,
                new TypeReference<List<CheckoutRequest.Item>>() {}
        );
    }

    private boolean parseIsGuest(Map<String, String> metadata) {
        String isGuestValue = metadata.get(IS_GUEST);
        if (isGuestValue == null) {
            return true; // Default to guest if no value
        }

        // Check for various "false" conditions
        return !("false".equalsIgnoreCase(isGuestValue) ||
                "0".equals(isGuestValue) ||
                "no".equalsIgnoreCase(isGuestValue));
    }

    private Long parseUserId(Map<String, String> metadata, boolean isGuest) {
        if (!isGuest) {
            String userIdStr = metadata.get(USER_ID);
            if (userIdStr != null && !userIdStr.isEmpty()) {
                try {
                    Long userId = Long.parseLong(userIdStr);
                    log.debug("Parsed user ID to long: {}", userId);

                    // Verify user exists
                    GetUserResponse user = userUseCase.getUser(new GetUserRequest(userId));
                    if (user != null && user.getEmail() != null) {
                        log.info("Verified user ID: {}", userId);
                        return userId;
                    }else {
                        log.warn("User found but missing email for ID: {}", userId);
                        throw new PurchaseProcessingException("Invalid user: Missing email");
                    }
                } catch (NumberFormatException e) {
                    log.error("Failed to parse user ID: {}", userIdStr);
                    throw new PurchaseProcessingException("Invalid user ID format");
                } catch (Exception e) {
                    log.error("Error verifying user ID: {}", userIdStr, e);
                    throw new PurchaseProcessingException("Failed to verify user");
                }
            }
            // If it gets here with !isGuest but no valid userId, it's an error
            throw new PurchaseProcessingException("User ID required for registered user checkout");

        }
        log.info("No valid user ID found, processing as guest");
        return null;
    }

    // New method for retryable emailtest sending
    @Retryable(maxAttempts = 3, backoff = @Backoff(delay = 1000))
    private void sendOrderConfirmationWithRetry(String email, List<CheckoutRequest.Item> items,
                                                double totalAmount, String orderNumber) {
        try {
            emailUseCase.sendOrderConfirmation(email, items, totalAmount, orderNumber);
            log.info("Successfully sent order confirmation emailtest to: {}", email);
        } catch (Exception e) {
            log.error("Failed to send order confirmation emailtest to: {}", email, e);
            throw e; // Retry will be triggered
        }
    }
    private void processPurchaseItem(CheckoutRequest.Item item, Long userId, boolean isGuest, Map<String, String> metadata) {
        try {
            GetRecordResponse recordResponse = recordUseCase.getRecord(new GetRecordRequest(item.getRecordId()));
            if (recordResponse.getQuantity() < item.getQuantity()) {
                throw new InsufficientQuantityException(
                        "Insufficient quantity for record: " + item.getTitle()
                );
            }
            double finalPrice = item.getPrice();
            if (metadata.containsKey(COUPON_DISCOUNT)) {
                Integer couponDiscount = Integer.parseInt(metadata.get(COUPON_DISCOUNT));
                if (couponDiscount > 0) {
                    finalPrice = finalPrice * (1 - (couponDiscount / 100.0));
                }
            }


            // Create purchase history first
            CreatePurchaseHistoryRequest purchaseRequest = CreatePurchaseHistoryRequest.builder()
                    .userId(userId)
                    .isGuest(isGuest)
                    .recordId(item.getRecordId())
                    .quantity(item.getQuantity())
                    .price(finalPrice)
                    .totalAmount(finalPrice * item.getQuantity())
                    .discountPercentage(metadata.containsKey(COUPON_DISCOUNT) ?
                            Integer.parseInt(metadata.get(COUPON_DISCOUNT)) : null)
                    .build();

            purchaseHistoryUseCase.createPurchaseHistory(purchaseRequest);


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
        synchronized (StripeUseCaseImpl.class) {
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
}