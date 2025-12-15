# FlightApp – Full Stack Flight Booking Application

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



Just say the word 👍
