package org.myexample.spinningmotion.business.exception;

public class StripeProcessingException extends RuntimeException {
    public StripeProcessingException(String message, Throwable cause) {
        super(message, cause);
    }
}