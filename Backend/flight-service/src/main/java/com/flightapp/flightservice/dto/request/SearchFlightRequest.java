package com.flightapp.flightservice.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class SearchFlightRequest {

    @NotBlank
    private String fromCity;

    @NotBlank
    private String toCity;
}
