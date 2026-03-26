# E-commerce Backend System

This is a complete, production-ready Spring Boot backend for an e-commerce platform.

## Features
- **JWT-based Security**: Secure authentication and role-based authorization.
- **Product CRUD**: Full management for products (restricted to ADMIN).
- **Cart Management**: User-specific shopping carts with owner validation.
- **Order Processing**: Atomic checkout flow with stock management.
- **Payment Integration**: Demo Stripe integration for a complete checkout experience.

## Tech Stack
- Spring Boot 3
- Spring Security + JWT
- Spring Data JPA
- H2 In-Memory Database
- Lombok

## Getting Started
Build and run the project using Maven:
```bash
mvn clean install
mvn spring-boot:run
```

The H2 console is available at `/h2-console` (JDBC URL: `jdbc:h2:mem:ecomdb`).
