package org.myexample.spinningmotion.business.exception;

public class OldPasswordException extends RuntimeException{
    public OldPasswordException() {
        super("This is your latest password. Try something new");
    }
}
