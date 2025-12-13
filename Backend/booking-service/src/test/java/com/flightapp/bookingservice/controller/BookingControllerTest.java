package com.flightapp.bookingservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.flightapp.bookingservice.dto.request.BookingRequest;
import com.flightapp.bookingservice.entity.Booking;
import com.flightapp.bookingservice.enums.GENDER;
import com.flightapp.bookingservice.enums.MEAL;
import com.flightapp.bookingservice.exception.BookingInvalidException;
import com.flightapp.bookingservice.exception.BookingNotFoundException;
import com.flightapp.bookingservice.service.BookingService;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest
class BookingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper mapper;

    @MockBean
    private BookingService service;

    @Test
    void bookTicket_success_returnsCreated() throws Exception {
        BookingRequest req = new BookingRequest(
                10,
                "Hari",
                25,
                GENDER.MALE,
                MEAL.VEG,
                "hari@test.com",
                1
        );

        Booking booking = Booking.builder()
                .id(1)
                .flightId(10)
                .passengerName("Hari")
                .age(25)
                .gender(GENDER.MALE)
                .meal(MEAL.VEG)
                .email("hari@test.com")
                .numberOfTickets(1)
                .status("BOOKED")
                .pnr("PNR123")
                .build();

        when(service.bookTicket(any(BookingRequest.class))).thenReturn(booking);

        mockMvc.perform(post("/bookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.pnr").value("PNR123"));
    }

    @Test
    void getByEmail_returnsList() throws Exception {
        Booking b = new Booking();
        when(service.getBookingsByEmail("hari@test.com")).thenReturn(List.of(b));

        mockMvc.perform(get("/bookings/email/hari@test.com"))
                .andExpect(status().isOk());
    }

    @Test
    void getById_returnsBooking() throws Exception {
        Booking b = new Booking();
        when(service.getBookingById(1)).thenReturn(b);

        mockMvc.perform(get("/bookings/id/1"))
                .andExpect(status().isOk());
    }

    @Test
    void getByPnr_returnsBooking() throws Exception {
        Booking b = new Booking();
        when(service.getBookingByPnr("PNR123")).thenReturn(b);

        mockMvc.perform(get("/bookings/pnr/PNR123"))
                .andExpect(status().isOk());
    }

    @Test
    void cancelBooking_success() throws Exception {
        Booking b = new Booking(); // mock returned booking
        when(service.cancelBooking(1)).thenReturn(b);

        mockMvc.perform(delete("/bookings/1"))
                .andExpect(status().isOk())
                .andExpect(content().string("Booking cancelled successfully"));
    }

    // ✅ Exception tests for GlobalExceptionHandler

    @Test
    void getBookingById_notFound_returns404() throws Exception {
        when(service.getBookingById(1)).thenThrow(new BookingNotFoundException("Booking not found"));

        mockMvc.perform(get("/bookings/id/1"))
                .andExpect(status().isNotFound())
                .andExpect(content().string("Booking not found"));
    }

    @Test
    void cancelBooking_invalid_returns400() throws Exception {
        when(service.cancelBooking(1)).thenThrow(new BookingInvalidException("Cannot cancel"));

        mockMvc.perform(delete("/bookings/1"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Cannot cancel"));
    }
}
