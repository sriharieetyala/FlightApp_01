import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { AuthService } from '../../services/auth.service';

@Component({
    selector: 'app-change-password',
    standalone: true,
    imports: [CommonModule, FormsModule],
    templateUrl: './change-password.component.html',
    styleUrl: './change-password.component.css'
})
export class ChangePasswordComponent {

    // Form fields
    currentPassword = '';
    newPassword = '';
    confirmPassword = '';

    // UI state
    isSubmitting = false;
    errorMessage = '';
    successMessage = '';

    constructor(
        private authService: AuthService,
        private router: Router
    ) {
        // Redirect to login if not authenticated
        if (!this.authService.isAuthenticated()) {
            this.router.navigate(['/login']);
        }
    }

    // Check if form is valid before submitting
    isFormValid(): boolean {
        return (
            this.currentPassword.length > 0 &&
            this.newPassword.length >= 6 &&
            this.newPassword !== this.currentPassword &&
            this.newPassword === this.confirmPassword
        );
    }

    // Show validation errors while typing
    getPasswordError(): string {
        if (this.newPassword.length > 0 && this.newPassword.length < 6) {
            return 'Password must be at least 6 characters';
        }
        if (this.newPassword.length > 0 && this.newPassword === this.currentPassword) {
            return 'New password must be different from current password';
        }
        if (this.confirmPassword.length > 0 && this.newPassword !== this.confirmPassword) {
            return 'Passwords do not match';
        }
        return '';
    }

    // Handle form submission
    onSubmit(): void {
        if (!this.isFormValid()) {
            this.errorMessage = this.getPasswordError() || 'Please fill all fields correctly';
            return;
        }

        this.isSubmitting = true;
        this.errorMessage = '';
        this.successMessage = '';

        this.authService.changePassword(this.currentPassword, this.newPassword).subscribe({
            next: () => {
                this.isSubmitting = false;
                this.successMessage = 'Password changed successfully! Redirecting...';
                setTimeout(() => this.router.navigate(['/']), 1500);
            },
            error: (error) => {
                this.isSubmitting = false;
                if (error.status === 400) {
                    this.errorMessage = 'Current password is incorrect';
                } else {
                    this.errorMessage = 'Failed to change password. Please try again.';
                }
            }
        });
    }

}
