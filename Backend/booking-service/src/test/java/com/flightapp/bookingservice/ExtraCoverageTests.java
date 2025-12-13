package com.flightapp.bookingservice;



import com.flightapp.bookingservice.dto.request.BookingRequest;
import com.flightapp.bookingservice.entity.Booking;
import com.flightapp.bookingservice.exception.BookingInvalidException;
import com.flightapp.bookingservice.exception.BookingNotFoundException;
import com.flightapp.bookingservice.service.BookingServiceImpl;
import com.flightapp.bookingservice.producer.RabbitMQProducer;
import com.flightapp.bookingservice.repository.BookingRepository;
import com.flightapp.bookingservice.feign.FlightServiceClient;
import com.flightapp.bookingservice.dto.response.FlightResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(org.mockito.junit.jupiter.MockitoExtension.class)
public class ExtraCoverageTests {

    @Mock
    BookingRepository repo;

    @Mock
    FlightServiceClient flightClient;

    @Mock
    RabbitMQProducer producer;

    @InjectMocks
    BookingServiceImpl service;

    private BookingRequest sampleRequest;

    @BeforeEach
    void setUp() {
        sampleRequest = BookingRequest.builder()
                .flightId(100)
                .passengerName("Alice")
                .age(28)
                .gender(null) // edge case
                .meal(null)   // edge case
                .email("alice@example.com")
                .numberOfTickets(2)
                .build();
    }

    // ---------------- Exception classes coverage ----------------
    @Test
    void bookingExceptions_constructors() {
        BookingNotFoundException notFound = new BookingNotFoundException("Not Found");
        assertEquals("Not Found", notFound.getMessage());

        BookingInvalidException invalid = new BookingInvalidException("Invalid Booking");
        assertEquals("Invalid Booking", invalid.getMessage());
    }

    // ---------------- Controller edge-like coverage for service ----------------
    @Test
    void getBookingsByEmail_emptyList() {
        when(repo.findByEmail("noone@test.com")).thenReturn(List.of());
        var result = service.getBookingsByEmail("noone@test.com");
        assertNotNull(result);
        assertEquals(0, result.size());
    }

    // ---------------- Service edge cases ----------------
    @Test
    void bookTicket_nullGenderMeal_savesAndSendsEmail() {
        FlightResponse flight = new FlightResponse();
        flight.setId(100);
        flight.setSeatsAvailable(5);
        flight.setDepartureTime(LocalDateTime.now().plusDays(5));
        when(flightClient.getFlightById(100)).thenReturn(flight);

        Booking savedBooking = Booking.builder()
                .id(56)
                .pnr(UUID.randomUUID().toString().substring(0, 8).toUpperCase())
                .status("BOOKED")
                .email("alice@example.com")
                .build();
        when(repo.save(any())).thenReturn(savedBooking);

        Booking result = service.bookTicket(sampleRequest);
        assertNotNull(result);
        verify(producer, times(1)).sendBookingEmail(contains(sampleRequest.getEmail()));
    }

    @Test
    void cancelBooking_fallback_path() {
        BookingInvalidException ex = assertThrows(BookingInvalidException.class,
                () -> service.getBookingFallback(1, new RuntimeException()));
        assertTrue(ex.getMessage().contains("Flight service unavailable"));
    }

    @Test
    void flightServiceFallback_path() {
        BookingInvalidException ex = assertThrows(BookingInvalidException.class,
                () -> service.flightServiceFallback(sampleRequest, new RuntimeException()));
        assertTrue(ex.getMessage().contains("Flight service unavailable"));
    }

    @Test
    void getBookingById_notFoundEdge() {
        when(repo.findById(9999)).thenReturn(Optional.empty());
        assertThrows(BookingNotFoundException.class, () -> service.getBookingById(9999));
    }

    @Test
    void getBookingByPnr_notFoundEdge() {
        when(repo.findByPnr("NOPNR")).thenReturn(Optional.empty());
        assertThrows(BookingNotFoundException.class, () -> service.getBookingByPnr("NOPNR"));
    }
}
