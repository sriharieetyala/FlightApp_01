import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, RouterModule } from '@angular/router';
import { AuthService } from '../../services/auth.service';
import { LoginRequest } from '../../models/auth.models';

@Component({
  selector: 'app-login',
  standalone: true,
  // FormsModule is required for ngModel, RouterModule for navigation after login
  imports: [CommonModule, FormsModule, RouterModule],
  templateUrl: './login.component.html',
  styleUrl: './login.component.css'
})
export class LoginComponent {

  // Bound to login form inputs
  loginForm: LoginRequest = {
    username: '',
    password: ''
  };

  // Displays authentication or validation errors
  errorMessage: string = '';

  // Used to control loading state during login request
  isLoading: boolean = false;

  // AuthService handles login logic, Router is used for redirection
  constructor(
    private authService: AuthService,
    private router: Router
  ) {}

  onSubmit(): void {
    // Prevents empty login requests
    if (!this.loginForm.username || !this.loginForm.password) {
      this.errorMessage = 'Please fill in all fields';
      return;
    }

    this.isLoading = true;
    this.errorMessage = '';

    // Logging only the username for debugging (password excluded)
    console.log('Attempting login with:', { username: this.loginForm.username });

    // Calls backend login API through AuthService
    this.authService.login(this.loginForm).subscribe({
      next: (response) => {
        // Saves token/user data on successful authentication
        this.authService.saveAuthData(response);
        this.isLoading = false;

        // Redirects user after successful login
        this.router.navigate(['/']);
      },
      error: (error) => {
        this.isLoading = false;

        // Status 0 usually indicates backend or network issue
        if (error.status === 0) {
          this.errorMessage = 'Cannot connect to server. Please ensure the backend is running on port 8080.';
        } 
        // Handles invalid credentials or bad request
        else if (error.status === 400 || error.status === 401) {
          this.errorMessage = error.error?.message || error.error || 'Invalid username or password';
        } 
        // Fallback for unexpected errors
        else {
          this.errorMessage = error.error?.message || error.message || 'An error occurred. Please try again.';
        }
      }
    });
  }
}
