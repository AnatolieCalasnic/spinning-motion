package org.myexample.spinningmotion.business.exception;

public class EmailAlreadyExistsException extends RuntimeException {
    public EmailAlreadyExistsException() {
        super("It appears a user with this email already exists.");
    }
}
