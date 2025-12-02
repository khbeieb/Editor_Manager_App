# EditorManager Backend

EditorManager is a Spring Boot REST API application designed to manage Authors, Books, Magazines, and Publications. It provides comprehensive REST endpoints with validation, error handling, and API documentation.

---

## Features

- **RESTful API** for managing Authors, Books, Magazines, and Publications
- **Multiple environments** support (dev, staging, prod) via Spring profiles
- **DTO-based architecture** for clean API request and response contracts
- **Input validation** using Jakarta Bean Validation
- **Comprehensive error handling** with global exception handlers
- **API documentation** via Swagger/OpenAPI
- **Unit tested services** with Mockito and JUnit 5
- **MySQL database** integration with JPA/Hibernate
- **CORS configuration** for frontend integration

---

## Prerequisites

- **Java 17+** (JDK 17 or higher)
- **Maven 3.8+** (or use the included `mvnw` wrapper)
- **MySQL 8.0+** or compatible database
- **Bash shell** (Linux, macOS, or Windows with WSL/Git Bash)

---

## Project Structure

```
backend/
├── src/
│   ├── main/
│   │   ├── java/org/mobelite/editormanager/
│   │   │   ├── config/          # Configuration classes (CORS, OpenAPI)
│   │   │   ├── controllers/      # REST controllers
│   │   │   ├── dto/              # Data Transfer Objects
│   │   │   ├── entities/         # JPA entities
│   │   │   ├── enums/            # Enumeration types
│   │   │   ├── exceptions/       # Exception handlers
│   │   │   ├── mappers/          # Entity-DTO mappers
│   │   │   ├── repositories/    # JPA repositories
│   │   │   ├── services/         # Business logic services
│   │   │   └── EditorManagerApplication.java
│   │   └── resources/
│   │       └── application.properties
│   └── test/                     # Unit and integration tests
├── pom.xml                       # Maven configuration
├── launch.sh                     # Launch script
└── test.sh                       # Test execution script
```

---

## Setup

### 1. Database Configuration

Ensure MySQL is running and create a database:

```sql
CREATE DATABASE editormanager;
```

### 2. Environment Configuration

Create environment configuration files in the `../config/` directory:

**`config/.env.dev`** (example):
```bash
SPRING_PROFILES_ACTIVE=dev
MYSQL_HOST=localhost
MYSQL_PORT=3306
MYSQL_DB=editormanager
MYSQL_USER=root
MYSQL_PASSWORD=your_password
```

**`config/.env.staging`** and **`config/.env.prod`** should follow the same format with appropriate values.

### 3. Application Properties

The `application.properties` file uses environment variables for configuration:
- Database connection settings
- JPA/Hibernate configuration
- Logging levels
- CORS settings

---

## Running the Application

### Option 1: Using the Launch Script (Recommended)

The `launch.sh` script builds the application and runs it with the specified environment:

```bash
cd backend
./launch.sh dev
```

This will:
1. Load environment variables from `config/.env.dev`
2. Build the application (`mvn clean package`)
3. Run the JAR file with the dev profile

### Option 2: Using Maven Directly

Set environment variables and run:

```bash
export SPRING_PROFILES_ACTIVE=dev
export MYSQL_HOST=localhost
export MYSQL_PORT=3306
export MYSQL_DB=editormanager
export MYSQL_USER=root
export MYSQL_PASSWORD=your_password

./mvnw spring-boot:run
```

Or if Maven is installed globally:

```bash
mvn spring-boot:run
```

### Option 3: Running the Pre-built JAR

If the JAR is already built:

```bash
export SPRING_PROFILES_ACTIVE=dev
export MYSQL_HOST=localhost
export MYSQL_PORT=3306
export MYSQL_DB=editormanager
export MYSQL_USER=root
export MYSQL_PASSWORD=your_password

java -jar target/EditorManager-0.0.1-SNAPSHOT.jar
```

### Option 4: Using Docker Compose

From the project root:

```bash
docker-compose -f docker-compose.dev.yml up backend db
```

---

## API Documentation (Swagger UI)

Once the application is running, access the interactive API documentation:

🔗 **Swagger UI**: [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)

The Swagger UI allows you to:
- Browse all REST endpoints
- View request/response schemas
- Execute API calls interactively
- Test different endpoints with sample data

---

## API Endpoints

### Authors
- `GET /api/authors` - Get all authors
- `GET /api/authors/{id}` - Get author by ID
- `POST /api/authors` - Create a new author
- `PUT /api/authors/{id}` - Update an author
- `DELETE /api/authors/{id}` - Delete an author

### Books
- `GET /api/books` - Get all books
- `GET /api/books/{id}` - Get book by ID
- `POST /api/books` - Create a new book
- `PUT /api/books/{id}` - Update a book
- `DELETE /api/books/{id}` - Delete a book

### Magazines
- `GET /api/magazines` - Get all magazines
- `GET /api/magazines/{id}` - Get magazine by ID
- `POST /api/magazines` - Create a new magazine
- `PUT /api/magazines/{id}` - Update a magazine
- `DELETE /api/magazines/{id}` - Delete a magazine

### Publications
- `GET /api/publications` - Get all publications
- `GET /api/publications/{id}` - Get publication by ID
- `POST /api/publications` - Create a new publication
- `PUT /api/publications/{id}` - Update a publication
- `DELETE /api/publications/{id}` - Delete a publication

---

## Running Tests

### Unit Tests

Run all unit tests:

```bash
./test.sh dev
```

Or using Maven:

```bash
./mvnw test
```

### Test Coverage

The project uses JaCoCo for code coverage. Generate coverage reports:

```bash
./mvnw clean verify
```

Coverage reports will be available in `target/site/jacoco/index.html`

---

## Configuration

### Environment Variables

| Variable | Description | Default |
|----------|-------------|---------|
| `SPRING_PROFILES_ACTIVE` | Active Spring profile | `dev` |
| `MYSQL_HOST` | MySQL host address | - |
| `MYSQL_PORT` | MySQL port | `3306` |
| `MYSQL_DB` | Database name | - |
| `MYSQL_USER` | Database user | `dev_user` |
| `MYSQL_PASSWORD` | Database password | `dev_pass` |

### Application Properties

Key properties in `application.properties`:
- `spring.jpa.hibernate.ddl-auto=update` - Automatically update database schema
- `spring.jpa.show-sql=true` - Show SQL queries in logs
- `server.address=0.0.0.0` - Listen on all network interfaces
- `frontend.origin=http://localhost:4200` - CORS allowed origin

---

## Development

### Building the Project

```bash
./mvnw clean package
```

### Running in Development Mode

For hot-reload during development, use Spring Boot DevTools (if configured) or run with:

```bash
./mvnw spring-boot:run
```

### Code Style

The project uses:
- **Lombok** for reducing boilerplate code
- **Spring Boot** best practices
- **RESTful API** design principles

---

## Troubleshooting

### Database Connection Issues

- Verify MySQL is running: `mysql -u root -p`
- Check database exists: `SHOW DATABASES;`
- Verify credentials in environment variables
- Check network connectivity to MySQL host

### Port Already in Use

If port 8080 is already in use:
- Change the port in `application.properties`: `server.port=8081`
- Or stop the process using port 8080

### Build Failures

- Ensure Java 17+ is installed: `java -version`
- Clean and rebuild: `./mvnw clean install`
- Check Maven wrapper permissions: `chmod +x mvnw`

---

## Dependencies

Key dependencies:
- **Spring Boot 3.5.3** - Framework
- **Spring Data JPA** - Database access
- **MySQL Connector** - MySQL driver
- **Lombok** - Code generation
- **SpringDoc OpenAPI** - API documentation
- **Jakarta Validation** - Input validation
- **JUnit 5** - Testing framework
- **Mockito** - Mocking framework

---

## License

MIT License
