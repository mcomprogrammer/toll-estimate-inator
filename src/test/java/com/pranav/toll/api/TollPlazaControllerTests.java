package com.pranav.toll.api;

import com.pranav.toll.exception.InvalidPincodeException;
import com.pranav.toll.google.GoogleMapsClient;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import static org.mockito.Mockito.when;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class TollPlazaControllerTests {

    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private GoogleMapsClient googleMapsClient;

    @Test
    void unresolvedPincodeIsRejected() throws Exception {
        when(googleMapsClient.geocode("999999")).thenThrow(new InvalidPincodeException());

        mvc.perform(post("/api/v1/toll-plazas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"sourcePincode":"999999","destinationPincode":"411045"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(content().json("""
                        {"error":"Invalid source or destination pincode"}
                        """));
    }

    @Test
    void missingSourcePincodeIsRejected() throws Exception {
        mvc.perform(post("/api/v1/toll-plazas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"destinationPincode":"411045"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(content().json("""
                        {"error":"Invalid source or destination pincode"}
                        """));
    }

    @Test
    void missingDestinationPincodeIsRejected() throws Exception {
        mvc.perform(post("/api/v1/toll-plazas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"sourcePincode":"560064"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(content().json("""
                        {"error":"Invalid source or destination pincode"}
                        """));
    }

    @Test
    void malformedPincodeIsRejected() throws Exception {
        mvc.perform(post("/api/v1/toll-plazas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"sourcePincode":"012345","destinationPincode":"411045"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(content().json("""
                        {"error":"Invalid source or destination pincode"}
                        """));
    }

    @Test
    void identicalPincodesAreRejected() throws Exception {
        mvc.perform(post("/api/v1/toll-plazas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"sourcePincode":"560064","destinationPincode":"560064"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(content().json("""
                        {"error":"Source and destination pincodes cannot be the same"}
                        """));
    }

    @Test
    void malformedJsonIsRejected() throws Exception {
        mvc.perform(post("/api/v1/toll-plazas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"sourcePincode\":\"560064\""))
                .andExpect(status().isBadRequest())
                .andExpect(content().json("""
                        {"error":"Invalid source or destination pincode"}
                        """));
    }
}
