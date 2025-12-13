package com.flightapp.bookingservice.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class BookingResponse {
    private Integer id;
    private String pnr;
}
