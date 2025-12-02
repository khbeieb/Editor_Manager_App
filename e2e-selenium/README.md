# E2E Tests (Selenium + TestNG)

This module contains end-to-end UI tests for the EditorManager application using **Selenium WebDriver** with **Selenide** wrapper and **TestNG** testing framework.

---

## Features

- **Cross-browser testing** with Selenium (Chrome, Firefox)
- **Selenide** wrapper for concise and readable test code
- **TestNG** framework for flexible test execution
- **Allure reporting** for detailed test results
- **Page Object Model** (POM) pattern
- **Selenium Grid** support for distributed testing
- **Screenshot capture** on test failures

---

## Prerequisites

- **Java 17+**
- **Maven 3.8+** (or use the included `mvnw` wrapper)
- **Backend API** running and accessible
- **Frontend application** running and accessible
- **Docker** (optional, for Selenium Grid and containerized execution)

---

## Project Structure

```
e2e-selenium/
├── src/
│   ├── main/java/org/mobelite/utils/  # Utility classes
│   └── test/
│       ├── java/org/mobelite/
│       │   ├── base/                  # Base test classes
│       │   ├── pages/                 # Page Object Model classes
│       │   ├── tests/                 # TestNG test classes
│       │   └── utils/                 # Test utilities
│       └── resources/
│           ├── testng.xml              # TestNG suite configuration
│           └── testng-browsers.xml     # Browser-specific configuration
├── pom.xml                             # Maven configuration
├── Dockerfile                          # Docker configuration
└── browsers.json                       # Browser configuration
```

---

## Setup

### 1. Environment Configuration

Ensure both backend and frontend are running:

- **Backend API**: `http://localhost:8080` (or configured endpoint)
- **Frontend UI**: `http://localhost:4200` (or configured endpoint)

### 2. Selenium Grid (Optional)

For distributed testing, use Selenium Grid via Docker Compose:

```bash
docker-compose -f docker-compose.dev.yml up selenium-hub selenium-chrome selenium-firefox
```

Selenium Hub will be available at: `http://localhost:4444`

### 3. Browser Drivers

Selenide automatically manages browser drivers. No manual installation needed.

For manual setup:
- **Chrome**: ChromeDriver (automatically downloaded by Selenide)
- **Firefox**: GeckoDriver (automatically downloaded by Selenide)

---

## Running Tests

### Option 1: Using the Root Script (Recommended)

From the project root:

**Run all browsers:**
```bash
./run_e2e_selenium.sh dev all
```

**Run specific browser:**
```bash
./run_e2e_selenium.sh dev chrome
./run_e2e_selenium.sh dev firefox
```

This script:
1. Runs tests on specified browsers
2. Generates Allure reports
3. Starts an Allure server on port 8083

### Option 2: Using Maven Directly

**Run all tests:**
```bash
cd e2e-selenium
./mvnw clean test
```

**Run with specific TestNG suite:**
```bash
./mvnw test -Dsurefire.suiteXmlFiles=src/test/resources/testng.xml
```

**Run with browser-specific suite:**
```bash
./mvnw test -Dsurefire.suiteXmlFiles=src/test/resources/testng-browsers.xml
```

**Run specific test class:**
```bash
./mvnw test -Dtest=AuthorFormTest
```

**Run with TestNG groups:**
```bash
./mvnw test -Dgroups=smoke
```

### Option 3: Using Docker Compose

From the project root:

```bash
docker-compose -f docker-compose.dev.yml run --rm e2e-selenium mvn clean test
```

### Option 4: Using TestNG XML Directly

Configure test execution in `src/test/resources/testng.xml`:

```xml
<suite name="E2E Test Suite">
    <test name="Chrome Tests">
        <parameter name="browser" value="chrome"/>
        <classes>
            <class name="org.mobelite.tests.AuthorFormTest"/>
        </classes>
    </test>
</suite>
```

Run:
```bash
./mvnw test -Dsurefire.suiteXmlFiles=src/test/resources/testng.xml
```

---

## Test Configuration

### TestNG Configuration

**`src/test/resources/testng.xml`:**
```xml
<?xml version="1.0" encoding="UTF-8"?>
<suite name="E2E Test Suite">
    <test name="All Tests">
        <classes>
            <class name="org.mobelite.tests.AuthorFormTest"/>
            <class name="org.mobelite.tests.AuthorsPageTest"/>
        </classes>
    </test>
</suite>
```

**`src/test/resources/testng-browsers.xml`:**
Browser-specific configuration for parallel execution.

### Selenide Configuration

Configure in test classes or via system properties:

```java
Configuration.browser = "chrome";
Configuration.headless = false;
Configuration.timeout = 10000;
Configuration.baseUrl = "http://localhost:4200";
```

### Browser Configuration

**`browsers.json`** contains browser-specific settings for Selenium Grid.

---

## Writing Tests

### TestNG Test Example

```java
@Test
public void testCreateAuthor() {
    open("/authors");
    $(By.cssSelector("button")).click();
    $(By.name("name")).setValue("John Doe");
    $(By.name("email")).setValue("john@example.com");
    $(By.cssSelector("button[type='submit']")).click();
    
    $("table").shouldBe(visible);
    $("table").shouldHave(text("John Doe"));
}
```

### Page Object Model Example

**Page Class:**
```java
public class AuthorsListPage {
    public static SelenideElement authorsTable = $("table");
    public static SelenideElement addButton = $("button:has-text('Add Author')");
    
    public static void navigate() {
        open("/authors");
    }
    
    public static void clickAddAuthor() {
        addButton.click();
    }
    
    public static void verifyAuthorExists(String name) {
        authorsTable.shouldHave(text(name));
    }
}
```

**Test Class:**
```java
public class AuthorsPageTest extends BaseTest {
    @Test
    public void testAuthorsPage() {
        AuthorsListPage.navigate();
        AuthorsListPage.clickAddAuthor();
        AuthorsListPage.verifyAuthorExists("John Doe");
    }
}
```

---

## Base Test Class

All tests extend `BaseTest` which provides:

- Browser initialization
- Common setup/teardown
- Allure integration
- Screenshot capture on failures

```java
public class BaseTest {
    @BeforeMethod
    public void setUp() {
        Configuration.browser = "chrome";
        Configuration.baseUrl = "http://localhost:4200";
    }
    
    @AfterMethod
    public void tearDown(ITestResult result) {
        if (result.getStatus() == ITestResult.FAILURE) {
            Allure.addAttachment("Screenshot", 
                new ByteArrayInputStream(
                    Screenshots.takeScreenShotAsBytes()
                )
            );
        }
    }
}
```

---

## Screenshots

Selenide automatically captures screenshots on test failures.

Screenshots are saved in:
- `target/screenshots/` - During test execution
- Attached to Allure reports automatically

### Manual Screenshot Capture

```java
Screenshot screenshot = Screenshots.takeScreenShotAsFile();
Allure.addAttachment("Custom Screenshot", 
    new FileInputStream(screenshot));
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

The `run_e2e_selenium.sh` script automatically starts an Allure server:

```bash
./run_e2e_selenium.sh dev all
```

Access at: **http://localhost:8083**

Stop the server:
```bash
docker stop allure-server-selenium
```

---

## Selenium Grid

### Starting Selenium Grid

Using Docker Compose:

```bash
docker-compose -f docker-compose.dev.yml up selenium-hub selenium-chrome selenium-firefox
```

### Configuring Tests for Grid

Set the remote URL in test configuration:

```java
Configuration.remote = "http://localhost:4444/wd/hub";
Configuration.browser = "chrome";
```

Or via environment variable:
```bash
export SELENIUM_REMOTE_URL=http://selenium-hub:4444/wd/hub
```

---

## TestNG Features

### Groups

Tag tests with groups:

```java
@Test(groups = {"smoke", "regression"})
public void testAuthorCreation() {
    // test code
}
```

Run specific groups:
```bash
./mvnw test -Dgroups=smoke
```

### Parameters

Use parameters in TestNG XML:

```xml
<parameter name="browser" value="chrome"/>
```

Access in tests:
```java
@Parameters("browser")
@BeforeMethod
public void setUp(String browser) {
    Configuration.browser = browser;
}
```

### Data Providers

```java
@DataProvider(name = "authorData")
public Object[][] authorData() {
    return new Object[][] {
        {"John Doe", "john@example.com"},
        {"Jane Smith", "jane@example.com"}
    };
}

@Test(dataProvider = "authorData")
public void testCreateAuthor(String name, String email) {
    // test code
}
```

---

## Continuous Integration

Tests are configured for CI/CD:

- **GitHub Actions**: See `.github/workflows/selenium-e2e-tests.yml`
- Runs on Chrome and Firefox
- Generates and publishes Allure reports
- Runs on push, PR, or manual dispatch

---

## Troubleshooting

### Browser Launch Issues

- Check browser is installed (Chrome/Firefox)
- Verify browser driver compatibility
- Check Selenide configuration
- Review browser logs

### Selenium Grid Issues

- Verify Selenium Hub is running: `curl http://localhost:4444/status`
- Check node registration
- Verify network connectivity
- Review Grid logs

### Test Failures

- Check Allure reports for detailed information
- Review screenshots in `target/screenshots/`
- Verify frontend and backend are running
- Check element selectors are correct

### Timeout Issues

- Increase Selenide timeout: `Configuration.timeout = 20000`
- Check application performance
- Verify network latency

---

## Dependencies

Key dependencies:
- **Selenide** 7.6.0 - Selenium wrapper
- **TestNG** - Testing framework
- **Allure TestNG** 2.29.0 - Allure integration
- **Allure Selenide** 2.29.0 - Selenide integration
- **AssertJ** - Assertions
- **AspectJ** - Allure aspect weaving

---

## Best Practices

1. **Use Page Object Model** for maintainable tests
2. **Use Selenide's concise API** for readable code
3. **Leverage TestNG features** (groups, parameters, data providers)
4. **Take screenshots** on failures for debugging
5. **Use descriptive test names** and method names
6. **Tag tests** with TestNG groups for selective execution
7. **Clean up test data** after tests complete
8. **Wait for elements** using Selenide's built-in waits
9. **Use Selenium Grid** for parallel execution
10. **Generate Allure reports** for test analysis

---

## Comparison with Playwright E2E Tests

This module uses **Selenium + TestNG**, while `e2e-tests/` uses **Playwright + Cucumber**:

| Feature | Selenium (This Module) | Playwright (e2e-tests/) |
|---------|------------------------|--------------------------|
| Framework | TestNG | JUnit 5 + Cucumber |
| Browser Automation | Selenium WebDriver | Playwright |
| BDD Support | No | Yes (Cucumber) |
| API Testing | No | Yes |
| Cross-browser | Chrome, Firefox | Chromium, Firefox, WebKit |
| Reporting | Allure | Allure |

Choose based on your needs:
- **Selenium + TestNG**: If you prefer TestNG features and Selenium ecosystem
- **Playwright + Cucumber**: If you need BDD and modern browser automation

---

## License

MIT License

