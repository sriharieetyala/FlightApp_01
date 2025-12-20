import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { BookingHistoryService, BookingHistory } from '../../services/booking-history.service';
import { forkJoin } from 'rxjs';

@Component({
  selector: 'app-booking-history',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './booking-history.component.html',
  styleUrl: './booking-history.component.css'
})
export class BookingHistoryComponent implements OnInit {
  bookings: BookingHistory[] = [];
  isLoading = true;
  errorMessage = '';
  successMessage = '';

  // For confirmation dialog
  showConfirmDialog = false;
  bookingToCancel: BookingHistory | null = null;
  isCancelling = false;

  constructor(
    private readonly bookingHistoryService: BookingHistoryService,
    private readonly router: Router
  ) {}

  ngOnInit(): void {
    const email = this.bookingHistoryService.getUserEmail();

    if (!email) {
      this.isLoading = false;
      return;
    }

    this.loadBookings();
  }

  loadBookings(): void {
    this.bookingHistoryService.getMyBookings().subscribe({
      next: (bookings) => {
        this.bookings = bookings;
        // Fetch flight details for each booking
        this.loadFlightDetails();
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

  loadFlightDetails(): void {
    if (this.bookings.length === 0) {
      this.isLoading = false;
      return;
    }

    // Get unique flight IDs
    const flightIds = [...new Set(this.bookings.map(b => b.flightId))];

    // Fetch all flight details in parallel
    const flightRequests = flightIds.map(id => this.bookingHistoryService.getFlightById(id));

    forkJoin(flightRequests).subscribe({
      next: (flights) => {
        // Map flights to bookings
        this.bookings.forEach(booking => {
          booking.flight = flights.find(f => f.id === booking.flightId);
        });
        this.isLoading = false;
      },
      error: () => {
        // Even if flight fetch fails, show bookings without flight details
        this.isLoading = false;
      }
    });
  }

  goBack(): void {
    this.router.navigate(['/']);
  }

  // Show confirmation dialog before cancelling
  cancelBooking(booking: BookingHistory): void {
    this.bookingToCancel = booking;
    this.showConfirmDialog = true;
    this.errorMessage = '';
    this.successMessage = '';
  }

  // User confirms cancellation
  confirmCancel(): void {
    if (!this.bookingToCancel) return;

    this.isCancelling = true;
    this.bookingHistoryService.cancelBooking(this.bookingToCancel.id).subscribe({
      next: () => {
        // Update the booking status locally
        const booking = this.bookings.find(b => b.id === this.bookingToCancel!.id);
        if (booking) {
          booking.status = 'CANCELLED';
        }
        this.successMessage = 'Booking cancelled successfully.';
        this.closeConfirmDialog();
      },
      error: (error) => {
        this.closeConfirmDialog();
        // Backend returns 400 for 24-hour restriction
        if (error.status === 400) {
          this.errorMessage = 'Sorry, this booking cannot be cancelled as the flight departs within the next 24 hours.';
        } else {
          this.errorMessage = 'Failed to cancel booking. Please try again.';
        }
      }
    });
  }

  // User cancels the confirmation dialog
  closeConfirmDialog(): void {
    this.showConfirmDialog = false;
    this.bookingToCancel = null;
    this.isCancelling = false;
  }
}
