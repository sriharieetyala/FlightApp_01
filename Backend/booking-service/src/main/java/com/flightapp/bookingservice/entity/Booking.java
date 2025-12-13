package com.flightapp.bookingservice.entity;

import com.flightapp.bookingservice.enums.GENDER;
import com.flightapp.bookingservice.enums.MEAL;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "bookings")
public class Booking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private Integer flightId;
    private String passengerName;
    private Integer age;

    @Enumerated(EnumType.STRING)
    private GENDER gender;

    @Enumerated(EnumType.STRING)
    private MEAL meal;

    private String email;
    private Integer numberOfTickets;

    private String status; // BOOKED / CANCELLED

    @Column(unique = true, nullable = false)
    private String pnr;

}
