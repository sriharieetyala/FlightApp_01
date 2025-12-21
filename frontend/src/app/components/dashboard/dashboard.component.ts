import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { FlightService } from '../../services/flight.service';
import { AuthService } from '../../services/auth.service';
import { Flight, SearchFlightRequest } from '../../models/flight.models';



@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './dashboard.component.html',
  styleUrl: './dashboard.component.css'
})
export class DashboardComponent implements OnInit {

  // Holds all flights fetched on initial load
  flights: Flight[] = [];

  // Stores flights returned from search operation
  searchResults: Flight[] = [];

  // Controls search/loading state in UI
  isSearching = false;

  // Used to switch between all flights and search results
  showAllFlights = true;

  // Displays error-related feedback
  errorMessage: string = '';

  // Displays user-friendly informational messages
  infoMessage: string = '';

  // Validation message for same city error
  sameCityError: string = '';

  // Unique cities extracted from flights for autocomplete suggestions
  availableCities: string[] = [];

  // Filtered cities for dropdown suggestions
  filteredFromCities: string[] = [];
  filteredToCities: string[] = [];

  // Controls dropdown visibility
  showFromDropdown = false;
  showToDropdown = false;

  // Minimum date for travel (today)
  minDate: string = '';

  // Bound to the search form inputs
  searchForm: SearchFlightRequest = {
    fromCity: '',
    toCity: '',
    travelDate: ''
  };

  // Admin flag
  isAdmin = false;

  constructor(
    private readonly flightService: FlightService,
    private readonly authService: AuthService,
    private readonly router: Router
  ) { }

  ngOnInit(): void {
    this.isAdmin = this.authService.isAdmin();
    this.setMinDate();
    this.loadAllFlights();
  }

  // Sets minimum selectable date to today
  setMinDate(): void {
    const today = new Date();
    const year = today.getFullYear();
    const month = String(today.getMonth() + 1).padStart(2, '0');
    const day = String(today.getDate()).padStart(2, '0');
    this.minDate = `${year}-${month}-${day}`;
  }

  // Extracts unique cities from loaded flights
  extractCities(): void {
    const citySet = new Set<string>();
    for (const flight of this.flights) {
      citySet.add(flight.fromCity.toUpperCase());
      citySet.add(flight.toCity.toUpperCase());
    }
    this.availableCities = Array.from(citySet).sort((a, b) => a.localeCompare(b));
  }

  // Filters cities based on user input for From field
  onFromCityInput(): void {
    this.sameCityError = '';
    const input = this.searchForm.fromCity.trim().toUpperCase();
    if (input.length > 0) {
      this.filteredFromCities = this.availableCities.filter(city =>
        city.includes(input)
      );
      this.showFromDropdown = this.filteredFromCities.length > 0;
    } else {
      this.filteredFromCities = this.availableCities;
      this.showFromDropdown = true;
    }
    this.checkSameCity();
  }

  // Filters cities based on user input for To field
  onToCityInput(): void {
    this.sameCityError = '';
    const input = this.searchForm.toCity.trim().toUpperCase();
    if (input.length > 0) {
      this.filteredToCities = this.availableCities.filter(city =>
        city.includes(input)
      );
      this.showToDropdown = this.filteredToCities.length > 0;
    } else {
      this.filteredToCities = this.availableCities;
      this.showToDropdown = true;
    }
    this.checkSameCity();
  }

  // Selects a city from the From dropdown
  selectFromCity(city: string): void {
    this.searchForm.fromCity = city;
    this.showFromDropdown = false;
    this.checkSameCity();
  }

  // Selects a city from the To dropdown
  selectToCity(city: string): void {
    this.searchForm.toCity = city;
    this.showToDropdown = false;
    this.checkSameCity();
  }

  // Shows dropdown on focus
  onFromFocus(): void {
    this.filteredFromCities = this.availableCities;
    this.showFromDropdown = true;
  }

  onToFocus(): void {
    this.filteredToCities = this.availableCities;
    this.showToDropdown = true;
  }

  // Hides dropdown on blur with delay to allow click selection
  onFromBlur(): void {
    setTimeout(() => this.showFromDropdown = false, 200);
  }

  onToBlur(): void {
    setTimeout(() => this.showToDropdown = false, 200);
  }

  // Checks if source and destination are the same
  checkSameCity(): void {
    const from = this.searchForm.fromCity.trim().toUpperCase();
    const to = this.searchForm.toCity.trim().toUpperCase();
    if (from && to && from === to) {
      this.sameCityError = 'Source and destination cannot be the same';
    } else {
      this.sameCityError = '';
    }
  }

  // Validates if the search form is complete and valid
  isFormValid(): boolean {
    const hasFrom = this.searchForm.fromCity.trim().length > 0;
    const hasTo = this.searchForm.toCity.trim().length > 0;
    const hasDate = this.searchForm.travelDate.length > 0;
    const notSameCity = this.searchForm.fromCity.trim().toUpperCase() !== this.searchForm.toCity.trim().toUpperCase();
    return hasFrom && hasTo && hasDate && notSameCity;
  }

  loadAllFlights(): void {
    // Reset UI messages before API call
    this.errorMessage = '';
    this.infoMessage = '';

    // Fetches all flights from backend
    this.flightService.getAllFlights().subscribe({
      next: (flights) => {
        this.flights = flights;
        this.showAllFlights = true;

        // Extract unique cities for autocomplete suggestions
        this.extractCities();

        // Handles empty flight list scenario
        if (flights.length === 0) {
          this.infoMessage = 'No flights available at the moment.';
        }
      },
      error: (error) => {
        // Status 0 usually indicates backend/network issue
        if (error.status === 0) {
          this.errorMessage = 'Cannot connect to server. Please ensure the backend is running on port 8080.';
        } else {
          this.errorMessage = 'Error loading flights. Please try again later.';
        }
      }
    });
  }

  onSearch(): void {
    // Basic validation to prevent empty search requests
    if (!this.searchForm.fromCity || !this.searchForm.toCity || !this.searchForm.travelDate) {
      this.errorMessage = 'Please fill in all search fields';
      return;
    }

    this.isSearching = true;
    this.errorMessage = '';
    this.infoMessage = '';

    // Request is formatted to match backend expectations
    const searchRequest: SearchFlightRequest = {
      fromCity: this.searchForm.fromCity.trim().toUpperCase(),
      toCity: this.searchForm.toCity.trim().toUpperCase(),
      travelDate: this.searchForm.travelDate
    };

    // Executes flight search based on user criteria
    this.flightService.searchFlights(searchRequest).subscribe({
      next: (flights) => {
        this.searchResults = flights;
        this.showAllFlights = false;
        this.isSearching = false;

        // Feedback based on search result count
        if (flights.length === 0) {
          this.infoMessage = 'No flights available for your search criteria.';
        } else {
          this.infoMessage = `Found ${flights.length} flight(s) matching your search.`;
        }
      },
      error: (error) => {
        this.isSearching = false;

        // 404 is treated as a valid "no data" response from backend
        if (error.status === 404) {
          this.errorMessage = '';
          this.infoMessage = 'No flights available for your search criteria.';
          this.searchResults = [];
          this.showAllFlights = false;
        }
        // Handles backend down or connectivity issues
        else if (error.status === 0 || error.status === undefined) {
          this.errorMessage = 'Cannot connect to server. Please ensure the backend is running on port 8080.';
        }
        // Handles common backend error cases
        else if (error.status === 400) {
          this.errorMessage = 'Invalid search criteria. Please check your input.';
        } else if (error.status === 500) {
          this.errorMessage = 'Server error. Please check backend logs.';
        } else {
          this.errorMessage = `Error (${error.status}) occurred while searching flights.`;
        }
      }
    });
  }

  // Determines which flight list should be rendered in UI
  getDisplayFlights(): Flight[] {
    return this.showAllFlights ? this.flights : this.searchResults;
  }

  // Formats date for consistent display
  formatDate(dateString: string): string {
    const date = new Date(dateString);
    return date.toLocaleDateString('en-US', {
      year: 'numeric',
      month: 'short',
      day: 'numeric',
      hour: '2-digit',
      minute: '2-digit'
    });
  }

  // Navigate to booking page
  openBooking(flight: Flight): void {
    this.router.navigate(['/booking', flight.id]);
  }

  // Navigate to Add Flight page (Admin only)
  goToAddFlight(): void {
    this.router.navigate(['/add-flight']);
  }
}
