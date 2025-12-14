import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Flight, SearchFlightRequest } from '../models/flight.models';

@Injectable({
  providedIn: 'root'
})
export class FlightService {
  // Direct URL - proxy will handle routing
  private apiUrl = '/flight-service/flights';

  constructor(private http: HttpClient) {}

  getAllFlights(): Observable<Flight[]> {
    return this.http.get<Flight[]>(this.apiUrl);
  }

  searchFlights(request: SearchFlightRequest): Observable<Flight[]> {
    console.log('FlightService: Making POST request to:', `${this.apiUrl}/search`);
    console.log('FlightService: Request body:', request);
    return this.http.post<Flight[]>(`${this.apiUrl}/search`, request);
  }
}

