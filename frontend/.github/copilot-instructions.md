# Flight Booking Application - Frontend (Angular 20)

## Architecture Overview

This is a **standalone component-based Angular 20** application that serves as the frontend for a flight booking system. It communicates with a backend REST API running on `localhost:8080` via Angular's dev proxy.

**Key architectural decisions:**
- Uses Angular's **standalone components** (no NgModules) throughout
- Implements **lazy-loaded routes** for all page components
- Uses **class-based HTTP interceptor** with `withInterceptorsFromDi()` for JWT token injection
- Stores auth state in **localStorage** (token, username, role)
- No route guards implemented - components check auth status internally

## Project Structure

```
src/app/
├── components/          # UI components (dashboard, login, signup, header)
├── services/           # Business logic (AuthService, FlightService)
├── interceptors/       # HTTP interceptors (AuthInterceptor)
├── models/             # TypeScript interfaces for API contracts
├── app.config.ts       # Application providers configuration
└── app.routes.ts       # Route definitions with lazy loading
```

## Development Workflow

**Run development server:**
```bash
npm start
# Starts on http://localhost:4200 with proxy to backend on :8080
```

**Run tests:**
```bash
npm test
# Karma + Jasmine test runner
```

**Build for production:**
```bash
npm run build
# Output in dist/ directory
```

## Critical Patterns & Conventions

### Component Patterns
- All components use `standalone: true` with explicit `imports` array
- Include `CommonModule` for structural directives (`*ngIf`, `*ngFor`)
- Include `FormsModule` for template-driven forms with `[(ngModel)]`
- Separation: `.ts`, `.html`, `.css` files (never inline)
- Component selector prefix: `app-` (e.g., `app-header`)

### Service Architecture
- Services use relative API URLs (e.g., `/auth`, `/flight-service`) that rely on [proxy.conf.json](proxy.conf.json)
- **Important:** Backend proxy routes `/auth` and `/flight-service` to `http://localhost:8080`
- All services are `providedIn: 'root'` for singleton behavior
- HTTP calls return raw Observables - components handle subscription

### Error Handling Convention
**Status 0** (network/CORS error) is treated specially across all components:
```typescript
if (error.status === 0) {
  this.errorMessage = 'Cannot connect to server. Please ensure the backend is running on port 8080.';
}
```

**Status 404** on search operations is valid "no results" (see [dashboard.component.ts](src/app/components/dashboard/dashboard.component.ts#L118-L122))

### Authentication Flow
1. [AuthInterceptor](src/app/interceptors/auth.interceptor.ts) automatically adds `Authorization: Bearer <token>` header
2. Login/signup responses save to localStorage via `AuthService.saveAuthData()`
3. Header component subscribes to router events to update auth state on navigation
4. Logout clears localStorage and navigates to `/`

### TypeScript Configuration
- **Strict mode enabled** (`strict: true`) - enforce type safety
- `strictTemplates: true` - strict type checking in Angular templates
- `experimentalDecorators: true` - required for Angular decorators

### Code Style (Prettier Config)
- `printWidth: 100`
- `singleQuote: true`
- HTML parser: `angular` for proper template formatting

## Backend Integration

**API Endpoints (proxied through Angular):**
- `POST /auth/login` - Returns `{ token, username, role }`
- `POST /auth/signup` - Creates user account
- `GET /flight-service/flights` - Fetch all flights
- `POST /flight-service/flights/search` - Search with `{ fromCity, toCity, travelDate }`

**Data models:** See [auth.models.ts](src/app/models/auth.models.ts) and [flight.models.ts](src/app/models/flight.models.ts) for API contracts.

## Common Tasks

**Add a new route:**
1. Create component in `src/app/components/`
2. Add lazy-loaded route to [app.routes.ts](src/app/app.routes.ts):
   ```typescript
   {
     path: 'new-page',
     loadComponent: () => import('./components/new-page/new-page.component').then(m => m.NewPageComponent)
   }
   ```

**Add a new service:**
1. Create in `src/app/services/`
2. Use `@Injectable({ providedIn: 'root' })`
3. Inject `HttpClient` for API calls
4. Use relative URLs - proxy handles routing

**Handle authentication in new component:**
```typescript
constructor(private authService: AuthService, private router: Router) {}

checkAuth() {
  if (!this.authService.isAuthenticated()) {
    this.router.navigate(['/login']);
  }
}
```

## Important Notes

- **No route guards** - auth checks happen inside components
- **City codes** must be UPPERCASE (e.g., "HYD", "VIJ") - see [dashboard.component.ts](src/app/components/dashboard/dashboard.component.ts#L91-L95)
- **Date inputs** use ISO format (`YYYY-MM-DD`) for consistency
- **Currency display** uses Indian Rupees (₹) symbol
- Comments in components use business-logic style (see [dashboard.component.ts](src/app/components/dashboard/dashboard.component.ts) for reference)
