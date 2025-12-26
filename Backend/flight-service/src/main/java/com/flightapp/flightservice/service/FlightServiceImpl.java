package com.flightapp.flightservice.service;

import com.flightapp.flightservice.dto.request.AddFlightRequest;
import com.flightapp.flightservice.dto.request.SearchFlightRequest;
import com.flightapp.flightservice.dto.response.AddFlightResponse;
import com.flightapp.flightservice.dto.response.FlightResponse;
import com.flightapp.flightservice.entity.Flight;
import com.flightapp.flightservice.exception.DuplicateFlightException;
import com.flightapp.flightservice.exception.FlightNotFoundException;
import com.flightapp.flightservice.repository.FlightRepository;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;

@Service
@AllArgsConstructor
public class FlightServiceImpl implements FlightService {

    private final FlightRepository repo;

    // ================= ADD FLIGHT =================
    @Override
    public AddFlightResponse addFlight(AddFlightRequest req) {

        // Duplicate flight number
        if (repo.findByFlightNumber(req.getFlightNumber()).isPresent()) {
            throw new DuplicateFlightException("Flight already exists");
        }

        // From and To city must be different
        if (req.getFromCity().equalsIgnoreCase(req.getToCity())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "From city and To city cannot be the same");
        }

        // Departure time must not be in the past
        if (req.getDepartureTime().isBefore(LocalDateTime.now())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Departure time cannot be in the past");
        }

        // Arrival must be after departure
        if (req.getArrivalTime().isBefore(req.getDepartureTime())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Arrival time must be after departure time");
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

    // ================= GET ALL =================
    @Override
    public List<FlightResponse> getAllFlights() {
        return repo.findAll()
                .stream()
                .map(this::mapToDto)
                .toList();
    }

    // ================= GET BY ID =================
    @Override
    public FlightResponse getFlightById(Integer id) {
        Flight flight = repo.findById(id)
                .orElseThrow(() -> new FlightNotFoundException("Flight Not Found"));
        return mapToDto(flight);
    }

    // ================= SEARCH (FIXED) =================
    @Override
    public List<FlightResponse> searchFlight(SearchFlightRequest req) {

        // Calculate day range
        LocalDateTime startOfDay = req.getTravelDate().atStartOfDay();
        LocalDateTime endOfDay = req.getTravelDate().atTime(23, 59, 59);

        List<Flight> flights = repo.findByFromCityIgnoreCaseAndToCityIgnoreCaseAndDepartureTimeBetween(
                req.getFromCity(),
                req.getToCity(),
                startOfDay,
                endOfDay);

        if (flights.isEmpty()) {
            throw new FlightNotFoundException(
                    "No flights found from " + req.getFromCity()
                            + " to " + req.getToCity()
                            + " on " + req.getTravelDate());
        }

        return flights.stream()
                .map(this::mapToDto)
                .toList();
    }

    // ================= MAPPER =================
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

    // ================= REDUCE SEATS =================
    @Override
    public void reduceSeats(Integer flightId, Integer count) {
        Flight flight = repo.findById(flightId)
                .orElseThrow(() -> new FlightNotFoundException("Flight Not Found"));

        if (flight.getSeatsAvailable() < count) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Not enough seats available");
        }

        flight.setSeatsAvailable(flight.getSeatsAvailable() - count);
        repo.save(flight);
    }
}
