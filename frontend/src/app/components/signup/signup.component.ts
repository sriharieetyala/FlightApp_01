import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, RouterModule } from '@angular/router';
import { AuthService } from '../../services/auth.service';
import { SignupRequest } from '../../models/auth.models';

@Component({
  selector: 'app-signup',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule],
  templateUrl: './signup.component.html',
  styleUrl: './signup.component.css'
})
export class SignupComponent {
  signupForm: SignupRequest = {
    username: '',
    password: '',
    adminSecret: ''
  };

  confirmPassword: string = '';
  errorMessage: string = '';
  successMessage: string = '';
  isLoading: boolean = false;

  constructor(
    private authService: AuthService,
    private router: Router
  ) {}

  onSubmit(): void {
    if (!this.signupForm.username || !this.signupForm.password) {
      this.errorMessage = 'Please fill in all required fields';
      return;
    }

    if (this.signupForm.password !== this.confirmPassword) {
      this.errorMessage = 'Passwords do not match';
      return;
    }

    if (this.signupForm.password.length < 6) {
      this.errorMessage = 'Password must be at least 6 characters';
      return;
    }

    this.isLoading = true;
    this.errorMessage = '';
    this.successMessage = '';

    // Remove adminSecret if empty
    const request: SignupRequest = {
      username: this.signupForm.username,
      password: this.signupForm.password
    };

    if (this.signupForm.adminSecret) {
      request.adminSecret = this.signupForm.adminSecret;
    }

    console.log('Attempting signup with:', { username: request.username });

    this.authService.signup(request).subscribe({
      next: (response) => {
        console.log('Signup successful:', response);
        this.isLoading = false;
        this.successMessage = 'Account created successfully! Redirecting to login...';
        setTimeout(() => {
          this.router.navigate(['/login']);
        }, 2000);
      },
      error: (error) => {
        console.error('Signup error:', error);
        console.error('Error status:', error.status);
        console.error('Error details:', error.error);
        this.isLoading = false;
        
        if (error.status === 0) {
          this.errorMessage = 'Cannot connect to server. Please ensure the backend is running on port 8080.';
        } else if (error.status === 400) {
          this.errorMessage = error.error?.message || error.error || 'Failed to create account. Username may already exist.';
        } else {
          this.errorMessage = error.error?.message || error.message || 'An error occurred. Please try again.';
        }
      }
    });
  }
}

