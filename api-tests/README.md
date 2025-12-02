# API Tests

This module contains comprehensive API integration tests for the EditorManager backend. Tests are written using **Playwright**, **JUnit 5**, and **Cucumber** for BDD-style test scenarios.

---

## Features

- **REST API testing** using Playwright's HTTP client
- **BDD approach** with Cucumber feature files
- **JSON schema validation** for API responses
- **Allure reporting** for detailed test results
- **Test data management** with JSON files
- **Multiple test types**: Unit API tests and Cucumber integration tests

---

## Prerequisites

- **Java 17+**
- **Maven 3.8+** (or use the included `mvnw` wrapper)
- **Backend API** running and accessible
- **Docker** (optional, for running tests in containers)

---

## Project Structure

```
api-tests/
├── src/
│   └── test/
│       ├── java/com/project/api/
│       │   ├── base/              # Base test classes
│       │   ├── cucumber/           # Cucumber step definitions and runners
│       │   ├── services/           # API service classes
│       │   ├── tests/              # JUnit API test classes
│       │   └── utils/              # Utility classes
│       └── resources/
│           ├── features/            # Cucumber feature files (.feature)
│           ├── schemas/             # JSON schema files for validation
│           ├── testdata/            # Test data JSON files
│           ├── allure.properties    # Allure configuration
│           ├── cucumber.properties  # Cucumber configuration
│           └── junit-platform.properties
├── pom.xml                          # Maven configuration
└── Dockerfile                       # Docker configuration
```

---

## Setup

### 1. Environment Configuration

Ensure the backend API is running and accessible. Configure the API base URL:

**Option A: Environment Variable**
```bash
export API_BASE_URL=http://localhost:8080
```

**Option B: Docker Compose**
The tests use environment variables from `config/.env.dev` when run via Docker Compose.

### 2. Test Data

Test data files are located in `src/test/resources/testdata/`:
- `authors.json` - Author test data
- `books.json` - Book test data

### 3. JSON Schemas

Response validation schemas are in `src/test/resources/schemas/`:
- `author-schema.json` - Author response schema
- `book-schema.json` - Book response schema

---

## Running Tests

### Option 1: Using the Root Script (Recommended)

From the project root:

```bash
./run-api-tests.sh dev
```

This script:
1. Runs all API tests (JUnit + Cucumber)
2. Generates Allure reports
3. Starts an Allure server on port 8083

### Option 2: Using Maven Directly

**Run all tests:**
```bash
cd api-tests
./mvnw clean test
```

**Run only JUnit API tests:**
```bash
./mvnw test -Dtest='com.project.api.tests.*Test'
```

**Run only Cucumber tests:**
```bash
./mvnw test -Dtest='CucumberRunnerTest'
```

**Run with specific tags:**
```bash
./mvnw test -Dcucumber.filter.tags="@smoke"
```

### Option 3: Using Docker Compose

From the project root:

```bash
docker-compose -f docker-compose.dev.yml run --rm api-tests mvn test
```

### Option 4: Generate Allure Report

After running tests, generate the Allure report:

```bash
./mvnw allure:report
```

View the report:
```bash
open target/site/allure-maven-plugin/index.html
```

---

## Test Types

### 1. JUnit API Tests

Located in `src/test/java/com/project/api/tests/`:
- `AuthorApiTest.java` - Author API endpoint tests
- `BookApiTest.java` - Book API endpoint tests

These tests use Playwright's HTTP client to make API calls and validate responses.

### 2. Cucumber BDD Tests

**Feature files** in `src/test/resources/features/`:
- `authors.feature` - Author-related scenarios
- `books.feature` - Book-related scenarios

**Step definitions** in `src/test/java/com/project/api/cucumber/steps/`

**Test runner** in `src/test/java/com/project/api/cucumber/runners/`

---

## Writing Tests

### JUnit Test Example

```java
@Test
public void testGetAllAuthors() {
    APIRequestContext request = playwright.request().newContext();
    APIResponse response = request.get("http://localhost:8080/api/authors");
    
    assertEquals(200, response.status());
    assertNotNull(response.json());
}
```

### Cucumber Feature Example

```gherkin
Feature: Authors API
  Scenario: Get all authors
    Given the API is available
    When I request all authors
    Then the response status should be 200
    And the response should contain authors
```

### Step Definition Example

```java
@When("I request all authors")
public void iRequestAllAuthors() {
    response = apiService.getAuthors();
}
```

---

## Configuration

### Allure Configuration

`src/test/resources/allure.properties`:
```properties
allure.results.directory=target/allure-results
```

### Cucumber Configuration

`src/test/resources/cucumber.properties`:
```properties
cucumber.publish.quiet=true
cucumber.plugin=pretty,html:target/cucumber-html-report
```

### JUnit Platform Configuration

`src/test/resources/junit-platform.properties`:
```properties
junit.jupiter.testmethod.order.default=org.junit.jupiter.api.MethodOrderer$OrderAnnotation
```

---

## Test Data Management

Test data is stored in JSON files:

**`src/test/resources/testdata/authors.json`:**
```json
{
  "validAuthor": {
    "name": "John Doe",
    "email": "john@example.com"
  }
}
```

Load test data in tests:
```java
ObjectMapper mapper = new ObjectMapper();
JsonNode testData = mapper.readTree(
    getClass().getResourceAsStream("/testdata/authors.json")
);
```

---

## JSON Schema Validation

Validate API responses against JSON schemas:

```java
import com.github.everit.json.schema.loader.SchemaLoader;
import org.json.JSONObject;

JSONObject schema = new JSONObject(
    new String(Files.readAllBytes(
        Paths.get("src/test/resources/schemas/author-schema.json")
    ))
);

SchemaLoader.load(schema).validate(responseJson);
```

---

## Allure Reports

### Generate Report

```bash
./mvnw allure:report
```

### View Report Locally

```bash
open target/site/allure-maven-plugin/index.html
```

### Using Allure Docker Service

The `run-api-tests.sh` script automatically starts an Allure server:

```bash
./run-api-tests.sh dev
```

Access the report at: **http://localhost:8083**

Stop the server:
```bash
docker stop allure-server-api
```

---

## Continuous Integration

Tests are configured to run in CI/CD pipelines:

- **GitHub Actions**: See `.github/workflows/integration-tests.yml`
- **Docker**: Tests can run in containers
- **Allure**: Reports are generated and published

---

## Troubleshooting

### API Connection Errors

- Verify backend is running: `curl http://localhost:8080/api/authors`
- Check `API_BASE_URL` environment variable
- Verify network connectivity

### Test Failures

- Check Allure reports for detailed failure information
- Review test logs in `target/surefire-reports/`
- Verify test data files are correct
- Check JSON schema files match API responses

### Maven Build Issues

- Clean and rebuild: `./mvnw clean install`
- Check Java version: `java -version` (should be 17+)
- Verify Maven wrapper permissions: `chmod +x mvnw`

---

## Dependencies

Key dependencies:
- **Playwright** 1.53.0 - HTTP client for API testing
- **JUnit 5** 5.11.3 - Testing framework
- **Cucumber** 7.20.1 - BDD framework
- **Allure** 2.29.0 - Test reporting
- **Jackson** 2.17.0 - JSON processing
- **Everit JSON Schema** 1.14.6 - Schema validation
- **AssertJ** 4.0.0 - Assertions

---

## Best Practices

1. **Use descriptive test names** that explain what is being tested
2. **Keep test data separate** from test code
3. **Validate responses** using JSON schemas
4. **Use BDD** for integration tests with Cucumber
5. **Generate Allure reports** for detailed test analysis
6. **Tag tests** appropriately for selective execution
7. **Clean up test data** after tests complete

---

## License

MIT License

