# Lending App

This is a Spring Boot application that provides a lending platform, allowing users to apply for loans, process installment payments, and manage their user accounts.

## What is a Lending App?

A lending application is a software solution designed to facilitate the process of borrowing and lending money. It typically connects borrowers with lenders, automates loan application processing, credit assessment, disbursement, and repayment management. These applications streamline traditional banking operations, offering convenience, speed, and often more personalized financial services. Key features often include user authentication, loan application workflows, installment tracking, and secure transaction processing.

## Features

*   **User Authentication and Authorization**: Secure user registration and login with JWT.
*   **User Management**: Create, retrieve, update, and soft-delete user accounts. Manage user scores.
*   **Loan Application**: Users can apply for loans. The system checks the borrower's credit score, and if insufficient, allows for a guarantor to be involved.
*   **Installment Payments**: Process loan installment payments, update loan transaction status, and trigger asynchronous creation of subsequent installments.
*   **Asynchronous Processing**: Utilizes Spring's `@Async` for tasks like installment creation to ensure non-blocking operations.
*   **Data Persistence**: Uses Spring Data JPA with PostgreSQL as the primary database.
*   **Caching**: Integrates Spring Cache for improving performance of user data retrieval.
*   **Messaging**: Utilizes Spring AMQP (RabbitMQ) for asynchronous messaging.
*   **Redis**: Integrated with Redis for caching or other potential uses.

## Technologies Used

*   **Java 21**
*   **Spring Boot 3.x**:
    *   Spring Data JPA
    *   Spring Security
    *   Spring Web
    *   Spring AMQP
    *   Spring Data Redis
    *   Spring Validation
*   **OpenAPI (Swagger UI)**: For interactive API documentation.
*   **PostgreSQL**: Relational database for persistent storage.
*   **H2 Database**: Used for testing purposes.
*   **JWT (JSON Web Tokens)**: For secure authentication and authorization.
*   **Lombok**: To reduce boilerplate code.
*   **MapStruct**: For automatic mapping between entity and DTO objects.
*   **ULID**: For Universally Unique Lexicographically Sortable Identifiers.
*   **Maven**: Dependency management and build automation.

## Project Structure

The project follows a layered architecture:

*   `aspect`: Aspect-Oriented Programming concerns, e.g., logging.
*   `application`: Contains business logic, services, and processors for core functionalities.
    *   `processor`: Handles complex business flows like `LoanApplicationProcessor` and `InstallmentPaymentProcessor`.
    *   `service`: Defines service interfaces and their implementations.
*   `config`: Spring configurations for security, Redis, and general services.
*   `controller`: REST API endpoints for user, loan, and authentication operations.
*   `exception`: Custom exception classes and a global exception handler.
*   `mapper`: MapStruct mappers for DTO-entity conversions.
*   `model`:
    *   `entity`: JPA entities representing database tables.
    *   `enums`: Enumerations for various types.
    *   `record`: Java records for DTOs (Data Transfer Objects) and commands.
    *   `util`: Utility classes like `UserPrincipal`.
*   `repository`: Spring Data JPA repositories for data access.
*   `security`: JWT-related classes for security filters and service.
*   `util`: General utility classes.

## Getting Started

### Prerequisites

*   Java Development Kit (JDK) 21
*   Maven
*   Docker and Docker Compose (for setting up PostgreSQL and Redis)

### Setup

1.  **Clone the repository:**
    ```bash
    git clone <repository_url>
    cd lending-app
    ```

2.  **Set up environment variables:**
    *   Create an `application.properties` file in `src/main/resources/` (if it doesn't exist) and configure your database and other properties.
        ```properties
        spring.datasource.url=jdbc:postgresql://localhost:5432/lendingdb
        spring.datasource.username=lendinguser
        spring.datasource.password=lendingpassword
        spring.jpa.hibernate.ddl-auto=update
        spring.jpa.show-sql=true
        spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.PostgreSQLDialect
        spring.redis.host=localhost
        spring.redis.port=6379
        jwt.secret=<your_jwt_secret_key>
        signup.bonus=100
        ```
        Replace `<your_jwt_secret_key>` with a strong, unique secret key.

3.  **Start Docker containers for PostgreSQL and Redis:**
    ```bash
    docker-compose up -d
    ```

4.  **Build the application:**
    ```bash
    mvn clean install
    ```

5.  **Run the application:**
    ```bash
    mvn spring-boot:run
    ```

    The application will start on `http://localhost:8080` (or your configured port).

## API Endpoints

Access the OpenAPI (Swagger UI) documentation at `http://localhost:8080/swagger-ui.html` after the application starts.

### Authentication
*   `POST /api/auth/signup`: Register a new user.
*   `POST /api/auth/signin`: Authenticate a user and receive a JWT.

### User Management
*   `GET /api/users`: Get current user's details.
*   `GET /api/users/all`: Get all users.
*   `PUT /api/users`: Update current user's details.
*   `DELETE /api/users`: Soft-delete current user.

### Loan Management
*   `POST /api/loans`: Create a new loan.
*   `PUT /api/loans`: Update an existing loan.
*   `DELETE /api/loans/{id}`: Delete a loan by ID.
*   `GET /api/loans/{id}`: Get a loan by ID.
*   `GET /api/loans`: Get all loans.

### Loan Operations
*   `POST /api/operation/loan`: Process a loan application.
*   `POST /api/operation/installment`: Process an installment payment.

## Testing

To run the unit and integration tests:

```bash
mvn test
```
