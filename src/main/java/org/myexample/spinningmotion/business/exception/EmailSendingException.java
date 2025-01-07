package org.myexample.spinningmotion.business.exception;

public class EmailSendingException extends RuntimeException {
    public EmailSendingException(String message, String errorDetails) {
        super(message + ": " + errorDetails);
    }
}