import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, FormGroup, Validators, AbstractControl, ValidationErrors } from '@angular/forms';
import { Router, RouterModule } from '@angular/router';
import { AuthService } from '../../services/auth.service';
import { SignupRequest } from '../../models/auth.models';

@Component({
  selector: 'app-signup',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterModule],
  templateUrl: './signup.component.html',
  styleUrl: './signup.component.css'
})
export class SignupComponent {

  // Reactive form group for signup
  signupForm: FormGroup;

  // Displays validation or backend errors
  errorMessage: string = '';

  // Displays success feedback after account creation
  successMessage: string = '';

  // Controls loading state during signup process
  isLoading: boolean = false;

  // AuthService handles signup logic, Router is used for redirection
  constructor(
    private authService: AuthService,
    private router: Router,
    private fb: FormBuilder
  ) {
    // Initialize form with validators
    this.signupForm = this.fb.group({
      username: ['', Validators.required],
      email: ['', [Validators.required, Validators.email]],
      password: ['', [
        Validators.required,
        Validators.minLength(6),
        this.uppercaseValidator,
        this.lowercaseValidator,
        this.specialCharValidator
      ]],
      confirmPassword: ['', Validators.required],
      adminSecret: ['']
    });
  }

  // Custom validator for uppercase letter
  uppercaseValidator(control: AbstractControl): ValidationErrors | null {
    const value = control.value;
    if (!value) return null;
    return /[A-Z]/.test(value) ? null : { noUppercase: true };
  }

  // Custom validator for lowercase letter
  lowercaseValidator(control: AbstractControl): ValidationErrors | null {
    const value = control.value;
    if (!value) return null;
    return /[a-z]/.test(value) ? null : { noLowercase: true };
  }

  // Custom validator for special character
  specialCharValidator(control: AbstractControl): ValidationErrors | null {
    const value = control.value;
    if (!value) return null;
    return /[!@#$%^&*(),.?":{}|<>_\-+=\[\]\\/~`]/.test(value) ? null : { noSpecialChar: true };
  }

  // Password validation helpers for real-time feedback (used in template)
  get hasMinLength(): boolean {
    const password = this.signupForm.get('password')?.value || '';
    return password.length >= 6;
  }

  get hasUppercase(): boolean {
    const password = this.signupForm.get('password')?.value || '';
    return /[A-Z]/.test(password);
  }

  get hasLowercase(): boolean {
    const password = this.signupForm.get('password')?.value || '';
    return /[a-z]/.test(password);
  }

  get hasSpecialChar(): boolean {
    const password = this.signupForm.get('password')?.value || '';
    return /[!@#$%^&*(),.?":{}|<>_\-+=\[\]\\/~`]/.test(password);
  }

  get isPasswordValid(): boolean {
    return this.hasMinLength && this.hasUppercase && this.hasLowercase && this.hasSpecialChar;
  }

  onSubmit(): void {
    const formValue = this.signupForm.value;

    // Basic validation to avoid empty requests
    if (!formValue.username || !formValue.email || !formValue.password) {
      this.errorMessage = 'Please fill in all required fields';
      return;
    }

    // Validates email format
    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    if (!emailRegex.test(formValue.email)) {
      this.errorMessage = 'Please enter a valid email address';
      return;
    }

    // Ensures password and confirm password match
    if (formValue.password !== formValue.confirmPassword) {
      this.errorMessage = 'Passwords do not match';
      return;
    }

    // Validates password strength
    if (!this.isPasswordValid) {
      this.errorMessage = 'Password does not meet all requirements';
      return;
    }

    this.isLoading = true;
    this.errorMessage = '';
    this.successMessage = '';

    // Constructs request object and excludes adminSecret if not provided
    const request: SignupRequest = {
      username: formValue.username,
      email: formValue.email,
      password: formValue.password
    };

    if (formValue.adminSecret) {
      request.adminSecret = formValue.adminSecret;
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
