import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterModule, NavigationEnd } from '@angular/router';
import { AuthService } from '../../services/auth.service';
import { filter } from 'rxjs/operators';

@Component({
  selector: 'app-header',
  standalone: true,
  // RouterModule is needed for routerLink and navigation-related features
  imports: [CommonModule, RouterModule],
  templateUrl: './header.component.html',
  styleUrl: './header.component.css'
})
export class HeaderComponent implements OnInit {

  // Tracks whether the user is logged in
  isAuthenticated: boolean = false;

  // Holds the logged-in username for display in header
  username: string | null = null;

  // Logout confirmation dialog
  showLogoutConfirm: boolean = false;

  // AuthService handles authentication logic
  // Router is used for navigation and listening to route changes
  constructor(
    private authService: AuthService,
    private router: Router
  ) {}

  ngOnInit(): void {
    // Initial auth check when header loads
    this.checkAuthStatus();

    // Updates authentication state whenever route changes
    this.router.events
      .pipe(filter(event => event instanceof NavigationEnd))
      .subscribe(() => {
        this.checkAuthStatus();
      });
  }

  // Syncs header state with authentication status
  checkAuthStatus(): void {
    this.isAuthenticated = this.authService.isAuthenticated();
    this.username = this.authService.getUsername();
  }

  logout(): void {
    this.showLogoutConfirm = true;
  }

  confirmLogout(): void {
    this.authService.logout();
    this.isAuthenticated = false;
    this.username = null;
    this.showLogoutConfirm = false;
    this.router.navigate(['/']);
  }

  cancelLogout(): void {
    this.showLogoutConfirm = false;
  }
}
