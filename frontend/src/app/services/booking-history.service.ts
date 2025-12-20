import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { AuthService } from './auth.service';
import { Flight } from '../models/flight.models';

export interface BookingHistory {
  id: number;
  flightId: number;
  passengerName: string;
  age: number;
  gender: string;
  meal: string;
  email: string;
  numberOfTickets: number;
  status: string;
  pnr: string;
  // Flight details (populated from frontend)
  flight?: Flight;
}

@Injectable({
  providedIn: 'root'
})
export class BookingHistoryService {
  private readonly bookingApiUrl = 'http://localhost:8080/booking-service/bookings';
  private readonly flightApiUrl = 'http://localhost:8080/flight-service/flights';

  constructor(
    private readonly http: HttpClient,
    private readonly authService: AuthService
  ) {}

  // Fetches all bookings for the logged-in user
  getMyBookings(): Observable<BookingHistory[]> {
    const email = this.authService.getEmail();
    return this.http.get<BookingHistory[]>(`${this.bookingApiUrl}/email/${email}`);
  }

  // Fetch flight details by ID
  getFlightById(flightId: number): Observable<Flight> {
    return this.http.get<Flight>(`${this.flightApiUrl}/${flightId}`);
  }

  // Get logged-in user's email
  getUserEmail(): string | null {
    return this.authService.getEmail();
  }

  // Cancel a booking - returns success message or error
  cancelBooking(bookingId: number): Observable<string> {
    return this.http.delete(`${this.bookingApiUrl}/${bookingId}`, { responseType: 'text' });
  }
}

