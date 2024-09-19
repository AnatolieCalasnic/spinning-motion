package org.myexample.spinningmotion.business.exception;

public class UsernameAlreadyExistsException extends RuntimeException {
    public UsernameAlreadyExistsException() {
        super("It appears a user with this username already exists.");
    }
}
