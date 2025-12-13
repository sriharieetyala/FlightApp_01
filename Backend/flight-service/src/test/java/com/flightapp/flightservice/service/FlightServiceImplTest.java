package com.flightapp.flightservice.service;

import com.flightapp.flightservice.dto.request.AddFlightRequest;
import com.flightapp.flightservice.dto.request.SearchFlightRequest;
import com.flightapp.flightservice.dto.response.AddFlightResponse;
import com.flightapp.flightservice.dto.response.FlightResponse;
import com.flightapp.flightservice.entity.Flight;
import com.flightapp.flightservice.exception.DuplicateFlightException;
import com.flightapp.flightservice.exception.FlightNotFoundException;
import com.flightapp.flightservice.repository.FlightRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class FlightServiceImplTest {

    @InjectMocks
    private FlightServiceImpl service;

    @Mock
    private FlightRepository repo;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void addFlight_success() {
        AddFlightRequest req = new AddFlightRequest();
        req.setFlightNumber("F101");
        req.setFromCity("CityA");
        req.setToCity("CityB");
        req.setDepartureTime(LocalDateTime.now().plusDays(1));
        req.setArrivalTime(LocalDateTime.now().plusDays(1).plusHours(2));
        req.setCost(200f);
        req.setSeatsAvailable(100);

        when(repo.findByFlightNumber("F101")).thenReturn(Optional.empty());
        when(repo.save(any(Flight.class))).thenAnswer(invocation -> {
            Flight f = invocation.getArgument(0);
            f.setId(1);
            return f;
        });

        AddFlightResponse response = service.addFlight(req);

        assertEquals(1, response.getId());
        verify(repo, times(1)).save(any(Flight.class));
    }

    @Test
    void addFlight_duplicateFlight() {
        when(repo.findByFlightNumber("F101")).thenReturn(Optional.of(new Flight()));
        AddFlightRequest req = new AddFlightRequest();
        req.setFlightNumber("F101");

        assertThrows(DuplicateFlightException.class, () -> service.addFlight(req));
        verify(repo, never()).save(any());
    }

    @Test
    void getAllFlights_success() {
        Flight f1 = Flight.builder().id(1).flightNumber("F101").fromCity("A").toCity("B")
                .departureTime(LocalDateTime.now()).arrivalTime(LocalDateTime.now())
                .cost(100).seatsAvailable(50).build();
        when(repo.findAll()).thenReturn(List.of(f1));

        List<FlightResponse> flights = service.getAllFlights();
        assertEquals(1, flights.size());
        assertEquals("F101", flights.get(0).getFlightNumber());
    }

    @Test
    void getAllFlights_emptyList() {
        when(repo.findAll()).thenReturn(Collections.emptyList());

        List<FlightResponse> flights = service.getAllFlights();
        assertTrue(flights.isEmpty());
    }

    @Test
    void getFlightById_found() {
        Flight flight = Flight.builder().id(1).flightNumber("F101").fromCity("A").toCity("B")
                .departureTime(LocalDateTime.now()).arrivalTime(LocalDateTime.now())
                .cost(100).seatsAvailable(50).build();
        when(repo.findById(1)).thenReturn(Optional.of(flight));

        FlightResponse res = service.getFlightById(1);
        assertEquals("F101", res.getFlightNumber());
    }

    @Test
    void getFlightById_notFound() {
        when(repo.findById(1)).thenReturn(Optional.empty());
        assertThrows(FlightNotFoundException.class, () -> service.getFlightById(1));
    }

    @Test
    void searchFlight_found() {
        SearchFlightRequest req = new SearchFlightRequest();
        req.setFromCity("A");
        req.setToCity("B");

        Flight flight = Flight.builder().id(1).flightNumber("F101").fromCity("A").toCity("B")
                .departureTime(LocalDateTime.now()).arrivalTime(LocalDateTime.now())
                .cost(100).seatsAvailable(50).build();

        // repo returns List<Flight>, so stub with a list
        when(repo.findByFromCityAndToCity("A", "B"))
                .thenReturn(List.of(flight));

        // service returns List<FlightResponse>, not a single object
        List<FlightResponse> resList = service.searchFlight(req);

        assertNotNull(resList);
        assertEquals(1, resList.size());
        FlightResponse res = resList.get(0);
        assertEquals("F101", res.getFlightNumber());
        assertEquals(1, res.getId());
    }

    @Test
    void searchFlight_notFound() {
        SearchFlightRequest req = new SearchFlightRequest();
        req.setFromCity("A");
        req.setToCity("B");

        // no flights found -> empty list
        when(repo.findByFromCityAndToCity("A", "B"))
                .thenReturn(Collections.emptyList());

        assertThrows(FlightNotFoundException.class, () -> service.searchFlight(req));
    }
}
