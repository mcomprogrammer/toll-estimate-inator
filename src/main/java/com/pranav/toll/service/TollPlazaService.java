package com.pranav.toll.service;

import com.pranav.toll.api.TollPlazaRequest;
import com.pranav.toll.api.TollPlazaResponse;
import com.pranav.toll.exception.FeatureNotImplementedException;
import com.pranav.toll.exception.SamePincodeException;
import org.springframework.stereotype.Service;

@Service
public class TollPlazaService {

    public TollPlazaResponse findTollPlazas(TollPlazaRequest request) {
        if (request != null && java.util.Objects.equals(request.sourcePincode(), request.destinationPincode())) {
            throw new SamePincodeException();
        }
        throw new FeatureNotImplementedException();
    }
}
