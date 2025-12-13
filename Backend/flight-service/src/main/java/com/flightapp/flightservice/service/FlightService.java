package com.flightapp.flightservice.service;

import com.flightapp.flightservice.dto.request.AddFlightRequest;
import com.flightapp.flightservice.dto.request.SearchFlightRequest;
import com.flightapp.flightservice.dto.response.AddFlightResponse;
import com.flightapp.flightservice.dto.response.FlightResponse;

import java.util.List;

public interface FlightService {

    AddFlightResponse addFlight(AddFlightRequest request);

    List<FlightResponse> getAllFlights();

    FlightResponse getFlightById(Integer id);

    List<FlightResponse> searchFlight(SearchFlightRequest request);
}
