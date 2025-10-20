package org.mobelite.base;

import io.qameta.allure.Attachment;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.edge.EdgeOptions;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.testng.ITestResult;
import org.testng.annotations.*;

import java.net.URL;
import java.time.Duration;

public class BaseTest {

  protected WebDriver driver;

  // Base URL for your frontend
  protected static final String BASE_URL = System.getenv().getOrDefault(
    "E2E_BASE_URL_UI", "http://frontend:4200");
   // protected static final String BASE_URL = "http://localhost:4200";
  // Remote Selenium URL (inside Docker network)
  private static final String SELENIUM_URL = System.getenv("SELENIUM_REMOTE_URL");

  // Retry config
  private static final int MAX_RETRIES = 5;
  private static final int RETRY_DELAY_MS = 5000;

  @BeforeMethod(alwaysRun = true)
  public void setUp() throws Exception {

    ChromeOptions options = new ChromeOptions();
    options.addArguments("--headless=new");
    options.addArguments("--no-sandbox");
    options.addArguments("--disable-dev-shm-usage");
    options.addArguments("--disable-gpu");
    options.addArguments("--window-size=1920,1080");

    if (SELENIUM_URL != null && !SELENIUM_URL.isEmpty()) {
      System.out.println("🔗 Using remote Selenium at " + SELENIUM_URL);

      int attempt = 0;
      while (attempt < MAX_RETRIES) {
        try {
          driver = new RemoteWebDriver(new URL(SELENIUM_URL), options);
          System.out.println("✅ RemoteWebDriver session created successfully");
          break;
        } catch (Exception e) { // Catch all for connection/session issues
          attempt++;

          System.err.println("⚠️ Failed to create RemoteWebDriver session (attempt " + attempt + "): " + e.getMessage());
          Thread.sleep(RETRY_DELAY_MS);
        }
      }

      if (driver == null) {
        throw new RuntimeException("Could not create RemoteWebDriver session after " + MAX_RETRIES + " attempts");
      }

    } else {
      System.out.println("🧩 Running locally with ChromeDriver");
      driver = new ChromeDriver(options);
    }

    // Set timeouts
    driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(10));
    driver.manage().timeouts().pageLoadTimeout(Duration.ofSeconds(60));
    driver.manage().timeouts().scriptTimeout(Duration.ofSeconds(30));
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
