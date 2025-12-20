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
  ) { }

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
        // Sort bookings: BOOKED first (by date, newest first), then CANCELLED (by date, newest first)
        this.bookings = this.sortBookings(bookings);
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

  sortBookings(bookings: BookingHistory[]): BookingHistory[] {
    return bookings.sort((a, b) => {
      // BOOKED first, CANCELLED at the end
      if (a.status === 'BOOKED' && b.status === 'CANCELLED') {
        return -1;
      }
      if (a.status === 'CANCELLED' && b.status === 'BOOKED') {
        return 1;
      }

      // Within same status, sort by date (newest first)
      const dateA = a.flight?.departureTime
        ? new Date(a.flight.departureTime).getTime()
        : a.id;
      const dateB = b.flight?.departureTime
        ? new Date(b.flight.departureTime).getTime()
        : b.id;
      return dateB - dateA;
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
        // Re-sort after flight details are loaded (so we can sort by departure date)
        this.bookings = this.sortBookings(this.bookings);
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
        // Update the booking status locally and re-sort
        const booking = this.bookings.find(b => b.id === this.bookingToCancel!.id);
        if (booking) {
          booking.status = 'CANCELLED';
        }
        // Re-sort after status change
        this.bookings = this.sortBookings(this.bookings);
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
