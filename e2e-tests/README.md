# E2E Tests (Playwright + Cucumber)

This module contains end-to-end UI tests for the EditorManager application using **Playwright** for browser automation and **Cucumber** for BDD-style test scenarios.

---

## Features

- **Cross-browser testing** with Playwright (Chromium, Firefox, WebKit)
- **BDD approach** with Cucumber feature files
- **Page Object Model** (POM) pattern for maintainable tests
- **Allure reporting** for detailed test results
- **Screenshot capture** on test failures
- **Test data management** with JSON files
- **JSON schema validation** for API responses

---

## Prerequisites

- **Java 17+**
- **Maven 3.8+** (or use the included `mvnw` wrapper)
- **Backend API** running and accessible
- **Frontend application** running and accessible
- **Docker** (optional, for running tests in containers)

---

## Project Structure

```
e2e-tests/
├── src/
│   └── test/
│       ├── java/com/project/
│       │   ├── base/              # Base test classes
│       │   ├── config/            # Configuration and factories
│       │   ├── model/              # Data models
│       │   ├── ui/
│       │   │   ├── cucumber/      # Cucumber step definitions and runners
│       │   │   ├── pages/         # Page Object Model classes
│       │   │   └── *Test.java     # JUnit test classes
│       │   └── utils/              # Utility classes
│       └── resources/
│           ├── features/            # Cucumber feature files (.feature)
│           ├── schemas/             # JSON schema files
│           ├── testdata/            # Test data JSON files
│           ├── allure.properties    # Allure configuration
│           └── junit-platform.properties
├── pom.xml                          # Maven configuration
├── Dockerfile                       # Docker configuration
└── screenshots/                     # Test screenshots
```

---

## Setup

### 1. Environment Configuration

Ensure both backend and frontend are running:

- **Backend API**: `http://localhost:8080` (or configured endpoint)
- **Frontend UI**: `http://localhost:4200` (or configured endpoint)

### 2. Environment Variables

When running via Docker Compose, tests use:
- `E2E_BASE_URL_UI` - Frontend URL (default: `http://frontend:4200`)
- `E2E_BASE_URL_API` - Backend API URL (default: `http://backend:8080`)

For local execution, configure in test classes or via system properties.

### 3. Browser Configuration

Tests can run on multiple browsers:
- **chromium** (default)
- **firefox**
- **webkit**

Set browser via system property:
```bash
-Dbrowser=firefox
```

---

## Running Tests

### Option 1: Using the Root Script (Recommended)

From the project root:

**Run all browsers:**
```bash
./run-e2e.sh dev all
```

**Run specific browser:**
```bash
./run-e2e.sh dev chromium
./run-e2e.sh dev firefox
./run-e2e.sh dev webkit
```

This script:
1. Runs tests on specified browsers
2. Generates Allure reports
3. Starts an Allure server on port 8083

### Option 2: Using Maven Directly

**Run all tests (default browser):**
```bash
cd e2e-tests
./mvnw clean test
```

**Run with specific browser:**
```bash
./mvnw test -Dbrowser=firefox
```

**Run only JUnit tests:**
```bash
./mvnw test -Dtest='com.project.ui.*Test'
```

**Run only Cucumber tests:**
```bash
./mvnw test -Dtest='CucumberE2ERunnerTest'
```

**Run with Cucumber tags:**
```bash
./mvnw test -Dcucumber.filter.tags="@smoke"
```

### Option 3: Using Docker Compose

From the project root:

```bash
docker-compose -f docker-compose.dev.yml run --rm e2e-tests mvn test
```

### Option 4: Generate Allure Report

After running tests:

```bash
./mvnw allure:report
open target/site/allure-maven-plugin/index.html
```

---

## Test Types

### 1. JUnit UI Tests

Located in `src/test/java/com/project/ui/`:
- `AuthorsPageTest.java` - Authors page tests
- `AuthorFormTest.java` - Author form tests

These tests use Playwright's browser automation API.

### 2. Cucumber BDD Tests

**Feature files** in `src/test/resources/features/`:
- `authors.feature` - Author-related UI scenarios

**Step definitions** in `src/test/java/com/project/ui/cucumber/steps/`

**Test runner** in `src/test/java/com/project/ui/cucumber/runners/`

---

## Page Object Model

The project uses the Page Object Model pattern for maintainable tests.

### Page Class Example

```java
public class AuthorsListPage {
    private final Page page;
    
    public AuthorsListPage(Page page) {
        this.page = page;
    }
    
    public void navigate() {
        page.navigate("http://localhost:4200/authors");
    }
    
    public Locator getAuthorsTable() {
        return page.locator("table");
    }
    
    public void clickAddAuthor() {
        page.click("button:has-text('Add Author')");
    }
}
```

### Using Page Objects in Tests

```java
@Test
public void testAuthorsPage() {
    AuthorsListPage authorsPage = new AuthorsListPage(page);
    authorsPage.navigate();
    assertTrue(authorsPage.getAuthorsTable().isVisible());
}
```

---

## Writing Tests

### JUnit Test Example

```java
@Test
public void testCreateAuthor() {
    page.navigate("http://localhost:4200/authors");
    page.click("button:has-text('Add Author')");
    page.fill("input[name='name']", "John Doe");
    page.fill("input[name='email']", "john@example.com");
    page.click("button:has-text('Save')");
    
    assertTrue(page.locator("text=John Doe").isVisible());
}
```

### Cucumber Feature Example

```gherkin
Feature: Authors Management
  Scenario: Create and display author
    Given I am on the authors page
    When I click "Add Author"
    And I fill in the author form with:
      | name  | email           |
      | John  | john@example.com |
    And I click "Save"
    Then I should see "John" in the authors table
```

### Step Definition Example

```java
@When("I click {string}")
public void iClick(String buttonText) {
    page.click("button:has-text('" + buttonText + "')");
}
```

---

## Screenshots

Screenshots are automatically captured:
- On test failures
- At specific test steps (if configured)

Screenshots are saved in:
- `target/screenshots/` - During test execution
- `screenshots/` - Project-level screenshots

### Manual Screenshot Capture

```java
page.screenshot(new Page.ScreenshotOptions()
    .setPath(Paths.get("screenshot.png"))
);
```

---

## Configuration

### Playwright Configuration

Browser configuration in `PlaywrightFactory`:
- Headless mode (configurable)
- Viewport size
- Browser context options
- Timeout settings

### Allure Configuration

`src/test/resources/allure.properties`:
```properties
allure.results.directory=target/allure-results
```

### Cucumber Configuration

Configured via Maven properties and annotations.

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

Load test data:
```java
ObjectMapper mapper = new ObjectMapper();
JsonNode testData = mapper.readTree(
    getClass().getResourceAsStream("/testdata/authors.json")
);
String name = testData.get("validAuthor").get("name").asText();
```

---

## Hooks

Cucumber hooks are available for setup and teardown:

**`PlaywrightHooks.java`** - Browser setup/teardown
**`ScenarioLifecycleHooks.java`** - Scenario-level hooks

Example:
```java
@Before
public void setUp() {
    browser = playwright.chromium().launch();
    page = browser.newPage();
}

@After
public void tearDown() {
    browser.close();
}
```

---

## Allure Reports

### Generate Report

```bash
./mvnw allure:report
```

### View Report

```bash
open target/site/allure-maven-plugin/index.html
```

### Using Allure Docker Service

The `run-e2e.sh` script automatically starts an Allure server:

```bash
./run-e2e.sh dev all
```

Access at: **http://localhost:8083**

Stop the server:
```bash
docker stop allure-server-e2e
```

---

## Continuous Integration

Tests are configured for CI/CD:

- **GitHub Actions**: See `.github/workflows/playwright_cucumber_e2e-tests.yml`
- Runs on multiple browsers
- Generates and publishes Allure reports
- Runs on push, PR, or manual dispatch

---

## Troubleshooting

### Browser Launch Issues

- Install Playwright browsers: `mvn exec:java -Dexec.mainClass="com.microsoft.playwright.CLI" -Dexec.args="install"`
- Check browser permissions
- Verify display server (for headless mode)

### Test Failures

- Check Allure reports for detailed information
- Review screenshots in `target/screenshots/`
- Verify frontend and backend are running
- Check network connectivity

### Timeout Issues

- Increase timeout in Playwright configuration
- Check application performance
- Verify network latency

---

## Dependencies

Key dependencies:
- **Playwright** 1.53.0 - Browser automation
- **JUnit 5** 5.12.0 - Testing framework
- **Cucumber** 7.20.1 - BDD framework
- **Allure** 2.29.0 - Test reporting
- **Jackson** - JSON processing
- **AssertJ** - Assertions

---

## Best Practices

1. **Use Page Object Model** for maintainable tests
2. **Keep test data separate** from test code
3. **Use descriptive test names** and scenario descriptions
4. **Take screenshots** on failures for debugging
5. **Use BDD** with Cucumber for readable scenarios
6. **Tag tests** appropriately for selective execution
7. **Clean up test data** after tests complete
8. **Wait for elements** instead of using fixed delays

---

## License

MIT License

