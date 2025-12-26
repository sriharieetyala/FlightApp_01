package com.flightapp.bookingservice.service;

import com.flightapp.bookingservice.dto.request.BookingRequest;
import com.flightapp.bookingservice.dto.response.FlightResponse;
import com.flightapp.bookingservice.entity.Booking;
import com.flightapp.bookingservice.exception.BookingInvalidException;
import com.flightapp.bookingservice.exception.BookingNotFoundException;
import com.flightapp.bookingservice.feign.FlightServiceClient;
import com.flightapp.bookingservice.producer.RabbitMQProducer;
import com.flightapp.bookingservice.repository.BookingRepository;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BookingServiceImpl implements BookingService {

    private final BookingRepository repo;
    private final FlightServiceClient flightClient;
    private final RabbitMQProducer producer;

    private static final String FLIGHT_CB = "flightServiceCB";

    @Override
    @CircuitBreaker(name = FLIGHT_CB, fallbackMethod = "flightServiceFallback")
    public Booking bookTicket(BookingRequest request) {

        FlightResponse flight = flightClient.getFlightById(request.getFlightId());

        if (request.getNumberOfTickets() > flight.getSeatsAvailable()) {
            throw new BookingInvalidException("Not enough seats available");
        }

        // generate PNR
        String pnr = UUID.randomUUID().toString().substring(0, 8).toUpperCase();

        Booking booking = Booking.builder()
                .flightId(request.getFlightId())
                .passengerName(request.getPassengerName())
                .age(request.getAge())
                .gender(request.getGender())
                .meal(request.getMeal())
                .email(request.getEmail())
                .numberOfTickets(request.getNumberOfTickets())
                .seatNumber(request.getSeatNumber()) // Save selected seat
                .status("BOOKED")
                .pnr(pnr)
                .build();

        Booking saved = repo.save(booking);

        // -------------- REDUCE SEATS IN FLIGHT SERVICE ----------------
        flightClient.reduceSeats(request.getFlightId(), request.getNumberOfTickets());

        // -------------- SEND EMAIL MESSAGE TO RABBITMQ ----------------
        String emailMessage = " Booking Confirmed!\n" +
                "PNR: " + pnr + "\n" +
                "Passenger: " + request.getPassengerName() + "\n" +
                "Seat: " + request.getSeatNumber() + "\n" +
                "Email: " + request.getEmail();

        producer.sendBookingEmail(emailMessage);

        return saved;
    }

    // ---------- FALLBACK METHOD ----------
    public Booking flightServiceFallback(BookingRequest request, Throwable ex) {
        throw new BookingInvalidException("Flight service unavailable, booking cannot be processed right now.");
    }

    @Override
    @CircuitBreaker(name = FLIGHT_CB, fallbackMethod = "getBookingFallback")
    public Booking cancelBooking(Integer id) {

        Booking booking = repo.findById(id)
                .orElseThrow(() -> new BookingNotFoundException("Booking not found"));

        FlightResponse flight = flightClient.getFlightById(booking.getFlightId());

        if (Duration.between(LocalDateTime.now(), flight.getDepartureTime()).toHours() < 24) {
            throw new BookingInvalidException("Cannot cancel less than 24 hours before departure");
        }

        booking.setStatus("CANCELLED");
        return repo.save(booking);
    }

    public Booking getBookingFallback(Integer id, Throwable ex) {
        throw new BookingInvalidException("Flight service unavailable, cancellation cannot be processed now.");
    }

    @Override
    public Booking getBookingById(Integer id) {
        return repo.findById(id)
                .orElseThrow(() -> new BookingNotFoundException("Booking not found"));
    }

    @Override
    public Booking getBookingByPnr(String pnr) {
        return repo.findByPnr(pnr)
                .orElseThrow(() -> new BookingNotFoundException("PNR not found"));
    }

    @Override
    public List<Booking> getBookingsByEmail(String email) {
        return repo.findByEmail(email);
    }

    @Override
    public List<String> getBookedSeats(Integer flightId) {
        // Get all BOOKED bookings for this flight and extract seat numbers
        return repo.findByFlightIdAndStatus(flightId, "BOOKED")
                .stream()
                .map(Booking::getSeatNumber)
                .filter(seat -> seat != null && !seat.isEmpty())
                .collect(Collectors.toList());
    }
}
