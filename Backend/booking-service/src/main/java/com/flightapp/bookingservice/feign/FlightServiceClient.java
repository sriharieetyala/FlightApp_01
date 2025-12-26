package com.flightapp.bookingservice.feign;

import com.flightapp.bookingservice.dto.response.FlightResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "FLIGHT-SERVICE")
public interface FlightServiceClient {

    @GetMapping("/flights/{id}")
    FlightResponse getFlightById(@PathVariable("id") Integer id);

    // Reduce seats after booking
    @PutMapping("/flights/{id}/seats")
    void reduceSeats(@PathVariable("id") Integer id, @RequestParam("reduce") Integer count);
}
