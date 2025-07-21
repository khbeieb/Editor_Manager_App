# EditorManager

EditorManager is a Spring Boot application designed to manage Authors, Books, Magazines, and Publications. It provides REST APIs to create, retrieve, and manage these entities.

---

## Features

- Manage authors and their books
- Manage magazines and associated authors
- Supports multiple environments (dev, staging, prod) via profiles and config files
- Uses DTOs for clean API request and response contracts
- Validation on API inputs
- Unit tested services with Mockito and JUnit 5

---

## Prerequisites

- Java 17+
- Maven 3.8+
- MySQL or compatible database configured for your environment
- Bash shell (Linux, macOS, or Windows with WSL/Git Bash)

---

## Setup

1. Configure your environment files under `./config/.env.dev`, `.env.staging`, `.env.prod`, etc.

2. Each `.env.<env>` file should contain environment variables such as:

    ```bash
    SPRING_PROFILES_ACTIVE=dev
    MYSQL_HOST=localhost
    MYSQL_PORT=3306
    MYSQL_USER=root
    MYSQL_PASSWORD=secret
    # other env variables your app needs
    ```
## API Documentation (Swagger UI)

Once the application is running, access the Swagger UI here:

🔗 [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)

Use this interface to:

- Browse all REST endpoints
- View and validate request/response structures
- Execute test requests interactively

---

## Running the Application

Use the provided `launch.sh` script to run the application with your desired environment:

```bash
./launch.sh dev
```

## Running Tests
```bash
./test.sh dev
```