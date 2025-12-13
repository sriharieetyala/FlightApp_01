package com.flightapp.flightservice.dto.response;

import lombok.Data;

import java.time.LocalDateTime;
@Data
public class FlightResponse {
    private Integer id;
    private String flightNumber;
    private String fromCity;
    private String toCity;
    private LocalDateTime departureTime;
    private LocalDateTime arrivalTime;
    private float cost;
    private Integer seatsAvailable;

}
