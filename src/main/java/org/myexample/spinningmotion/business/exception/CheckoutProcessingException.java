package org.myexample.spinningmotion.business.exception;

public class CheckoutProcessingException extends RuntimeException {
    private final CheckoutErrorType errorType;

    // Default constructors use PROCESSING_ERROR type
    public CheckoutProcessingException(String message) {
        super(message);
        this.errorType = CheckoutErrorType.PROCESSING_ERROR;
    }

    public CheckoutProcessingException(String message, Throwable cause) {
        super(message, cause);
        this.errorType = CheckoutErrorType.PROCESSING_ERROR;
    }

    public CheckoutProcessingException(String message, CheckoutErrorType errorType) {
        super(message);
        this.errorType = errorType;
    }

    public CheckoutProcessingException(String message, CheckoutErrorType errorType, Throwable cause) {
        super(message, cause);
        this.errorType = errorType;
    }

    public CheckoutErrorType getErrorType() {
        return errorType;
    }

    public enum CheckoutErrorType {
        INVALID_PAYLOAD,
        INVALID_SESSION,
        INVALID_METADATA,
        INVALID_USER,
        INVALID_ITEMS,
        PROCESSING_ERROR,
        INSUFFICIENT_INVENTORY
    }
}