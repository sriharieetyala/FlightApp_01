import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { BookingHistoryService, BookingHistory } from '../../services/booking-history.service';

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
