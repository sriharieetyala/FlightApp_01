import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, RouterModule } from '@angular/router';
import { AuthService } from '../../services/auth.service';
import { SignupRequest } from '../../models/auth.models';

@Component({
  selector: 'app-signup',
  standalone: true,
  // FormsModule handles form binding, RouterModule enables navigation after signup
  imports: [CommonModule, FormsModule, RouterModule],
  templateUrl: './signup.component.html',
  styleUrl: './signup.component.css'
})
export class SignupComponent {

  // Holds user input for signup request
  signupForm: SignupRequest = {
    username: '',
    password: '',
    adminSecret: ''
  };

  // Used to confirm password match on UI level
  confirmPassword: string = '';

  // Displays validation or backend errors
  errorMessage: string = '';

  // Displays success feedback after account creation
  successMessage: string = '';

  // Controls loading state during signup process
  isLoading: boolean = false;

  // AuthService handles signup logic, Router is used for redirection
  constructor(
    private authService: AuthService,
    private router: Router
  ) {}

  onSubmit(): void {
    // Basic validation to avoid empty requests
    if (!this.signupForm.username || !this.signupForm.password) {
      this.errorMessage = 'Please fill in all required fields';
      return;
    }

    // Ensures password and confirm password match
    if (this.signupForm.password !== this.confirmPassword) {
      this.errorMessage = 'Passwords do not match';
      return;
    }

    // Enforces minimum password length on client side
    if (this.signupForm.password.length < 6) {
      this.errorMessage = 'Password must be at least 6 characters';
      return;
    }

    this.isLoading = true;
    this.errorMessage = '';
    this.successMessage = '';

    // Constructs request object and excludes adminSecret if not provided
    const request: SignupRequest = {
      username: this.signupForm.username,
      password: this.signupForm.password
    };

    if (this.signupForm.adminSecret) {
      request.adminSecret = this.signupForm.adminSecret;
    }

    // Logs only username for debugging
    console.log('Attempting signup with:', { username: request.username });

    // Calls backend signup API through AuthService
    this.authService.signup(request).subscribe({
      next: (response) => {
        this.isLoading = false;

        // Shows success message before redirecting to login
        this.successMessage = 'Account created successfully! Redirecting to login...';

        setTimeout(() => {
          this.router.navigate(['/login']);
        }, 2000);
      },
      error: (error) => {
        this.isLoading = false;

        // Handles backend or network failure
        if (error.status === 0) {
          this.errorMessage = 'Cannot connect to server. Please ensure the backend is running on port 8080.';
        } 
        // Handles validation or duplicate user errors
        else if (error.status === 400) {
          this.errorMessage = error.error?.message || error.error || 'Failed to create account. Username may already exist.';
        } 
        // Generic fallback for unexpected errors
        else {
          this.errorMessage = error.error?.message || error.message || 'An error occurred. Please try again.';
        }
      }
    });
  }
}
