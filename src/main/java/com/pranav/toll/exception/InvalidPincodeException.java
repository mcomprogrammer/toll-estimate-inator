package com.pranav.toll.exception;

public class InvalidPincodeException extends RuntimeException {

    public InvalidPincodeException() {
        super("Invalid source or destination pincode");
    }
}
