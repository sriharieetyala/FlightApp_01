package com.flightapp.bookingservice.service;

import com.flightapp.bookingservice.dto.request.BookingRequest;
import com.flightapp.bookingservice.dto.response.FlightResponse;
import com.flightapp.bookingservice.entity.Booking;
import com.flightapp.bookingservice.exception.BookingInvalidException;
import com.flightapp.bookingservice.exception.BookingNotFoundException;
import com.flightapp.bookingservice.feign.FlightServiceClient;
import com.flightapp.bookingservice.producer.RabbitMQProducer;
import com.flightapp.bookingservice.repository.BookingRepository;
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
class BookingServiceImplTest {

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
                .gender(null)
                .meal(null)
                .email("alice@example.com")
                .numberOfTickets(2)
                .build();
    }

    @Test
    void bookTicket_success_savesAndSendsEmail() {
        FlightResponse flight = new FlightResponse();
        flight.setId(100);
        flight.setSeatsAvailable(5);
        flight.setDepartureTime(LocalDateTime.now().plusDays(5));
        when(flightClient.getFlightById(100)).thenReturn(flight);

        Booking savedBooking = Booking.builder()
                .id(55)
                .pnr(UUID.randomUUID().toString().substring(0,8).toUpperCase())
                .status("BOOKED")
                .email("alice@example.com")
                .build();
        when(repo.save(any())).thenReturn(savedBooking);

        Booking result = service.bookTicket(sampleRequest);

        assertNotNull(result);
        assertEquals(55, result.getId());

        // ✅ Verify producer call using predictable part of message
        verify(producer, times(1)).sendBookingEmail(contains(sampleRequest.getEmail()));
    }

    @Test
    void bookTicket_insufficientSeats_throwsBookingInvalidException() {
        FlightResponse flight = new FlightResponse();
        flight.setId(100);
        flight.setSeatsAvailable(1);
        when(flightClient.getFlightById(100)).thenReturn(flight);

        assertThrows(BookingInvalidException.class, () -> service.bookTicket(sampleRequest));
        verify(repo, never()).save(any());
        verify(producer, never()).sendBookingEmail(any());
    }

    @Test
    void bookTicket_fallback_throwsBookingInvalidException() {
        BookingInvalidException ex = assertThrows(BookingInvalidException.class,
                () -> service.flightServiceFallback(sampleRequest, new RuntimeException()));
        assertTrue(ex.getMessage().contains("Flight service unavailable"));
    }

    @Test
    void cancelBooking_success_changesStatusToCancelled() {
        Booking existing = Booking.builder()
                .id(200)
                .flightId(100)
                .pnr("PNR_CANCEL")
                .status("BOOKED")
                .build();
        when(repo.findById(200)).thenReturn(Optional.of(existing));

        FlightResponse flight = new FlightResponse();
        flight.setId(100);
        flight.setDepartureTime(LocalDateTime.now().plusDays(2));
        when(flightClient.getFlightById(100)).thenReturn(flight);

        Booking saved = Booking.builder()
                .id(200)
                .status("CANCELLED")
                .pnr("PNR_CANCEL")
                .build();
        when(repo.save(any(Booking.class))).thenReturn(saved);

        Booking res = service.cancelBooking(200);
        assertEquals("CANCELLED", res.getStatus());
        verify(repo, times(1)).save(any());
    }

    @Test
    void cancelBooking_within24Hours_throwsBookingInvalidException() {
        Booking existing = Booking.builder()
                .id(201)
                .flightId(101)
                .status("BOOKED")
                .pnr("PNR_SOON")
                .build();
        when(repo.findById(201)).thenReturn(Optional.of(existing));

        FlightResponse flight = new FlightResponse();
        flight.setId(101);
        flight.setDepartureTime(LocalDateTime.now().plusHours(12));
        when(flightClient.getFlightById(101)).thenReturn(flight);

        assertThrows(BookingInvalidException.class, () -> service.cancelBooking(201));
        verify(repo, never()).save(any());
    }

    @Test
    void cancelBooking_fallback_throwsBookingInvalidException() {
        BookingInvalidException ex = assertThrows(BookingInvalidException.class,
                () -> service.getBookingFallback(1, new RuntimeException()));
        assertTrue(ex.getMessage().contains("Flight service unavailable"));
    }

    @Test
    void getBookingById_notFound_throwsBookingNotFoundException() {
        when(repo.findById(999)).thenReturn(Optional.empty());
        assertThrows(BookingNotFoundException.class, () -> service.getBookingById(999));
    }

    @Test
    void getBookingByPnr_notFound_throwsBookingNotFoundException() {
        when(repo.findByPnr("NOPE")).thenReturn(Optional.empty());
        assertThrows(BookingNotFoundException.class, () -> service.getBookingByPnr("NOPE"));
    }

    @Test
    void getBookingsByEmail_returnsList() {
        Booking b = Booking.builder().id(1).email("x@y.com").pnr("P1").build();
        when(repo.findByEmail("x@y.com")).thenReturn(List.of(b));
        var res = service.getBookingsByEmail("x@y.com");
        assertEquals(1, res.size());
    }
}
