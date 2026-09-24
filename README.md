# Booking System

A RESTful Resource Booking System built with Spring Boot, Java 17, Spring Security, JWT authentication, PostgreSQL, JPA/Hibernate, and Docker.

The system allows users to view available resources and create/manage their own reservations, while administrators can manage resources and reservations with role-based access control.

## Features

- JWT-based authentication
- BCrypt password hashing
- USER and ADMIN roles
- Stateless Spring Security configuration
- Role-based authorization
- Resource CRUD operations for ADMIN
- Resource viewing for USER and ADMIN
- Reservation creation for USER
- Users can view only their own reservations
- ADMIN reservation management
- Reservation status management
- Reservation ownership validation
- Reservation conflict detection
- Reservation status filtering
- Price filtering
- Pagination
- Sorting for resources
- Request validation
- Centralized exception handling
- PostgreSQL database
- JPA/Hibernate ORM
- Swagger/OpenAPI documentation
- Seeded ADMIN and USER accounts
- Docker and Docker Compose support
- Environment-variable based configuration
- Automated Spring Boot context test

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
| JUnit | Testing |

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

Security flow:

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

The application is implemented as a modular monolith. Controllers handle HTTP requests, services contain business logic, repositories handle persistence, and security components handle JWT authentication and authorization.

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

## Domain Model

### Account

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

### Asset

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

### Booking

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

## Authentication

Authentication is provided through JWT.

### Login

```http
POST /auth/login
```

Request:

```json
{
  "username": "practice-user",
  "password": "User@123"
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

The application uses a stateless Spring Security configuration, so server-side HTTP sessions are not used for authentication.

## Authorization

### USER permissions

| Operation | Access |
|---|---|
| View resources | Yes |
| Create resource | No |
| Update resource | No |
| Delete resource | No |
| Create own booking | Yes |
| View own bookings | Yes |
| View another user's booking | No |
| Manage all bookings | No |

### ADMIN permissions

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

Reservation ownership is enforced in the service layer. The authenticated username is obtained from the JWT rather than being accepted as the identity of a normal USER booking request.

## API Endpoints

### Authentication

| Method | Endpoint | Access |
|---|---|---|
| POST | `/auth/login` | Public |

### Resources

| Method | Endpoint | Access |
|---|---|---|
| GET | `/api/assets` | USER, ADMIN |
| GET | `/api/assets/{id}` | USER, ADMIN |
| POST | `/api/assets` | ADMIN |
| PUT | `/api/assets/{id}` | ADMIN |
| DELETE | `/api/assets/{id}` | ADMIN |

### User Bookings

| Method | Endpoint | Access |
|---|---|---|
| POST | `/api/bookings` | USER |
| GET | `/api/bookings/my` | USER |
| GET | `/api/bookings/{id}` | USER |

### Admin Bookings

| Method | Endpoint | Access |
|---|---|---|
| GET | `/api/admin/bookings` | ADMIN |
| GET | `/api/admin/bookings/{id}` | ADMIN |
| POST | `/api/admin/bookings` | ADMIN |
| PUT | `/api/admin/bookings/{id}` | ADMIN |
| PATCH | `/api/admin/bookings/{id}/state` | ADMIN |
| DELETE | `/api/admin/bookings/{id}` | ADMIN |

## Resource Filtering, Pagination and Sorting

Resources support filtering by:

- Minimum price
- Maximum price
- Category
- Availability

Example:

```http
GET /api/assets?minPrice=500&maxPrice=2000&category=ROOM&available=true
```

Pagination:

```http
GET /api/assets?page=0&size=10
```

Sorting:

```http
GET /api/assets?sortBy=price&direction=asc
```

Supported resource sorting fields include:

- `id`
- `name`
- `price`
- `category`

## Booking Filtering and Pagination

Bookings support filtering by:

- Status
- Minimum price
- Maximum price

Example:

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

Administrators can use the same filtering options on:

```http
GET /api/admin/bookings
```

## Reservation Conflict Detection

The system prevents overlapping active reservations for the same resource.

A new booking is rejected when the selected time range overlaps an existing non-cancelled booking.

Cancelled bookings do not block a resource from being booked again.

## Validation and Error Handling

Request DTOs use Jakarta Bean Validation.

Examples include:

- Required fields
- Positive/non-negative prices
- Future booking start time
- Valid booking time ranges

The application uses centralized exception handling through `ApiExceptionHandler`.

Typical responses include:

```text
400 Bad Request
401 Unauthorized
403 Forbidden
404 Not Found
```

Example unauthorized response:

```json
{
  "status": 401,
  "error": "Unauthorized",
  "message": "Authentication is required to access this resource"
}
```

Example forbidden response:

```json
{
  "status": 403,
  "error": "Forbidden",
  "message": "You do not have permission to access this resource"
}
```

## Database Configuration

The application uses PostgreSQL.

Default local configuration:

```text
Database: booking_system
Username: postgres
Password: postgres
Host: localhost
Port: 5432
```

The application supports environment variables so database configuration does not have to be hardcoded.

Available variables:

```text
DB_URL
DB_USERNAME
DB_PASSWORD
JWT_SECRET
JWT_EXPIRATION_MS
SERVER_PORT
```

## Environment Configuration

Create a `.env` file for local/container configuration if required.

A template is provided as:

```text
.env.example
```

Example:

```env
POSTGRES_DB=booking_system
POSTGRES_USER=postgres
POSTGRES_PASSWORD=change-this-password

DB_URL=jdbc:postgresql://postgres:5432/booking_system
DB_USERNAME=postgres
DB_PASSWORD=change-this-password

JWT_SECRET=change-this-to-a-long-random-secret-key
JWT_EXPIRATION_MS=86400000

SERVER_PORT=8080
```

Do not commit real secrets or passwords to Git.

## Running Locally

### Prerequisites

Make sure the following are installed:

- Java 17+
- Docker Desktop
- Git

Maven Wrapper is included in the project, so Maven does not need to be installed globally.

### Run tests

Windows:

```powershell
.\mvnw.cmd clean test
```

### Run the application

Make sure PostgreSQL is running and the database is available.

Then:

```powershell
.\mvnw.cmd spring-boot:run
```

The application runs on:

```text
http://localhost:8080
```

## Running with Docker Compose

The recommended way to run the complete application is Docker Compose.

Build and start:

```powershell
docker compose up --build -d
```

Check running containers:

```powershell
docker ps
```

Stop the application:

```powershell
docker compose down
```

The application is available at:

```text
http://localhost:8080
```

PostgreSQL runs as a separate Docker Compose service.

The application connects to PostgreSQL using the Compose service name:

```text
postgres
```

## Seeded Accounts

The application creates sample accounts when they do not already exist.

### ADMIN

```text
Username: practice-admin
Password: Admin@123
Role: ADMIN
```

### USER

```text
Username: practice-user
Password: User@123
Role: USER
```

### USER 2

```text
Username: practice-user-2
Password: User2@123
Role: USER
```

The second USER account is useful for verifying reservation ownership isolation.

## Sample Resources

The application seeds sample resources when the resource table is empty.

Examples:

```text
Meeting Room A
Company Car
Developer Laptop
```

## Swagger / OpenAPI

Swagger UI is available at:

```text
http://localhost:8080/swagger-ui/index.html
```

OpenAPI documentation is available at:

```text
http://localhost:8080/v3/api-docs
```

After obtaining a JWT, click **Authorize** in Swagger UI and provide:

```text
Bearer <JWT_TOKEN>
```

This allows protected endpoints to be tested directly through Swagger.

## Testing

The project includes a Spring Boot context test.

Run:

```powershell
.\mvnw.cmd clean test
```

The test application uses a separate H2 in-memory database configuration, so the test does not require the local PostgreSQL database.

The application has also been manually tested for:

- JWT login
- USER and ADMIN authorization
- Unauthorized requests
- Resource CRUD
- USER resource access restrictions
- USER booking creation
- Reservation ownership isolation
- ADMIN reservation management
- Reservation filtering
- Pagination
- Resource sorting
- Validation
- Swagger API access
- Docker-based application startup

## Security Design

The application uses:

- BCrypt password hashing
- JWT authentication
- Stateless sessions
- Spring Security role-based authorization
- JWT request filtering
- Custom unauthorized and forbidden responses
- Reservation ownership checks
- DTO-based request validation

The security flow is:

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

## Build

Build the application using Maven Wrapper:

```powershell
.\mvnw.cmd clean package
```

Skip tests if required:

```powershell
.\mvnw.cmd clean package -DskipTests
```

## Docker Image

Build the Docker image:

```powershell
docker build -t booking-system .
```

Run the complete environment with:

```powershell
docker compose up --build -d
```

## License

This project was created as a backend development assignment demonstrating REST API development, authentication, authorization, database persistence, validation, testing, and containerization.
