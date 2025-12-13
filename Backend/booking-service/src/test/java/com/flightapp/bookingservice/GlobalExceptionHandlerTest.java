package com.flightapp.bookingservice;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.flightapp.bookingservice.controller.BookingController;
import com.flightapp.bookingservice.dto.request.BookingRequest;
import com.flightapp.bookingservice.enums.GENDER;
import com.flightapp.bookingservice.enums.MEAL;
import com.flightapp.bookingservice.exception.*;
import com.flightapp.bookingservice.service.BookingService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(BookingController.class)
class GlobalExceptionHandlerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper mapper;

    @MockBean
    private BookingService service;

    @Test
    void handleValidationException_returnsBadRequest() throws Exception {
        // empty request triggers validation errors
        BookingRequest invalidReq = new BookingRequest();

        mockMvc.perform(post("/bookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(invalidReq)))
                .andExpect(status().isBadRequest())
                // just check that some validation message is present
                .andExpect(content().string(org.hamcrest.Matchers.containsString("required")));
    }

    @Test
    void handleBookingNotFoundException_returnsNotFound() throws Exception {
        when(service.getBookingById(999))
                .thenThrow(new BookingNotFoundException("Booking not found"));

        mockMvc.perform(get("/bookings/id/999"))
                .andExpect(status().isNotFound())
                .andExpect(content().string("Booking not found"));
    }

    @Test
    void handleBookingInvalidException_returnsBadRequest() throws Exception {
        BookingRequest validReq = new BookingRequest(
                1,
                "Alice",
                25,
                GENDER.FEMALE,
                MEAL.VEG,
                "alice@test.com",
                1
        );

        when(service.bookTicket(any()))
                .thenThrow(new BookingInvalidException("Invalid booking"));

        mockMvc.perform(post("/bookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(validReq)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Invalid booking"));
    }
}
