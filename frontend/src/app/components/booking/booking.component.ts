import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { Flight } from '../../models/flight.models';
import { BookingService, BookingRequest } from '../../services/booking.service';
import { AuthService } from '../../services/auth.service';

// Total seats per flight (fixed for simple UI)
const TOTAL_SEATS = 72;  // 12 rows × 6 columns
const SEATS_PER_ROW = 6;

// Interface for individual passenger details
interface Passenger {
  passengerName: string;
  age: number;
  gender: 'MALE' | 'FEMALE' | 'OTHER';
  meal: 'NONE' | 'VEG' | 'NONVEG';
  seatNumber: string;
}

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
  flightId: number = 0;
  userEmail: string = '';

  // Number of passengers to book
  numberOfPassengers: number = 1;

  // Array of passenger details
  passengers: Passenger[] = [];

  // Seat grid data
  allSeats: string[] = [];      // All seat numbers ["1", "2", ..., "30"]
  bookedSeats: string[] = [];   // Already booked by others (red)
  selectedSeats: string[] = []; // Selected by current user (green)

  isSubmitting = false;
  successMessage = '';
  errorMessage = '';
  isLoggedIn = false;

  // Store PNRs for all passengers
  pnrList: string[] = [];

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
    const email = this.authService.getEmail();
    if (email) {
      this.userEmail = email;
      this.isLoggedIn = true;
    }

    const flightIdParam = this.route.snapshot.paramMap.get('id');
    if (flightIdParam) {
      this.flightId = +flightIdParam;
      this.loadFlight(this.flightId);
      this.loadBookedSeats(this.flightId);
    } else {
      this.router.navigate(['/']);
    }

    // Initialize seat grid
    this.initializeSeats();

    // Initialize with 1 passenger
    this.initializePassengers();
  }

  // Initialize all seat numbers
  initializeSeats(): void {
    this.allSeats = [];
    for (let i = 1; i <= TOTAL_SEATS; i++) {
      this.allSeats.push(i.toString());
    }
  }

  // Load already booked seats from backend
  loadBookedSeats(flightId: number): void {
    this.bookingService.getBookedSeats(flightId).subscribe({
      next: (seats) => {
        this.bookedSeats = seats;
      },
      error: () => {
        this.bookedSeats = [];
      }
    });
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

  // Initialize passengers array based on count
  initializePassengers(): void {
    this.passengers = [];
    for (let i = 0; i < this.numberOfPassengers; i++) {
      this.passengers.push({
        passengerName: '',
        age: 0,
        gender: 'MALE',
        meal: 'NONE',
        seatNumber: ''
      });
    }
    // Reset selected seats when passenger count changes
    this.selectedSeats = [];
  }

  // When user changes number of passengers
  onPassengerCountChange(): void {
    const availableSeats = TOTAL_SEATS - this.bookedSeats.length;
    if (this.numberOfPassengers > availableSeats) {
      this.numberOfPassengers = availableSeats;
    }
    if (this.numberOfPassengers < 1) {
      this.numberOfPassengers = 1;
    }
    this.initializePassengers();
  }

  // Get seat status for styling
  getSeatStatus(seat: string): string {
    if (this.bookedSeats.includes(seat)) {
      return 'booked';  // Red - already booked
    }
    if (this.selectedSeats.includes(seat)) {
      return 'selected';  // Green - selected by user
    }
    return 'available';  // Grey - available
  }

  // Handle seat click
  onSeatClick(seat: string): void {
    // Can't select already booked seats
    if (this.bookedSeats.includes(seat)) {
      return;
    }

    // Toggle selection
    if (this.selectedSeats.includes(seat)) {
      // Deselect
      this.selectedSeats = this.selectedSeats.filter(s => s !== seat);
    } else {
      // Can only select as many as passengers
      if (this.selectedSeats.length < this.numberOfPassengers) {
        this.selectedSeats.push(seat);
      }
    }

    // Assign seats to passengers
    this.passengers.forEach((p, i) => {
      p.seatNumber = this.selectedSeats[i] || '';
    });
  }

  // Get rows for seat grid display
  getSeatRows(): string[][] {
    const rows: string[][] = [];
    for (let i = 0; i < this.allSeats.length; i += SEATS_PER_ROW) {
      rows.push(this.allSeats.slice(i, i + SEATS_PER_ROW));
    }
    return rows;
  }

  goBack(): void {
    this.router.navigate(['/']);
  }

  isFormValid(): boolean {
    // Check all passengers have valid data
    for (const p of this.passengers) {
      if (!p.passengerName.trim() || p.age < 1) {
        return false;
      }
    }
    // Must have selected correct number of seats
    return (
      this.userEmail.trim().length > 0 &&
      this.numberOfPassengers > 0 &&
      this.selectedSeats.length === this.numberOfPassengers
    );
  }

  async submitBooking(): Promise<void> {
    if (!this.isFormValid()) {
      this.errorMessage = 'Please fill all fields and select seats for all passengers.';
      return;
    }

    this.isSubmitting = true;
    this.errorMessage = '';
    this.pnrList = [];

    // Book each passenger sequentially to avoid race condition
    try {
      for (const passenger of this.passengers) {
        const request: BookingRequest = {
          flightId: this.flightId,
          passengerName: passenger.passengerName,
          age: passenger.age,
          gender: passenger.gender,
          meal: passenger.meal,
          email: this.userEmail,
          numberOfTickets: 1,
          seatNumber: passenger.seatNumber
        };

        const response = await this.bookingService.createBooking(request).toPromise();
        if (response) {
          this.pnrList.push(response.pnr);
        }
      }

      this.isSubmitting = false;
      if (this.pnrList.length === 1) {
        this.successMessage = `Booking confirmed! Your PNR: ${this.pnrList[0]}`;
      } else {
        this.successMessage = `All ${this.pnrList.length} bookings confirmed! PNRs: ${this.pnrList.join(', ')}`;
      }
    } catch {
      this.isSubmitting = false;
      this.errorMessage = 'Booking failed. Please try again.';
    }
  }
}
