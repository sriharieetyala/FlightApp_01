package com.flightapp.flightservice.controller;


import com.flightapp.flightservice.dto.request.AddFlightRequest;
import com.flightapp.flightservice.dto.request.SearchFlightRequest;
import com.flightapp.flightservice.dto.response.AddFlightResponse;
import com.flightapp.flightservice.dto.response.FlightResponse;
import com.flightapp.flightservice.entity.Flight;
import com.flightapp.flightservice.service.FlightService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/flights")
@RequiredArgsConstructor
public class FlightController {

    private final FlightService service;

    @PostMapping
    public ResponseEntity<AddFlightResponse> addFlight(@Valid @RequestBody AddFlightRequest req) {
        AddFlightResponse response = service.addFlight(req);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<List<FlightResponse>> getAll() {
        return ResponseEntity.ok(service.getAllFlights());
    }

    @GetMapping("/{id}")
    public ResponseEntity<FlightResponse> getById(@PathVariable Integer id) {
        return ResponseEntity.ok(service.getFlightById(id));
    }

    @PostMapping("/search")

    public ResponseEntity<List<FlightResponse>> search(@Valid @RequestBody SearchFlightRequest req) {
        List<FlightResponse> response = service.searchFlight(req);
        return ResponseEntity.ok(response);
    }




}
