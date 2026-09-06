package com.pranav.toll.exception;

public class FeatureNotImplementedException extends RuntimeException {

    public FeatureNotImplementedException() {
        super("Toll plaza lookup is not implemented in this scaffold");
    }
}
