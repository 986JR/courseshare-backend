# CourseShare

A full-stack learning resource sharing platform where users can upload, discover, and manage academic course materials securely.

CourseShare focuses on secure authentication, scalable backend architecture, and real-world production practices using modern Spring Boot technologies.

# Features

### Authentication & Security

* JWT-based authentication (Access + Refresh token)
* Refresh token rotation with revocation
* Token blacklisting for logout security
* HTTP-only secure refresh cookies
* Role-based authorization
* Global API rate limiting using Bucket4j

### User & Content Management

* User registration and login
* Profile management
* File upload with metadata storage
* View personal uploaded files (`/me` endpoints)
* Course and enrollment handling

### Backend Architecture

* Spring Boot REST API
* PostgreSQL database
* Layered architecture (Controller → Service → Repository)
* DTO-based request/response mapping
* Exception handling & validation
* Secure password hashing

### Production-Ready Practices

* Refresh token persistence in database
* Logout token revocation
* Per-IP rate limiting
* Environment-based configuration
* Designed for containerization & cloud deployment



# Tech Stack

Backend

* Java 21
* Spring Boot 3
* Spring Security
* JWT
* Bucket4j
* PostgreSQL
* Maven

Planned / Optional

* Docker deployment
* Cloud file storage
* Email verification & password reset
* WhatsApp integration
* AI-assisted recommendations



# Project Structure

```
courseshare
 ┣ controller
 ┣ service
 ┣ repository
 ┣ entity
 ┣ dto
 ┣ security
 ┣ jwt
 ┗ config
```

This follows a clean layered architecture for maintainability and scalability.



# Authentication Flow

### Login

1. User sends username & password.
2. Server authenticates credentials.
3. Returns:

    * Access Token (JWT) in response body
    * Refresh Token in secure HTTP-only cookie.

### Access Protected Endpoints

* Frontend sends Authorization: Bearer accessToken.

### Access Token Expired

1. Frontend calls POST `/api/auth/refresh`.
2. Server validates refresh token from cookie.
3. Issues:

    * New access token
    * Rotated refresh token.

### Logout

* Access token is blacklisted.
* Refresh token is revoked in DB.



# Rate Limiting

Global API protection using Bucket4j:

* Per-IP request limiting
* Prevents brute force and abuse
* Returns HTTP 429 when exceeded



# Getting Started

## Prerequisites

* Java 21
* Maven
* PostgreSQL



## Clone the repository

```bash
git clone https://github.com/your-username/courseshare.git
cd courseshare
```



## Configure environment

Create application.properties or environment variables:

```
spring.datasource.url=jdbc:postgresql://localhost:5432/courseshare
spring.datasource.username=postgres
spring.datasource.password=your_password

app.jwt.secret=change_me_secure_secret
app.jwt.expiration-ms=900000
app.jwt.refresh.expiration=172800000
app.auth.max.sessions=5
```



## Run the application

```bash
mvn spring-boot:run
```

API will start at:

```
http://localhost:8080
```



# API Overview

### Auth

```
POST /api/auth/register
POST /api/auth/login
POST /api/auth/refresh
POST /api/auth/logout
```

### Users

```
GET /api/users/{publicId}
PATCH /api/users/{publicId}
DELETE /api/users/{publicId}
```

### Files / Courses / Enrollments

```
GET /api/files/me
GET /api/courses
POST /api/enrollments
GET /api/enrollments/me
```



# Security Notes

* Refresh tokens stored in HTTP-only cookies
* Access tokens never stored in cookies
* Logout revokes both access & refresh tokens
* Rate limiting protects authentication endpoints
* Designed for horizontal scaling with Redis in future



# Roadmap

* Cloud file storage (AWS S3 / Supabase)
* Email verification & password reset
* Docker & CI/CD deployment
* Multi-device session management UI
* AI-powered course recommendations
* Mobile app integration



# Author

Joshua Rodgers
Software Engineering Student – University of Dodoma (UDOM)
Focused on backend systems, machine learning, and scalable web platforms.


