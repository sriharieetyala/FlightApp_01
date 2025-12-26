import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Flight } from '../models/flight.models';

export interface BookingRequest {
  flightId: number;
  passengerName: string;
  age: number;
  gender: string;
  meal: string;
  email: string;
  numberOfTickets: number;
  seatNumber: string;  // Seat selected by passenger
}

export interface BookingResponse {
  pnr: string;
}

export interface Booking {
  id: number;
  flightId: number;
  passengerName: string;
  age: number;
  gender: string;
  meal: string;
  email: string;
  numberOfTickets: number;
  seatNumber: string;
  status: string;
  pnr: string;
}


@Injectable({
  providedIn: 'root'
})


export class BookingService {
  private readonly flightApiUrl = 'http://localhost:8080/flight-service/flights';
  private readonly bookingApiUrl = 'http://localhost:8080/booking-service/bookings';

  constructor(
    private readonly http: HttpClient
  ) { }

  getFlightById(id: number): Observable<Flight> {
    return this.http.get<Flight>(`${this.flightApiUrl}/${id}`);
  }

  createBooking(booking: BookingRequest): Observable<BookingResponse> {
    return this.http.post<BookingResponse>(this.bookingApiUrl, booking);
  }

  getBookingsByEmail(email: string): Observable<Booking[]> {
    return this.http.get<Booking[]>(`${this.bookingApiUrl}/email/${email}`);
  }

  // Get booked seat numbers for a flight
  getBookedSeats(flightId: number): Observable<string[]> {
    return this.http.get<string[]>(`${this.bookingApiUrl}/flight/${flightId}/seats`);
  }
}
