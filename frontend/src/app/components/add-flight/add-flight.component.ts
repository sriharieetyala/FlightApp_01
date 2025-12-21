import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { FlightService } from '../../services/flight.service';
import { AuthService } from '../../services/auth.service';
import { AddFlightRequest } from '../../models/flight.models';

@Component({
    selector: 'app-add-flight',
    standalone: true,
    imports: [CommonModule, FormsModule],
    templateUrl: './add-flight.component.html',
    styleUrl: './add-flight.component.css'
})
export class AddFlightComponent implements OnInit {

    isAdmin = false;
    isSubmitting = false;
    errorMessage = '';
    successMessage = '';
    minDateTime = '';

    addFlightForm: AddFlightRequest = {
        flightNumber: '',
        fromCity: '',
        toCity: '',
        departureTime: '',
        arrivalTime: '',
        cost: 0,
        seatsAvailable: 0
    };

    constructor(
        private readonly flightService: FlightService,
        private readonly authService: AuthService,
        private readonly router: Router
    ) { }

    ngOnInit(): void {
        // Check admin access
        this.isAdmin = this.authService.isAdmin();
        if (!this.isAdmin) {
            this.router.navigate(['/']);
            return;
        }

        // Set minimum datetime to now
        this.setMinDateTime();
    }

    setMinDateTime(): void {
        const now = new Date();
        const year = now.getFullYear();
        const month = String(now.getMonth() + 1).padStart(2, '0');
        const day = String(now.getDate()).padStart(2, '0');
        const hours = String(now.getHours()).padStart(2, '0');
        const minutes = String(now.getMinutes()).padStart(2, '0');
        this.minDateTime = `${year}-${month}-${day}T${hours}:${minutes}`;
    }

    isFormValid(): boolean {
        const f = this.addFlightForm;
        return (
            f.flightNumber.trim().length > 0 &&
            f.fromCity.trim().length > 0 &&
            f.toCity.trim().length > 0 &&
            f.departureTime.length > 0 &&
            f.arrivalTime.length > 0 &&
            f.cost > 0 &&
            f.seatsAvailable > 0 &&
            f.fromCity.trim().toUpperCase() !== f.toCity.trim().toUpperCase()
        );
    }

    onSubmit(): void {
        if (!this.isFormValid()) {
            this.errorMessage = 'Please fill all fields correctly.';
            return;
        }

        this.isSubmitting = true;
        this.errorMessage = '';
        this.successMessage = '';

        const request: AddFlightRequest = {
            flightNumber: this.addFlightForm.flightNumber.trim().toUpperCase(),
            fromCity: this.addFlightForm.fromCity.trim().toUpperCase(),
            toCity: this.addFlightForm.toCity.trim().toUpperCase(),
            departureTime: this.addFlightForm.departureTime,
            arrivalTime: this.addFlightForm.arrivalTime,
            cost: this.addFlightForm.cost,
            seatsAvailable: this.addFlightForm.seatsAvailable
        };

        this.flightService.addFlight(request).subscribe({
            next: (response) => {
                this.isSubmitting = false;
                this.successMessage = `Flight added successfully! (ID: ${response.id})`;
                setTimeout(() => this.router.navigate(['/']), 1500);
            },
            error: (error) => {
                this.isSubmitting = false;
                if (error.status === 409 || error.error?.message?.includes('already exists')) {
                    this.errorMessage = 'Flight number already exists.';
                } else if (error.status === 400) {
                    this.errorMessage = error.error?.message || 'Invalid flight data. Please check your inputs.';
                } else {
                    this.errorMessage = 'Failed to add flight. Please try again.';
                }
            }
        });
    }

    goBack(): void {
        this.router.navigate(['/']);
    }
}
