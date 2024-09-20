package org.myexample.spinningmotion.business.exception;

public class OutOfStockException extends RuntimeException {
    public OutOfStockException(String title, int requested, int available) {
        super("We're sorry, the record '" + title + "' is currently out of stock. Requested: " + requested + ", Available: " + available);
    }
}

