# Booking System

A RESTful Resource Booking System built with **Spring Boot 4.1.1, Java 17, Spring Security, JWT authentication, PostgreSQL, JPA/Hibernate, and Docker**.

The system allows users to view bookable resources and create/manage their own reservations, while administrators can manage resources and reservations with role-based access control.

---

## Features

- JWT-based authentication
- BCrypt password hashing
- `USER` and `ADMIN` roles
- Stateless Spring Security configuration
- Role-based authorization
- Resource CRUD operations for `ADMIN`
- Resource viewing for `USER` and `ADMIN`
- User booking creation
- Users can view only their own bookings
- Reservation ownership enforcement
- Administrator booking management
- Booking state management
- Booking conflict detection
- Booking status filtering
- Booking price filtering
- Resource price/category/availability filtering
- Pagination
- Sorting
- Request validation
- Centralized exception handling
- PostgreSQL database
- JPA/Hibernate ORM
- Swagger/OpenAPI documentation
- Environment-variable based configuration
- Seeded development/demo accounts
- Docker and Docker Compose support
- Integration and security tests

---

## Technology Stack

| Technology | Purpose |
|---|---|
| Java 17 | Programming language |
| Spring Boot 4.1.1 | Application framework |
| Spring Security | Authentication and authorization |
| JWT | Stateless authentication |
| Spring Data JPA | Database access |
| Hibernate | ORM |
| PostgreSQL | Relational database |
| Maven | Build and dependency management |
| Docker | Containerization |
| Docker Compose | Application and database orchestration |
| Swagger / OpenAPI | API documentation |
| JUnit 5 / Spring Boot Test | Automated testing |
| H2 | In-memory database for tests |

---

## Architecture

The application follows a layered architecture:

```text
Client
   |
   v
Controller Layer
   |
   v
Service Layer
   |
   v
Repository Layer
   |
   v
PostgreSQL
```

### Security flow

```text
Client
   |
   | Authorization: Bearer <JWT>
   v
JwtRequestFilter
   |
   v
JWT validation
   |
   v
Spring Security
   |
   +---- USER
   |
   +---- ADMIN
   |
   v
Controller
```

The application is implemented as a **modular monolith**.

- Controllers handle HTTP requests.
- Services contain business logic.
- Repositories handle persistence.
- DTOs define API request/response contracts.
- Security components handle JWT authentication.
- Spring Security enforces role-based authorization.

---

## Project Structure

```text
src/
├── main/
│   ├── java/
│   │   └── com/example/bookingsystem/
│   │       ├── config/
│   │       │   ├── DataInitializer.java
│   │       │   ├── OpenApiConfig.java
│   │       │   └── SecurityConfig.java
│   │       │
│   │       ├── controller/
│   │       │   ├── AdminBookingController.java
│   │       │   ├── AssetController.java
│   │       │   ├── AuthController.java
│   │       │   └── BookingController.java
│   │       │
│   │       ├── dto/
│   │       │   ├── AdminBookingRequest.java
│   │       │   ├── AssetRequest.java
│   │       │   ├── AssetResponse.java
│   │       │   ├── BookingRequest.java
│   │       │   ├── BookingResponse.java
│   │       │   ├── BookingStateRequest.java
│   │       │   ├── LoginRequest.java
│   │       │   └── LoginResponse.java
│   │       │
│   │       ├── entity/
│   │       │   ├── AccessLevel.java
│   │       │   ├── Account.java
│   │       │   ├── Asset.java
│   │       │   ├── Booking.java
│   │       │   └── BookingState.java
│   │       │
│   │       ├── exception/
│   │       │   ├── ApiExceptionHandler.java
│   │       │   ├── BookingConflictException.java
│   │       │   └── NotFoundException.java
│   │       │
│   │       ├── repository/
│   │       │   ├── AccountRepository.java
│   │       │   ├── AssetRepository.java
│   │       │   └── BookingRepository.java
│   │       │
│   │       ├── security/
│   │       │   ├── JwtRequestFilter.java
│   │       │   └── JwtTokenService.java
│   │       │
│   │       ├── service/
│   │       │   ├── AccountService.java
│   │       │   ├── AssetService.java
│   │       │   └── BookingService.java
│   │       │
│   │       └── BookingsystemApplication.java
│   │
│   └── resources/
│       └── application.yaml
│
└── test/
    ├── java/
    │   └── com/example/bookingsystem/
    │       └── BookingsystemApplicationTests.java
    │
    └── resources/
        └── application.yaml
```

---

# Domain Model

## Account

Represents a system user.

Important fields:

- `id`
- `username`
- `email`
- `passwordHash`
- `accessLevel`
- `enabled`

Roles:

```text
USER
ADMIN
```

## Asset

Represents a bookable resource.

Examples:

- Meeting room
- Vehicle
- Equipment

Important fields:

- `id`
- `name`
- `category`
- `description`
- `price`
- `available`

## Booking

Represents a reservation made for an asset.

Important fields:

- `id`
- `account`
- `asset`
- `startAt`
- `endAt`
- `price`
- `state`
- `createdAt`

Booking states:

```text
PENDING
CONFIRMED
CANCELLED
```

Relationships:

```text
Account 1 ---- * Booking * ---- 1 Asset
```

A booking belongs to one account and one asset.

---

# Authentication

Authentication is provided through JWT.

## Login

```http
POST /auth/login
Content-Type: application/json
```

Request:

```json
{
  "username": "practice-user",
  "password": "<SEED_USER_PASSWORD>"
}
```

Example response:

```json
{
  "token": "<JWT_TOKEN>",
  "username": "practice-user",
  "role": "USER"
}
```

For protected endpoints, send:

```http
Authorization: Bearer <JWT_TOKEN>
```

The JWT contains the authenticated username and role.

The application uses a **stateless Spring Security configuration**, so server-side HTTP sessions are not used for authentication.

---

# Authorization

## USER permissions

| Operation | Access |
|---|---|
| View resources | Yes |
| Create resource | No |
| Update resource | No |
| Delete resource | No |
| Create booking | Yes |
| View own bookings | Yes |
| View another user's booking | No |
| Manage all bookings | No |

## ADMIN permissions

| Operation | Access |
|---|---|
| View resources | Yes |
| Create resource | Yes |
| Update resource | Yes |
| Delete resource | Yes |
| View all bookings | Yes |
| Create booking for a user | Yes |
| Update booking | Yes |
| Change booking state | Yes |
| Delete booking | Yes |

## Ownership protection

For normal user booking creation, the account identity is **not accepted from the request body**.

Instead:

```text
JWT
  |
  v
Authenticated username
  |
  v
BookingService
  |
  v
Account associated with JWT
  |
  v
Booking
```

This prevents a user from submitting another user's identity when creating a booking.

When a user requests an individual booking, the service verifies that the booking belongs to the authenticated user.

---

# API Endpoints

## Authentication

| Method | Endpoint | Access |
|---|---|---|
| POST | `/auth/login` | Public |

---

## Resources

| Method | Endpoint | Access |
|---|---|---|
| GET | `/api/assets` | USER, ADMIN |
| GET | `/api/assets/{id}` | USER, ADMIN |
| POST | `/api/assets` | ADMIN |
| PUT | `/api/assets/{id}` | ADMIN |
| DELETE | `/api/assets/{id}` | ADMIN |

---

## User Bookings

| Method | Endpoint | Access |
|---|---|---|
| POST | `/api/bookings` | USER |
| GET | `/api/bookings/my` | USER |
| GET | `/api/bookings/{id}` | USER |

### Create booking

```http
POST /api/bookings
Authorization: Bearer <JWT_TOKEN>
Content-Type: application/json
```

Request:

```json
{
  "assetId": 1,
  "startAt": "2027-01-10T10:00:00",
  "endAt": "2027-01-10T12:00:00"
}
```

The user identity is obtained from the JWT.

---

## Admin Bookings

| Method | Endpoint | Access |
|---|---|---|
| GET | `/api/admin/bookings` | ADMIN |
| GET | `/api/admin/bookings/{id}` | ADMIN |
| POST | `/api/admin/bookings` | ADMIN |
| PUT | `/api/admin/bookings/{id}` | ADMIN |
| PATCH | `/api/admin/bookings/{id}/state` | ADMIN |
| DELETE | `/api/admin/bookings/{id}` | ADMIN |

### Create booking as ADMIN

```http
POST /api/admin/bookings
Authorization: Bearer <ADMIN_JWT>
Content-Type: application/json
```

Request:

```json
{
  "accountId": 1,
  "assetId": 1,
  "startAt": "2027-01-10T10:00:00",
  "endAt": "2027-01-10T12:00:00",
  "state": "CONFIRMED"
}
```

### Change booking state

```http
PATCH /api/admin/bookings/{id}/state
Authorization: Bearer <ADMIN_JWT>
Content-Type: application/json
```

Request:

```json
{
  "state": "CONFIRMED"
}
```

### Booking state transitions

The application enforces the following transitions:

```text
PENDING
   |
   +----> CONFIRMED
   |
   +----> CANCELLED

CONFIRMED
   |
   +----> CANCELLED

CANCELLED
   |
   +----> no further transition
```

The same state is also allowed.

---

# Resource Filtering, Pagination and Sorting

Resources support filtering by:

- Minimum price
- Maximum price
- Category
- Availability

Example:

```http
GET /api/assets?minPrice=500&maxPrice=2000&category=ROOM&available=true
```

If both prices are supplied, `minPrice` cannot be greater than `maxPrice`.

## Pagination

```http
GET /api/assets?page=0&size=10
```

The supported page size range is `1` to `100`.

## Sorting

```http
GET /api/assets?sortBy=price&direction=asc
```

Supported resource sorting fields:

- `id`
- `name`
- `price`
- `category`

---

# Booking Filtering, Pagination and Sorting

Bookings support filtering by:

- Status
- Minimum price
- Maximum price

## User booking filtering

```http
GET /api/bookings/my?state=PENDING
```

Price filtering:

```http
GET /api/bookings/my?minPrice=500&maxPrice=2000
```

Combined:

```http
GET /api/bookings/my?state=PENDING&minPrice=500&maxPrice=2000
```

Pagination:

```http
GET /api/bookings/my?page=0&size=10
```

Sorting:

```http
GET /api/bookings/my?sortBy=price&direction=asc
```

Supported user booking sorting fields:

- `id`
- `startAt`
- `endAt`
- `price`
- `createdAt`
- `state`

## Admin booking filtering

```http
GET /api/admin/bookings?state=PENDING&minPrice=500&maxPrice=2000
```

Pagination:

```http
GET /api/admin/bookings?page=0&size=10
```

Sorting:

```http
GET /api/admin/bookings?sortBy=price&direction=asc
```

Supported admin booking sorting fields:

- `id`
- `startAt`
- `endAt`
- `price`
- `createdAt`
- `state`

---

# Reservation Conflict Detection

The system prevents overlapping active reservations for the same resource.

A new booking is rejected when its time range overlaps an existing non-cancelled booking for the same asset.

The overlap condition is:

```text
existing.startAt < requested.endAt

AND

existing.endAt > requested.startAt
```

`CANCELLED` bookings do not block a resource from being booked again.

Booking creation and administrator booking updates use transactional conflict checking.

---

# Validation and Error Handling

Request DTOs use Jakarta Bean Validation.

Examples include:

- Required fields
- Non-negative resource prices
- Future booking start time
- Valid booking time ranges
- Valid pagination values
- Valid price ranges

The application uses centralized exception handling through:

```text
ApiExceptionHandler
```

Typical responses include:

```text
400 Bad Request
401 Unauthorized
403 Forbidden
404 Not Found
409 Conflict
```

### Example unauthorized response

```json
{
  "status": 401,
  "message": "Authentication required"
}
```

### Example forbidden response

```json
{
  "status": 403,
  "message": "Access denied"
}
```

### Booking conflict

An overlapping booking returns:

```text
409 Conflict
```

---

# Database Configuration

The application uses PostgreSQL.

For local development, the default database connection values in `application.yaml` are:

```text
Database: booking_system
Username: postgres
Host: localhost
Port: 5432
```

The database password should be supplied through the environment.

The application supports environment variables so database and JWT configuration can be changed without modifying source code.

Available application variables include:

```text
DB_URL
DB_USERNAME
DB_PASSWORD

JWT_SECRET
JWT_EXPIRATION_MS

SEED_ADMIN_USERNAME
SEED_ADMIN_EMAIL
SEED_ADMIN_PASSWORD

SEED_USER_USERNAME
SEED_USER_EMAIL
SEED_USER_PASSWORD

SEED_USER2_USERNAME
SEED_USER2_EMAIL
SEED_USER2_PASSWORD

SERVER_PORT
```

For production use, always provide secure database credentials and a cryptographically random JWT secret.

---

# Environment Configuration

Create a `.env` file for local/container configuration when needed.

The repository contains `.env.example` as a template.

Example:

```env
POSTGRES_DB=booking_system
POSTGRES_USER=postgres
POSTGRES_PASSWORD=change-this-password

DB_URL=jdbc:postgresql://postgres:5432/booking_system
DB_USERNAME=postgres
DB_PASSWORD=change-this-password

JWT_SECRET=
JWT_EXPIRATION_MS=86400000

SEED_ADMIN_USERNAME=practice-admin
SEED_ADMIN_EMAIL=practice-admin@example.com
SEED_ADMIN_PASSWORD=

SEED_USER_USERNAME=practice-user
SEED_USER_EMAIL=practice-user@example.com
SEED_USER_PASSWORD=

SEED_USER2_USERNAME=practice-user-2
SEED_USER2_EMAIL=practice-user-2@example.com
SEED_USER2_PASSWORD=

SERVER_PORT=8080
```

Generate a strong random value for `JWT_SECRET`.

Do not commit `.env` or real secrets, passwords, or production credentials to Git.

---

# Running Locally

## Prerequisites

Make sure the following are installed:

- Java 17+
- PostgreSQL
- Git

Docker Desktop is optional when running PostgreSQL and the application directly.

The project includes the **Maven Wrapper**, so Maven does not need to be installed globally.

---

## Run Tests

### Windows Command Prompt

```cmd
mvnw.cmd clean test
```

### Windows PowerShell

```powershell
.\mvnw.cmd clean test
```

### Git Bash

```bash
./mvnw.cmd clean test
```

The project contains automated Spring Boot integration and security tests.

---

## Build the Application

```cmd
mvnw.cmd clean package
```

To skip tests:

```cmd
mvnw.cmd clean package -DskipTests
```

The generated JAR is placed under:

```text
target/
```

---

## Run the Application

Make sure PostgreSQL is running and the required environment variables are configured.

Then:

```cmd
mvnw.cmd spring-boot:run
```

The application runs on:

```text
http://localhost:8080
```

---

# Running with Docker Compose

Docker Compose is the recommended way to run the complete application because it starts both the Spring Boot application and PostgreSQL.

Make sure your `.env` file contains non-empty values for:

```text
POSTGRES_PASSWORD
DB_PASSWORD
JWT_SECRET
SEED_ADMIN_PASSWORD
SEED_USER_PASSWORD
SEED_USER2_PASSWORD
```

Build and start:

```cmd
docker compose up --build -d
```

Check services:

```cmd
docker compose ps
```

Expected services are the application and PostgreSQL containers defined in `docker-compose.yml`.

Check application logs:

```cmd
docker compose logs -f app
```

Stop the application:

```cmd
docker compose down
```

The application is available at:

```text
http://localhost:8080
```

Inside Docker Compose, the application connects to PostgreSQL using the Compose service hostname:

```text
postgres
```

---

# Seeded Accounts

The application creates development/demo accounts when they do not already exist.

## ADMIN

```text
Username: configured by SEED_ADMIN_USERNAME
Password: configured by SEED_ADMIN_PASSWORD
Role: ADMIN
```

## USER

```text
Username: configured by SEED_USER_USERNAME
Password: configured by SEED_USER_PASSWORD
Role: USER
```

## USER 2

```text
Username: configured by SEED_USER2_USERNAME
Password: configured by SEED_USER2_PASSWORD
Role: USER
```

Passwords are intentionally **not hardcoded in the README or application configuration**. They must be supplied through environment variables.

These accounts are intended for development/demo use.

---

# Sample Resources

When the asset table is empty, the application creates sample resources.

Examples include:

```text
Meeting Room A
Company Car
Developer Laptop
```

---

# Swagger / OpenAPI

Swagger UI:

```text
http://localhost:8080/swagger-ui/index.html
```

OpenAPI specification:

```text
http://localhost:8080/v3/api-docs
```

After obtaining a JWT, click **Authorize** in Swagger UI and provide:

```text
Bearer <JWT_TOKEN>
```

This allows protected endpoints to be tested directly through Swagger.

---

# Testing

The project contains an automated integration/security test suite using:

- Spring Boot Test
- MockMvc
- JUnit 5
- H2 in-memory database

The current test suite contains **27 automated tests**.

The tests cover:

- Application context loading
- USER login
- ADMIN login
- Invalid login credentials
- Unauthenticated request rejection
- Tampered JWT rejection
- USER resource access
- USER resource creation restriction
- ADMIN resource creation
- USER booking creation
- USER booking retrieval
- Booking ownership isolation
- USER access restriction for admin endpoints
- ADMIN booking listing
- ADMIN booking creation
- ADMIN booking update
- ADMIN booking state changes
- Invalid booking state transitions
- Cancelled booking reuse
- ADMIN booking deletion
- Invalid booking time
- Invalid asset ID
- Booking conflict detection
- Invalid price range
- Resource filtering and pagination
- Admin booking filtering and sorting
- Invalid pagination

Run the complete test suite with:

```cmd
mvnw.cmd clean test
```

Expected result in the current validated state:

```text
Tests run: 27, Failures: 0, Errors: 0, Skipped: 0
BUILD SUCCESS
```

---

# Security Design

The application uses:

- BCrypt password hashing
- JWT authentication
- Stateless sessions
- Spring Security role-based authorization
- JWT request filtering
- JWT expiration
- JWT signature validation
- JWT secret length validation
- Custom unauthorized and forbidden responses
- Reservation ownership checks
- DTO-based request validation
- Environment-based secrets and credentials

Security flow:

```text
Login
  |
  v
AuthenticationManager
  |
  v
AccountService
  |
  v
BCrypt password verification
  |
  v
JWT generated
  |
  v
Client sends JWT
  |
  v
JwtRequestFilter
  |
  v
JWT validation
  |
  v
SecurityContext
  |
  v
Role-based authorization
```

Invalid or missing authentication results in:

```text
401 Unauthorized
```

Authenticated users without the required role receive:

```text
403 Forbidden
```

JWT signature tampering is rejected.

---

# API Security Example

Without authentication:

```http
GET /api/assets
```

returns:

```text
401 Unauthorized
```

With a valid USER JWT:

```http
GET /api/assets
Authorization: Bearer <JWT_TOKEN>
```

the request is authorized.

A USER attempting an ADMIN-only operation receives:

```text
403 Forbidden
```

---

# Build and Docker Image

Build the Docker image:

```cmd
docker build -t booking-system .
```

Run the complete environment:

```cmd
docker compose up --build -d
```

Check running containers:

```cmd
docker compose ps
```

Stop containers:

```cmd
docker compose down
```

---

# Verification

The application has been verified through:

- Automated integration/security tests
- Maven build
- Docker configuration
- Docker Compose configuration
- PostgreSQL integration
- Swagger/OpenAPI configuration
- JWT authentication
- Protected endpoint authorization
- Booking ownership enforcement
- Booking conflict detection
- Booking state transition validation

Current automated test status:

```text
27 tests
27 passed
0 failures
0 errors
```

---

# Design Notes

### Modular monolith

The project intentionally uses a modular monolith architecture rather than multiple microservices because the assignment focuses on a single RESTful booking system.

### DTO-based API

Request and response DTOs keep API contracts separate from persistence entities.

### JWT-based identity

For normal USER booking creation, the authenticated user is derived from the JWT rather than accepting an arbitrary account ID from the client.

### Transactional booking validation

Booking creation and administrator booking updates perform transactional conflict checks to reduce race conditions when reservations overlap.

### Booking state management

Booking states are explicitly controlled:

```text
PENDING -> CONFIRMED
PENDING -> CANCELLED
CONFIRMED -> CANCELLED
CANCELLED -> no further transition
```

### Environment-based secrets

Database passwords, JWT secrets, and seeded account passwords are supplied through environment variables rather than being stored as application defaults.

---

# License

This project was created as a backend development assignment demonstrating:

- REST API development
- JWT authentication
- Role-based authorization
- Database persistence
- JPA/Hibernate
- Validation
- Exception handling
- Filtering
- Pagination
- Sorting
- Booking conflict detection
- Automated testing
- Docker containerization
- PostgreSQL integration
