package org.mobelite.base;

import io.qameta.allure.Attachment;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.*;
import org.openqa.selenium.edge.*;
import org.openqa.selenium.firefox.*;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.testng.ITestResult;
import org.testng.annotations.*;

import java.net.URL;
import java.time.Duration;

public class BaseTest {

  protected WebDriver driver;

  protected static final String BASE_URL =
    System.getenv("E2E_BASE_URL_UI") != null
      ? System.getenv("E2E_BASE_URL_UI")
      : "http://localhost:4200"; // fallback for local runs
  // Base URL
//  protected static final String BASE_URL = "http://localhost:4200";

  // Remote Selenium URL (inside Docker network)
  private static final String SELENIUM_URL = System.getenv("SELENIUM_REMOTE_URL");

  // Retry config
  private static final int MAX_RETRIES = 5;
  private static final int RETRY_DELAY_MS = 5000;

  @Parameters("browser")
  @BeforeMethod(alwaysRun = true)
  public void setUp(@Optional("chrome") String browser) throws Exception {
    System.out.println("🌐 Launching browser: " + browser);

    MutableCapabilities options = getOptions(browser);

    if (SELENIUM_URL != null && !SELENIUM_URL.isEmpty()) {
      System.out.println("🔗 Using remote Selenium at " + SELENIUM_URL);

      int attempt = 0;
      while (attempt < MAX_RETRIES) {
        try {
          driver = new RemoteWebDriver(new URL(SELENIUM_URL), options);
          System.out.println("✅ RemoteWebDriver session created successfully");
          break;
        } catch (Exception e) {
          attempt++;
          System.err.println("⚠️ Retry " + attempt + " — Failed to create RemoteWebDriver: " + e.getMessage());
          Thread.sleep(RETRY_DELAY_MS);
        }
      }

      if (driver == null)
        throw new RuntimeException("Could not create RemoteWebDriver after " + MAX_RETRIES + " attempts");
    } else {
      System.out.println("🧩 Running locally with " + browser);
      driver = createLocalDriver(browser);
    }

    driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(10));
    driver.manage().timeouts().pageLoadTimeout(Duration.ofSeconds(60));
    driver.manage().timeouts().scriptTimeout(Duration.ofSeconds(30));
  }

  private MutableCapabilities getOptions(String browser) {
    switch (browser.toLowerCase()) {
      case "firefox" -> {
        FirefoxOptions ff = new FirefoxOptions();
        ff.addArguments("-headless");
        return ff;
      }
      case "edge" -> {
        EdgeOptions edge = new EdgeOptions();
        edge.addArguments("--headless=new", "--disable-gpu");
        return edge;
      }
      default -> {
        ChromeOptions chrome = new ChromeOptions();
        chrome.addArguments("--headless=new", "--no-sandbox", "--disable-dev-shm-usage", "--window-size=1920,1080");
        return chrome;
      }
    }
  }

  private WebDriver createLocalDriver(String browser) {
    return switch (browser.toLowerCase()) {
      case "firefox" -> new FirefoxDriver((FirefoxOptions) getOptions(browser));
      case "edge" -> new EdgeDriver((EdgeOptions) getOptions(browser));
      default -> new ChromeDriver((ChromeOptions) getOptions(browser));
    };
  }

  protected void navigateTo(String path) {
    driver.get(BASE_URL + path);
  }

  @AfterMethod(alwaysRun = true)
  public void tearDown(ITestResult result) {
    if (result != null && !result.isSuccess()) {
      takeScreenshot(result.getName());
    }

    if (driver != null) {
      driver.quit();
      driver = null;
    }
  }

  @Attachment(value = "Screenshot on failure - {0}", type = "image/png")
  public byte[] takeScreenshot(String name) {
    try {
      return ((TakesScreenshot) driver).getScreenshotAs(OutputType.BYTES);
    } catch (Exception e) {
      return new byte[0];
    }
  }
}
