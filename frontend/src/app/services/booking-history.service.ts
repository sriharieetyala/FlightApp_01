import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { AuthService } from './auth.service';

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
}

@Injectable({
  providedIn: 'root'
})
export class BookingHistoryService {
  private readonly bookingApiUrl = 'http://localhost:8080/booking-service/bookings';

  constructor(
    private readonly http: HttpClient,
    private readonly authService: AuthService
  ) {}

  // Fetches all bookings for the logged-in user
  getMyBookings(): Observable<BookingHistory[]> {
    const email = this.authService.getEmail();
    return this.http.get<BookingHistory[]>(`${this.bookingApiUrl}/email/${email}`);
  }

  // Get logged-in user's email
  getUserEmail(): string | null {
    return this.authService.getEmail();
  }

  // Cancel a booking (functionality to be added later)
  cancelBooking(bookingId: number): Observable<string> {
    return this.http.delete<string>(`${this.bookingApiUrl}/${bookingId}`);
  }
}

