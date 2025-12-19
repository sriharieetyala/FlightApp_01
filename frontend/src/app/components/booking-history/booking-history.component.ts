import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { AuthService } from '../../services/auth.service';
import { BookingService, Booking } from '../../services/booking.service';

@Component({
  selector: 'app-booking-history',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './booking-history.component.html',
  styleUrl: './booking-history.component.css'
})
export class BookingHistoryComponent implements OnInit {
  bookings: Booking[] = [];
  isLoading = true;
  errorMessage = '';

  constructor(
    private readonly authService: AuthService,
    private readonly bookingService: BookingService,
    private readonly router: Router
  ) {}

  ngOnInit(): void {
    const email = this.authService.getEmail();

    if (!email) {
      this.isLoading = false;
      return;
    }

    this.bookingService.getBookingsByEmail(email).subscribe({
      next: (bookings) => {
        this.bookings = bookings;
        this.isLoading = false;
      },
      error: (error) => {
        this.isLoading = false;
        if (error.status === 404) {
          this.bookings = [];
        } else {
          this.errorMessage = 'Failed to load booking history.';
        }
      }
    });
  }

  goBack(): void {
    this.router.navigate(['/']);
  }

  cancelBooking(bookingId: number): void {
    // Cancel functionality will be added later
    console.log('Cancel booking:', bookingId);
  }
}
