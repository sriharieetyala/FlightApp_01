package com.flightapp.bookingservice.dto.request;

import com.flightapp.bookingservice.enums.GENDER;
import com.flightapp.bookingservice.enums.MEAL;
import jakarta.validation.constraints.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookingRequest {

    @NotNull(message = "flightId is required")
    private Integer flightId;

    @NotBlank(message = "Passenger name is required")
    private String passengerName;

    @NotNull(message = "Age is required")
    @Min(value = 1, message = "Age must be greater than 0")
    private Integer age;

    @NotNull(message = "Gender is required")
    private GENDER gender;

    @NotNull(message = "Meal is required")
    private MEAL meal;

    @Email(message = "Invalid email")
    @NotBlank(message = "Email is required")
    private String email;

    @NotNull(message = "Number of tickets required")
    @Min(value = 1)
    private Integer numberOfTickets;
}
