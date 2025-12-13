package com.flightapp.flightservice.controller;

import com.flightapp.flightservice.dto.request.AddFlightRequest;
import com.flightapp.flightservice.dto.request.SearchFlightRequest;
import com.flightapp.flightservice.dto.response.AddFlightResponse;
import com.flightapp.flightservice.dto.response.FlightResponse;
import com.flightapp.flightservice.exception.DuplicateFlightException;
import com.flightapp.flightservice.exception.FlightNotFoundException;
import com.flightapp.flightservice.service.FlightService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = FlightController.class)
class FlightControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private FlightService service;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void addFlight_success() throws Exception {
        AddFlightRequest req = new AddFlightRequest();
        req.setFlightNumber("F101");
        req.setFromCity("A");
        req.setToCity("B");
        req.setDepartureTime(LocalDateTime.now().plusDays(1));
        req.setArrivalTime(LocalDateTime.now().plusDays(1).plusHours(2));
        req.setCost(100f);
        req.setSeatsAvailable(50);

        when(service.addFlight(any(AddFlightRequest.class))).thenReturn(new AddFlightResponse(1));

        mockMvc.perform(post("/flights")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void addFlight_duplicateFlight_throws() throws Exception {
        AddFlightRequest req = new AddFlightRequest();
        req.setFlightNumber("F101");
        req.setFromCity("A");
        req.setToCity("B");
        req.setDepartureTime(LocalDateTime.now().plusDays(1));
        req.setArrivalTime(LocalDateTime.now().plusDays(1).plusHours(2));
        req.setCost(100f);
        req.setSeatsAvailable(50);

        when(service.addFlight(any(AddFlightRequest.class)))
                .thenThrow(new DuplicateFlightException("Flight already exists"));

        mockMvc.perform(post("/flights")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Flight already exists"));
    }

    @Test
    void getAllFlights_success() throws Exception {
        FlightResponse f = new FlightResponse();
        f.setId(1);
        f.setFlightNumber("F101");
        when(service.getAllFlights()).thenReturn(List.of(f));

        mockMvc.perform(get("/flights"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].flightNumber").value("F101"));
    }

    @Test
    void getAllFlights_emptyList() throws Exception {
        when(service.getAllFlights()).thenReturn(List.of());

        mockMvc.perform(get("/flights"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    void getFlightById_success() throws Exception {
        FlightResponse f = new FlightResponse();
        f.setId(1);
        f.setFlightNumber("F101");
        when(service.getFlightById(1)).thenReturn(f);

        mockMvc.perform(get("/flights/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.flightNumber").value("F101"));
    }

    @Test
    void getFlightById_notFound_throws() throws Exception {
        when(service.getFlightById(1)).thenThrow(new FlightNotFoundException("Flight Not Found"));

        mockMvc.perform(get("/flights/1"))
                .andExpect(status().isNotFound())
                .andExpect(content().string("Flight Not Found"));
    }

    @Test
    void searchFlight_success() throws Exception {
        // given
        SearchFlightRequest req = new SearchFlightRequest();
        req.setFromCity("A");
        req.setToCity("B");

        FlightResponse f = new FlightResponse();
        f.setId(1);
        f.setFlightNumber("F101");

        // service returns a LIST, not a single object
        List<FlightResponse> flights = Collections.singletonList(f);
        when(service.searchFlight(any(SearchFlightRequest.class))).thenReturn(flights);

        // when + then
        mockMvc.perform(post("/flights/search")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())                       // 200, not 201
                .andExpect(jsonPath("$[0].flightNumber").value("F101"))
                .andExpect(jsonPath("$[0].id").value(1));
    }

    @Test
    void searchFlight_notFound_throws() throws Exception {
        // given
        SearchFlightRequest req = new SearchFlightRequest();
        req.setFromCity("A");
        req.setToCity("B");

        when(service.searchFlight(any(SearchFlightRequest.class)))
                .thenThrow(new FlightNotFoundException("No flight found"));

        // when + then
        mockMvc.perform(post("/flights/search")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isNotFound())
                .andExpect(content().string("No flight found"));
    }


    @Test
    void addFlight_validationError() throws Exception {
        // Missing required fields
        AddFlightRequest req = new AddFlightRequest();

        mockMvc.perform(post("/flights")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.flightNumber").exists())
                .andExpect(jsonPath("$.fromCity").exists())
                .andExpect(jsonPath("$.toCity").exists());
    }
}
