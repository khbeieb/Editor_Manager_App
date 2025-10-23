# Editor Manager App

## Overview

Editor Manager App is a full-stack application for managing authors and books. It consists of a Java Spring Boot backend, an Angular frontend, and comprehensive automated tests (API, E2E Playwright, and E2E Selenium) with Allure reporting and CI/CD workflows via GitHub Actions.

---

## Project Structure

```
.
├── backend/         # Java Spring Boot backend
├── frontend/        # Angular frontend
├── api-tests/       # API and Cucumber integration tests (Java, Playwright)
├── e2e-tests/       # E2E UI tests (Java, Playwright, Cucumber)
├── e2e-selenium/    # E2E UI tests (Java, Selenium, TestNG)
├── config/          # Environment variable files
├── .github/workflows/ # CI/CD workflows
├── docker-compose*.yml # Docker Compose files for different environments
├── launch.sh        # Script to start the stack
├── run-api-tests.sh # Script to run API tests
├── run-cucumber-tests.sh # Script to run API+Cucumber tests
├── run-e2e.sh       # Script to run Playwright E2E tests
├── run_e2e_selenium.sh # Script to run Selenium E2E tests
```

---

## Running the Application

### Prerequisites

- [Docker](https://www.docker.com/)
- [Docker Compose](https://docs.docker.com/compose/)
- Java 17+ (for local backend/tests)
- Node.js & npm (for local frontend)

### 1. Backend

#### Run with Docker Compose

```sh
./launch.sh dev
```

This will start the backend, frontend, and database using Docker Compose with the `config/.env.dev` file.

#### Run Locally

```sh
cd backend
./mvnw spring-boot:run
```

---

### 2. Frontend

#### Run with Docker Compose

The frontend will be started automatically with `./launch.sh dev`.

#### Run Locally

```sh
cd frontend
npm install
ng serve
```

Visit [http://localhost:4200](http://localhost:4200).

---

## Running Tests

### 1. API Tests

- **Run locally or in Docker:**

```sh
./run-api-tests.sh dev
```

- **GitHub Actions Workflow:**  
  See [.github/workflows/integration-tests.yml](.github/workflows/integration-tests.yml)

---

### 2. API + Cucumber Integration Tests

- **Run locally or in Docker:**

```sh
./run-cucumber-tests.sh dev
```

- **GitHub Actions Workflow:**  
  See [.github/workflows/integration-tests.yml](.github/workflows/integration-tests.yml)

---

### 3. E2E UI Tests (Playwright + Cucumber)

- **Run locally or in Docker:**

```sh
./run-e2e.sh dev all
```

- **GitHub Actions Workflow:**  
  See [.github/workflows/playwright_cucumber_e2e-tests.yml](.github/workflows/playwright_cucumber_e2e-tests.yml)

  - Runs on push, PR, or manual dispatch.
  - Runs E2E tests for Chromium, Firefox, and WebKit.
  - Generates Allure reports and publishes them to GitHub Pages.

---

### 4. E2E UI Tests (Selenium + TestNG)

- **Run locally or in Docker:**

```sh
./run_e2e_selenium.sh dev all
```

- **GitHub Actions Workflow:**  
  See [.github/workflows/selenium-e2e-tests.yml](.github/workflows/selenium-e2e-tests.yml)

  - Runs on push, PR, or manual dispatch.
  - Runs Selenium E2E tests for Chrome and Firefox.
  - Generates Allure reports and publishes them to GitHub Pages.

---

## Allure Reports

- After running tests, Allure reports are generated in the respective `target/allure-report` or `target/allure-results` directories.
- Reports are also published automatically to GitHub Pages by the workflows.

---

## Environment Configuration

- Environment variables are managed in the `config/` directory (e.g., `.env.dev`, `.env.staging`, `.env.prod`).
- Update these files to configure database, ports, and other settings.

---

## CI/CD Workflows

- All workflows are defined in [.github/workflows/](.github/workflows/)
- Workflows include:
  - Integration/API tests
  - Playwright E2E tests
  - Selenium E2E tests
  - Scheduled nightly runs

---

## Useful Commands

| Command                        | Description                          |
|---------------------------------|--------------------------------------|
| `./launch.sh dev`              | Start full stack (backend, frontend) |
| `./run-api-tests.sh dev`        | Run API tests                        |
| `./run-cucumber-tests.sh dev`   | Run API + Cucumber tests             |
| `./run-e2e.sh dev all`          | Run Playwright E2E tests             |
| `./run_e2e_selenium.sh dev all` | Run Selenium E2E tests               |

---

## Contributing

1. Fork the repo and create your branch.
2. Make your changes and add tests.
3. Submit a pull request.

---

## License

MIT License
