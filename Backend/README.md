
# FlightApp Microservices – Security + Docker

This project is a microservices-based flight booking system implemented using Spring Boot, Spring Cloud, Eureka, Docker, and JWT-based authentication. The platform provides secure user and admin operations such as flight search, flight management, and ticket bookings.

---

## Architecture Overview

The system consists of the following services:

| Service          | Purpose                                                      |
| ---------------- | ------------------------------------------------------------ |
| Config-Service   | Centralized configuration management for all services        |
| Service-Registry | Eureka server for service discovery                          |
| API-Gateway      | Single entry point, request routing and security enforcement |
| Auth-Service     | User and admin authentication, JWT token issuance            |
| Flight-Service   | Flight search and administrative flight management           |
| Booking-Service  | Ticket booking and query operations                          |
| Email-Service    | Sends booking confirmation emails through RabbitMQ           |

Communication between services happens through Eureka discovery and requests are routed via the API-Gateway.
<img width="3488" height="1216" alt="Architecture" src="https://github.com/user-attachments/assets/ed1f382b-7a4e-4aa8-a6d1-a4b6106d0b3c" />



---

## Security Implementation

Authentication is handled by Auth-Service which generates JWT tokens for valid user credentials.
Authorization is enforced in the API-Gateway using role-based access rules:

* **USER role** can access booking operations
* **ADMIN role** can manage flight creation

Public endpoints do not require authentication.
Token expiration is one hour and must be provided using:

```
Authorization: Bearer <jwt-token>
```

Logout is implemented by instructing the client to discard the token.

---

## Endpoint Access Rules

### Public Endpoints

| API             | Method | Access |
| --------------- | ------ | ------ |
| /auth/signup    | POST   | Public |
| /auth/login     | POST   | Public |
| /auth/logout    | POST   | Public |
| /flights        | GET    | Public |
| /flights/{id}   | GET    | Public |
| /flights/search | POST   | Public |

These endpoints support registration, login, and flight browsing.

---

### User Authorized Endpoints

| API                     | Method | Access |
| ----------------------- | ------ | ------ |
| /bookings               | POST   | USER   |
| /bookings/email/{email} | GET    | USER   |
| /bookings/id/{id}       | GET    | USER   |
| /bookings/pnr/{pnr}     | GET    | USER   |
| /bookings/{id}          | DELETE | USER   |

Users must include a valid token to manage their bookings.

---

### Admin Authorized Endpoints

| API      | Method | Access |
| -------- | ------ | ------ |
| /flights | POST   | ADMIN  |

Only admins are allowed to add flights.

---

### Unauthorized Access Behavior

| Case                                | Response         |
| ----------------------------------- | ---------------- |
| Missing token on protected endpoint | 401 Unauthorized |
| Invalid or expired token            | 401 Unauthorized |
| Wrong role for requested operation  | 403 Forbidden    |

---

## Deployment

All services are Docker-enabled and run as independent containers.
During deployment:

* Each service registers automatically with Eureka
* API-Gateway dynamically discovers service locations
* Config-Service supplies externalized configuration
* Email-Service processes notifications asynchronously via RabbitMQ

**Docker Compose** can be used to bring up the entire stack with a single command, enabling full environment setup with minimal effort.

---

## Features Completed

* JWT authentication with secure password handling
* Role-based authorization via API-Gateway
* Public flight browsing functionality
* User-based flight booking capability
* Admin-only flight management
* Distributed service discovery with Eureka
* Centralized configuration using Config-Service
* Email notifications using RabbitMQ
* Docker containerization for all microservices
* Code coverage above 90%

---


