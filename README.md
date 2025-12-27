---

# FlightApp – Full Stack Flight Booking Application

---

## 1. Project Overview

FlightApp is a full-stack flight booking application developed with an **Angular frontend** and a **Spring Boot microservices backend**.

---

## 2. RBAC – Role Based Access Control

### RBAC (Role-Based Access Control)

* Only **ADMIN** role can add flights
* Frontend hides **Add Flight** button for regular users
* Backend checks user role before adding flight
* **USER** role can only view and book flights

---

## 3. Admin Signup

<img width="1149" height="609" alt="image" src="https://github.com/user-attachments/assets/b4f8cf18-1f44-46b9-8935-33b24451cef6" />


* During signup, user can provide an **admin secret**
* If admin secret is correct → role is set as **ADMIN** in database
* If admin secret is missing or wrong → role is set as **USER** (default)

---

## 4. Role Storage (Inside Database)

* Admin role is stored in the database
* Role is fetched after login and used for authorization

<img width="1132" height="371" alt="image" src="https://github.com/user-attachments/assets/8e509080-0713-4514-a005-f3bc4abbbf91" />


---

## 5. After Login as ADMIN

* Add Flight button is visible
* Admin can add new flights
* Admin can view all flights
* <img width="1040" height="592" alt="image" src="https://github.com/user-attachments/assets/26bab6e1-1e39-4036-841f-b4fdd71b8677" />


---

## 6. USER Role

* If admin secret is missing or wrong → **USER** role (default)
* USER role permissions:
<img width="1095" height="589" alt="image" src="https://github.com/user-attachments/assets/a5bacfe5-662d-425c-946c-7cda407b30f7" />

  * View flights
  * Book flights
* USER cannot add flights

##Inside database

<img width="1100" height="272" alt="image" src="https://github.com/user-attachments/assets/45e7a6bf-2da1-4406-a9d0-b39c3663b3be" />



---

## 7. After Login as USER

* Add Flight button is hidden
* User can only view and book flights
  <img width="1066" height="569" alt="image" src="https://github.com/user-attachments/assets/824d0635-4917-4e9d-b95c-d135c20c2e03" />


---

## 8. Adding Flights (Only ADMIN Role)

* Created **Add Flight** page for admins
* Form to enter flight details:

  * Source
  * Destination
  * Date
  * Price
  * Seats
* Submits to **POST /flights** API
* Redirects to dashboard on success
 ---

## 9. Add Flight Button Visibility

* Add Flight button is visible **only if the role is ADMIN**
* Frontend hides Add Flight button for regular users
  <img width="1040" height="592" alt="image" src="https://github.com/user-attachments/assets/e270bcae-c3a3-4794-a93a-c9569bcba4bd" />


---

## 10. Add Flight Form

Form to fill the details to add flights.
<img width="1080" height="577" alt="image" src="https://github.com/user-attachments/assets/27c63b4e-a591-44e1-bfe9-f36d66d68dc2" />

---

## 11. Dashboard Update

* Successfully added flights appear in the dashboard
  <img width="940" height="322" alt="image" src="https://github.com/user-attachments/assets/4bde4766-30ec-4a8e-b444-8ffa1d5cf853" />


---

## 12. Validations Implemented

| Field / Validation Type   | Rule                                       |
| ------------------------- | ------------------------------------------ |
| Flight Number             | Cannot be empty                            |
| From City                 | Cannot be empty                            |
| To City                   | Cannot be empty                            |
| Departure Time            | Must be selected (minimum = current time)  |
| Arrival Time              | Must be selected                           |
| Cost                      | Must be greater than 0                     |
| Seats Available           | Must be greater than 0                     |
| From City & To City       | Cannot be the same city                    |
| Flight Number (on submit) | Converted to UPPERCASE                     |
| City Names (on submit)    | Converted to UPPERCASE                     |
| Duplicate Flight Number   | Show error: "Flight number already exists" |

---

## 13. Validation Example

This is an example validation where the **price is not provided** and the **Add button is disabled**.
<img width="680" height="609" alt="image" src="https://github.com/user-attachments/assets/1f8f9ee7-a563-47a0-821c-fdc76e6ba5eb" />

---

## 14. Change Password Feature

### Backend (API)

* New API endpoint: **PUT /auth/change-password**
* Request body:

  ```json
  {
    "currentPassword": "",
    "newPassword": ""
  }
  ```
* Username received via **X-User-Name** header
* Created **ChangePasswordRequest DTO**
<img width="940" height="412" alt="image" src="https://github.com/user-attachments/assets/4e273e93-b7de-46fe-81ea-2fa15ba211c0" />

---

### Frontend (UI)

* New page at **/change-password** route
  <img width="1069" height="555" alt="image" src="https://github.com/user-attachments/assets/7771a397-7df6-42d7-9782-6610f171ebf7" />

* Form with 3 fields:

  * Current password
  * New password
  * Confirm password
  <img width="1072" height="432" alt="image" src="https://github.com/user-attachments/assets/9b29e8d1-38ca-46e5-a56f-3c980f771e73" />

* Real-time validation errors shown below fields
* Redirects to home page after success

---

## 15. Spring Profiles (Two Properties Files)

**Note:** Profile configuration is applied to all microservices.
Screenshot shown is for **auth-service** as reference.
<img width="1119" height="790" alt="image" src="https://github.com/user-attachments/assets/db0d5788-cf70-46f0-9947-5c8595ad15ca" />


### Files

* `application.properties` → default profile (local)
* `application-docker.properties` → docker profile

### How It Works

* Docker Compose sets `SPRING_PROFILES_ACTIVE=docker`
* Spring Boot loads `application-docker.properties`
* Overrides values from `application.properties`
* Container names used instead of localhost

---

## 16. Dockerfile Optimization

### Before (Two-Stage Build)
<img width="855" height="303" alt="image" src="https://github.com/user-attachments/assets/f066ba2b-b627-45f2-83b5-ee544cfc0177" />


* JAR built inside container

### After (Single-Stage with Pre-built JAR)
<img width="563" height="188" alt="image" src="https://github.com/user-attachments/assets/b3ff3521-db5b-4280-be23-18c0ecd4d402" />


* JAR built locally and copied to container

### Benefits Found Using This Approach

* Faster build time (JAR built locally, not inside container)
* Smaller image size (no JDK, only JRE)
* Simpler Dockerfile (7 lines vs 17 lines)

---

## 17. SWOT Analysis of FlightApp

### Strengths

* Built with microservices so each part can be updated separately
* Role-based access keeps admin features secure
* Passwords are encrypted, not stored in plain text
* Docker makes it easy to run anywhere
* Spring profiles let us switch between local and production easily

---

### Weaknesses

* No forgot password or email reset option yet
* Flight search could be more advanced (filters, sorting)
* No payment integration for bookings
* Frontend has limited error handling in some places

---

### Opportunities

* Advanced search with filters like price range, airlines, stops
* Forgot password feature using email verification
* Add seat selection during booking

---

### Threats

* No rate limiting means possible spam or brute force attacks
* Hardcoded secrets (JWT secret, admin secret) in properties files
* No logging for failed login attempts (can't detect attacks)
* No seat locking during booking could cause double booking

---

#Jenkins testing

#Assignment(Before RBAC)
---

## 1. Project Overview

FlightApp is a full-stack flight booking application developed with an **Angular frontend** and a **Spring Boot microservices backend**.

---





## 2. Backend Usage

The backend used in this project is **reused from the previous assignment**.
No changes were made to the existing backend logic or microservice functionality but a simple **reconfiguration of docker-compose.yml**

---

## 3. Docker Port Reconfiguration

| Service Name    | Container Port | Host Port |
| --------------- | -------------- | --------- |
| PostgreSQL      | 5432           | 5433      |
| Config Server   | 8888           | 9888      |
| Eureka Server   | 8761           | 9761      |
| API Gateway     | 8080           | 8080      |
| Auth Service    | 9000           | 9900      |
| Flight Service  | 8081           | 9081      |
| Booking Service | 8082           | 9082      |
| Email Service   | 8085           | 9085      |

---

## 4. APIs Used by Frontend (via API Gateway)

All frontend requests are routed through:

```
http://localhost:8080
```

### Authentication APIs

| Frontend Feature | API Endpoint   | Method |
| ---------------- | -------------- | ------ |
| Signup           | `/auth/signup` | POST   |
| Login            | `/auth/login`  | POST   |

* USER role is assigned by default
* ADMIN role requires a valid admin secret key

---

### Flight APIs

| Frontend Feature        | API Endpoint                     | Method |
| ----------------------- | -------------------------------- | ------ |
| Dashboard (All Flights) | `/flight-service/flights`        | GET    |
| Search Flights          | `/flight-service/flights/search` | POST   |

---

## 5. Frontend Implementation

### Technologies Used

* Angular 20
* TypeScript
* HTML
* CSS
* Angular HttpClient
* HTTP Interceptors

---

## 6. Frontend Pages & Flow

### Dashboard

* URL: `http://localhost:4200/dashboard`
* API Call: `GET /flight-service/flights`
* Displays all available flights
* Accessible without login
  <img width="1110" height="650" alt="image" src="https://github.com/user-attachments/assets/c141e0df-85e2-4c0d-a781-07cfa44e34cc" />
  
  <img width="1094" height="636" alt="image" src="https://github.com/user-attachments/assets/e30b2e12-ee8d-410a-b99b-5accb7f2a3e2" />


---

### Flight Search

* Search by source, destination, and date
* URL: `http://localhost:4200/dashboard`
* API Call: `POST /flight-service/flights/search`
* Displays matching flights dynamically
* Shows **“No flights found”** if no data matches

* Displaying flights that are requested:
* <img width="1104" height="639" alt="image" src="https://github.com/user-attachments/assets/3ac2abcb-63d6-4b1a-a638-490036db1bac" />
*  If there is no flights that matches the search conditions:
<img width="1103" height="620" alt="image" src="https://github.com/user-attachments/assets/d530f7cc-a0c5-438a-8f60-96d5bf1a25a4" />



*(Screenshots attached for both successful and empty search results)*

---

### Signup

* URL: `http://localhost:4200/signup`
* API Call: `POST /auth/signup`
* Validated registration form

**Validation Behavior**

* If the signup form does not receive valid inputs, the user is prompted to fill in the required details.
* <img width="1082" height="609" alt="image" src="https://github.com/user-attachments/assets/c3a402d3-5d40-42e8-ad84-263424034faf" />


**Successful Signup**

* Displays a success message
* Automatically redirects the user to the login page
* <img width="1062" height="610" alt="image" src="https://github.com/user-attachments/assets/03682fda-2eb6-4a1a-be9c-f2ba4257db27" />


---

### Login

* URL: `http://localhost:4200/login`
* API Call: `POST /auth/login`

**On Successful Login**
<img width="1043" height="574" alt="image" src="https://github.com/user-attachments/assets/180135aa-b380-4996-8c2f-d1546ec6ea2b" />


* JWT token is received
* User is redirected to the dashboard
* Logged-in username is displayed in the header

**On Unsuccessful Login**

* Error message is displayed for invalid credentials
<img width="1063" height="597" alt="image" src="https://github.com/user-attachments/assets/256776f8-3913-4bbd-a454-0b297695dced" />


---

### Logout

* Clears JWT token and user data from `localStorage`
* Returns the application to a public state

---

## 7. JWT Handling (Frontend)

* JWT token stored in `localStorage`
* Angular HTTP Interceptor automatically attaches:

  ```
  Authorization: Bearer <token>
  ```
* Public APIs remain unaffected
* Token expiry is handled by re-login

---

## 8. Conclusion

* Backend reused from the previous assignment
* Docker ports reconfigured using Docker Compose
* Angular frontend fully implemented
* Secure JWT-based communication via API Gateway
* Clear and structured frontend-to-backend integration




