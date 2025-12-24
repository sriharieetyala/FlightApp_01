import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { Flight } from '../../models/flight.models';
import { BookingService, BookingRequest } from '../../services/booking.service';
import { AuthService } from '../../services/auth.service';

@Component({
  selector: 'app-booking',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './booking.component.html',
  styleUrl: './booking.component.css'
})
export class BookingComponent implements OnInit {
  flight: Flight | null = null;
  isLoading = true;

  bookingForm: BookingRequest = {
    flightId: 0,
    passengerName: '',
    age: 0,
    gender: 'MALE',
    meal: 'NONE',
    email: '',
    numberOfTickets: 1
  };

  isSubmitting = false;
  successMessage = '';
  errorMessage = '';
  isLoggedIn = false;

  constructor(
    private readonly bookingService: BookingService,
    private readonly route: ActivatedRoute,
    private readonly router: Router,
    private readonly authService: AuthService
  ) { }

  ngOnInit(): void {
    // Check if user is logged in
    if (!this.authService.isAuthenticated()) {
      this.router.navigate(['/login']);
      return;
    }

    // Auto-populate email from logged-in user
    const userEmail = this.authService.getEmail();
    if (userEmail) {
      this.bookingForm.email = userEmail;
      this.isLoggedIn = true;
    }

    const flightId = this.route.snapshot.paramMap.get('id');
    if (flightId) {
      this.bookingForm.flightId = +flightId;
      this.loadFlight(+flightId);
    } else {
      this.router.navigate(['/']);
    }
  }

  loadFlight(id: number): void {
    this.bookingService.getFlightById(id).subscribe({
      next: (flight) => {
        this.flight = flight;
        this.isLoading = false;
      },
      error: () => {
        this.errorMessage = 'Could not load flight details.';
        this.isLoading = false;
      }
    });
  }

  goBack(): void {
    this.router.navigate(['/']);
  }

  isFormValid(): boolean {
    return (
      this.bookingForm.passengerName.trim().length > 0 &&
      this.bookingForm.age > 0 &&
      this.bookingForm.email.trim().length > 0 &&
      this.bookingForm.numberOfTickets > 0
    );
  }

  submitBooking(): void {
    if (!this.isFormValid()) {
      this.errorMessage = 'Please fill in all required fields.';
      return;
    }

    this.isSubmitting = true;
    this.errorMessage = '';

    this.bookingService.createBooking(this.bookingForm).subscribe({
      next: (response) => {
        this.isSubmitting = false;
        this.successMessage = `Booking confirmed! Your PNR: ${response.pnr}`;
      },
      error: () => {
        this.isSubmitting = false;
        this.errorMessage = 'Booking failed. Please try again.';
      }
    });
  }
}
