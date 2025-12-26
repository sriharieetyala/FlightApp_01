import { Component, OnInit } from '@angular/core';
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
export class ChangePasswordComponent implements OnInit {

    // Form fields
    currentPassword = '';
    newPassword = '';
    confirmPassword = '';

    // UI state
    isSubmitting = false;
    errorMessage = '';
    successMessage = '';

    // Flag to show password expired warning
    isPasswordExpired = false;

    constructor(
        private authService: AuthService,
        private router: Router
    ) { }

    ngOnInit(): void {
        // Redirect to login if not authenticated
        if (!this.authService.isAuthenticated()) {
            this.router.navigate(['/login']);
            return;
        }

        // Check if user was redirected due to password expiry
        const expiredFlag = localStorage.getItem('passwordExpired');
        if (expiredFlag === 'true') {
            this.isPasswordExpired = true;
        }
    }

    // Password strength validation helpers (same as signup)
    get hasMinLength(): boolean {
        return this.newPassword.length >= 6;
    }

    get hasUppercase(): boolean {
        return /[A-Z]/.test(this.newPassword);
    }

    get hasLowercase(): boolean {
        return /[a-z]/.test(this.newPassword);
    }

    get hasSpecialChar(): boolean {
        return /[!@#$%^&*(),.?":{}|<>_\-+=\[\]\\/~`]/.test(this.newPassword);
    }

    get isPasswordStrong(): boolean {
        return this.hasMinLength && this.hasUppercase && this.hasLowercase && this.hasSpecialChar;
    }

    // Check if form is valid before submitting
    isFormValid(): boolean {
        return (
            this.currentPassword.length > 0 &&
            this.isPasswordStrong &&
            this.newPassword !== this.currentPassword &&
            this.newPassword === this.confirmPassword
        );
    }

    // Show validation errors while typing
    getPasswordError(): string {
        if (this.newPassword.length > 0 && !this.isPasswordStrong) {
            return 'Password does not meet all requirements';
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

                // Clear password expired flag after successful change
                localStorage.removeItem('passwordExpired');
                this.isPasswordExpired = false;

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
