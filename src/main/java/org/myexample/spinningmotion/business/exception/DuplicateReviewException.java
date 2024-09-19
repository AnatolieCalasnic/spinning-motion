package org.myexample.spinningmotion.business.exception;

public class DuplicateReviewException extends RuntimeException {
    public DuplicateReviewException() {
        super("You've already reviewed this record. You can edit your existing review instead.");
    }
}
