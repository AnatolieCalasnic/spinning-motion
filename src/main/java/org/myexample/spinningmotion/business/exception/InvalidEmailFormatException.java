package org.myexample.spinningmotion.business.exception;

public class InvalidEmailFormatException extends RuntimeException{
    public InvalidEmailFormatException(){super("Your email contains invalid format");}
}
