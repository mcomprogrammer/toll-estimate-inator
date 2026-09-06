package com.pranav.toll.api;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

/** Request contract for a toll-plaza lookup. */
public record TollPlazaRequest(
        @NotBlank(message = "sourcePincode is required")
        @Pattern(regexp = "^[1-9][0-9]{5}$", message = "sourcePincode must be a six-digit Indian pincode")
        String sourcePincode,
        @NotBlank(message = "destinationPincode is required")
        @Pattern(regexp = "^[1-9][0-9]{5}$", message = "destinationPincode must be a six-digit Indian pincode")
        String destinationPincode) {
}
