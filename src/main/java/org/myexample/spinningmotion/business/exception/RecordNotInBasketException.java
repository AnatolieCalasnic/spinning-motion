package org.myexample.spinningmotion.business.exception;

public class RecordNotInBasketException extends RuntimeException {
    public RecordNotInBasketException(Long recordId, Long userId) {
        super("Record with ID " + recordId + " is not in the basket of user " + userId);
    }
}
