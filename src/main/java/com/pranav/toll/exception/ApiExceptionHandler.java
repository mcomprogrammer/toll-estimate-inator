package com.pranav.toll.exception;

import com.pranav.toll.api.ApiError;
import com.pranav.toll.google.GoogleMapsException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(GoogleMapsException.class)
    public ResponseEntity<ApiError> handleGoogleFailure(GoogleMapsException exception) {
        return ResponseEntity.status(502).body(new ApiError(exception.getMessage()));
    }

    @ExceptionHandler(InvalidPincodeException.class)
    public ResponseEntity<ApiError> handleInvalidPincode(InvalidPincodeException exception) {
        return invalidPincodeResponse();
    }

    @ExceptionHandler(SamePincodeException.class)
    public ResponseEntity<ApiError> handleSamePincode(SamePincodeException exception) {
        return ResponseEntity.badRequest().body(new ApiError(exception.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleValidationFailure(MethodArgumentNotValidException exception) {
        return invalidPincodeResponse();
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiError> handleUnreadableRequest(HttpMessageNotReadableException exception) {
        return invalidPincodeResponse();
    }

    private ResponseEntity<ApiError> invalidPincodeResponse() {
        return ResponseEntity.badRequest().body(new ApiError("Invalid source or destination pincode"));
    }
}
