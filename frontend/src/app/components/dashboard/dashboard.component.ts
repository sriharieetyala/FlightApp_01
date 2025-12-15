import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { FlightService } from '../../services/flight.service';
import { Flight, SearchFlightRequest } from '../../models/flight.models';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './dashboard.component.html',
  styleUrl: './dashboard.component.css'
})
export class DashboardComponent implements OnInit {
  flights: Flight[] = [];
  searchResults: Flight[] = [];
  isSearching = false;
  showAllFlights = true;
  errorMessage: string = '';
  infoMessage: string = '';

  searchForm: SearchFlightRequest = {
    fromCity: '',
    toCity: '',
    travelDate: ''
  };

  constructor(private flightService: FlightService) {}

  ngOnInit(): void {
    this.loadAllFlights();
  }

  loadAllFlights(): void {
    this.errorMessage = '';
    this.infoMessage = '';
    this.flightService.getAllFlights().subscribe({
      next: (flights) => {
        console.log('Loaded flights:', flights);
        this.flights = flights;
        this.showAllFlights = true;
        if (flights.length === 0) {
          this.infoMessage = 'No flights available at the moment.';
        }
      },
      error: (error) => {
        console.error('Error loading flights:', error);
        console.error('Error status:', error.status);
        console.error('Error details:', error.error);
        if (error.status === 0) {
          this.errorMessage = 'Cannot connect to server. Please ensure the backend is running on port 8080.';
        } else {
          this.errorMessage = 'Error loading flights. Please try again later.';
        }
      }
    });
  }

  onSearch(): void {
    if (!this.searchForm.fromCity || !this.searchForm.toCity || !this.searchForm.travelDate) {
      this.errorMessage = 'Please fill in all search fields';
      return;
    }

    this.isSearching = true;
    this.errorMessage = '';
    this.infoMessage = '';
    
    // Format date as YYYY-MM-DD for backend (HTML date input already provides this format)
    const searchRequest: SearchFlightRequest = {
      fromCity: this.searchForm.fromCity.trim().toUpperCase(),
      toCity: this.searchForm.toCity.trim().toUpperCase(),
      travelDate: this.searchForm.travelDate // Already in YYYY-MM-DD format from date input
    };

    console.log('Searching with:', searchRequest);
    console.log('Full request URL will be: /flight-service/flights/search');
    
    this.flightService.searchFlights(searchRequest).subscribe({
      next: (flights) => {
        console.log(' Search successful! Results:', flights);
        this.searchResults = flights;
        this.showAllFlights = false;
        this.isSearching = false;
        this.errorMessage = '';
        if (flights.length === 0) {
          this.infoMessage = 'No flights available for your search criteria. Please try different dates or cities.';
        } else {
          this.infoMessage = `Found ${flights.length} flight(s) matching your search.`;
        }
      },
      error: (error) => {
        console.error(' Error searching flights:', error);
        console.error('Error status:', error.status);
        console.error('Error statusText:', error.statusText);
        console.error('Error URL:', error.url);
        console.error('Error details:', error.error);
        this.isSearching = false;
        
        // Handle 404 as "no flights found" - this is the expected behavior when search returns no results
        if (error.status === 404) {
          // Check if it's a flight not found error (from backend) or actual endpoint not found
          const errorMessage = error.error || error.message || '';
          if (errorMessage.includes('No flights found') || errorMessage.includes('Flight Not Found')) {
            // This is a valid "no flights" response - show friendly message
            this.errorMessage = '';
            this.infoMessage = 'No flights available for your search criteria. Please try different dates or cities.';
            this.searchResults = []; // Clear any previous results
            this.showAllFlights = false;
          } else {
            // This is an actual endpoint error
            this.errorMessage = 'Search endpoint not found. Please check if the backend is properly configured.';
          }
        } else if (error.status === 0 || error.status === undefined) {
          this.errorMessage = 'Cannot connect to server. Please ensure the backend is running on port 8080.';
        } else if (error.status === 400) {
          this.errorMessage = error.error?.message || 'Invalid search criteria. Please check your input.';
        } else if (error.status === 500) {
          this.errorMessage = 'Server error. Please check backend logs.';
        } else {
          this.errorMessage = error.error?.message || `Error (${error.status}): ${error.statusText || 'Unknown error'}`;
        }
      }
    });
  }

  getDisplayFlights(): Flight[] {
    return this.showAllFlights ? this.flights : this.searchResults;
  }

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

