package com.pranav.toll.api;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class TollPlazaScaffoldTests {

    @Autowired
    private MockMvc mvc;

    @Test
    void assignmentEndpointReportsThatLookupIsNotImplemented() throws Exception {
        mvc.perform(post("/api/v1/toll-plazas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"sourcePincode":"110001","destinationPincode":"560001"}
                                """))
                .andExpect(status().isNotImplemented())
                .andExpect(content().json("""
                        {"error":"Toll plaza lookup is not implemented in this scaffold"}
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
