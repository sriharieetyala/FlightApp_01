import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { LoginRequest, LoginResponse, SignupRequest, SignupResponse } from '../models/auth.models';

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  // Direct URL to API Gateway (CORS enabled)
  private apiUrl = 'http://localhost:8080/auth';

  constructor(private http: HttpClient) { }

  login(request: LoginRequest): Observable<LoginResponse> {
    return this.http.post<LoginResponse>(`${this.apiUrl}/login`, request);
  }

  signup(request: SignupRequest): Observable<SignupResponse> {
    return this.http.post<SignupResponse>(`${this.apiUrl}/signup`, request);
  }

  logout(): void {
    localStorage.removeItem('token');
    localStorage.removeItem('username');
    localStorage.removeItem('email');
    localStorage.removeItem('role');
  }

  getToken(): string | null {
    return localStorage.getItem('token');
  }

  isAuthenticated(): boolean {
    return !!this.getToken();
  }

  getUsername(): string | null {
    return localStorage.getItem('username');
  }

  getEmail(): string | null {
    return localStorage.getItem('email');
  }

  getRole(): string | null {
    return localStorage.getItem('role');
  }

  saveAuthData(response: LoginResponse): void {
    localStorage.setItem('token', response.token);
    localStorage.setItem('username', response.username);
    localStorage.setItem('email', response.email);
    localStorage.setItem('role', response.role);
  }

  isAdmin(): boolean {
    return this.getRole() === 'ADMIN';
  }
}

