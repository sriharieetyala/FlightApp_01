export interface Flight {
  id: number;
  flightNumber: string;
  fromCity: string;
  toCity: string;
  departureTime: string;
  arrivalTime: string;
  cost: number;
  seatsAvailable: number;
}

export interface SearchFlightRequest {
  fromCity: string;
  toCity: string;
  travelDate: string; // ISO date string
}

