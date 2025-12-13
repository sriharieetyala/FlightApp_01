package com.flightapp.flightservice.repository;

import com.flightapp.flightservice.dto.response.FlightResponse;
import com.flightapp.flightservice.entity.Flight;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FlightRepository extends JpaRepository<Flight, Integer> {
    Optional<Flight> findByFlightNumber(String flightNumber);

    List<Flight> findByFromCityAndToCity(String fromCity, String toCity);
}
