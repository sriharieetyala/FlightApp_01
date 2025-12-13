package com.flightapp.flightservice.service;



import com.flightapp.flightservice.dto.request.AddFlightRequest;
import com.flightapp.flightservice.dto.request.SearchFlightRequest;
import com.flightapp.flightservice.dto.response.AddFlightResponse;
import com.flightapp.flightservice.dto.response.FlightResponse;
import com.flightapp.flightservice.entity.Flight;
import com.flightapp.flightservice.exception.*;
import com.flightapp.flightservice.repository.FlightRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;  // ✅ MUST ADD


@Service
@AllArgsConstructor
public class FlightServiceImpl implements FlightService {
    private final FlightRepository repo;

    @Override
    public AddFlightResponse addFlight(AddFlightRequest req) {

        if (repo.findByFlightNumber(req.getFlightNumber()).isPresent()) {
            throw new DuplicateFlightException("Flight already exists");
        }

        Flight flight = Flight.builder()
                .flightNumber(req.getFlightNumber())
                .fromCity(req.getFromCity())
                .toCity(req.getToCity())
                .departureTime(req.getDepartureTime())
                .arrivalTime(req.getArrivalTime())
                .cost(req.getCost())
                .seatsAvailable(req.getSeatsAvailable())
                .build();

        repo.save(flight);
        return new AddFlightResponse(flight.getId());
    }

    @Override
    public List<FlightResponse> getAllFlights() {
        return repo.findAll().stream().map(this::mapToDto).toList();
    }

    @Override
    public FlightResponse getFlightById(Integer id) {
        Flight flight = repo.findById(id)
                .orElseThrow(() -> new FlightNotFoundException("Flight Not Found"));
        return mapToDto(flight);
    }

    @Override
    public List<FlightResponse> searchFlight(SearchFlightRequest req) {

        List<Flight> flights = repo.findByFromCityAndToCity(
                req.getFromCity(),
                req.getToCity()
        );

        if (flights.isEmpty()) {
            throw new FlightNotFoundException(
                    "No flights found from " + req.getFromCity() + " to " + req.getToCity()
            );
        }

        return flights.stream()
                .map(this::mapToDto)
                .toList();
    }


    private FlightResponse mapToDto(Flight f) {
        FlightResponse res = new FlightResponse();
        res.setId(f.getId());
        res.setFlightNumber(f.getFlightNumber());
        res.setFromCity(f.getFromCity());
        res.setToCity(f.getToCity());
        res.setDepartureTime(f.getDepartureTime());
        res.setArrivalTime(f.getArrivalTime());
        res.setCost(f.getCost());
        res.setSeatsAvailable(f.getSeatsAvailable());
        return res;
    }
}
