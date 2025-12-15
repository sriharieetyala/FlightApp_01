import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { FlightService } from '../../services/flight.service';
import { Flight, SearchFlightRequest } from '../../models/flight.models';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  // CommonModule and FormsModule are required for structural directives and ngModel
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

  // Bound to the search form inputs
  searchForm: SearchFlightRequest = {
    fromCity: '',
    toCity: '',
    travelDate: ''
  };

  // FlightService is injected to interact with backend APIs
  constructor(private flightService: FlightService) {}

  // Loads flight data when the dashboard initializes
  ngOnInit(): void {
    this.loadAllFlights();
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
}
