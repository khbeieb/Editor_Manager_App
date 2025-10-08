package org.mobelite.base;

import io.qameta.allure.Attachment;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.testng.ITestResult;
import org.testng.annotations.*;

import java.time.Duration;

public class BaseTest {

  protected WebDriver driver;
  protected static final String BASE_URL = "http://localhost:4200";

  @BeforeMethod(alwaysRun = true)
  public void setUp() {
    ChromeOptions options = new ChromeOptions();
    options.addArguments("--headless=new"); // Use "--headless=new" for latest Chrome versions
    options.addArguments("--disable-gpu");  // Recommended for headless
    options.addArguments("--window-size=1920,1080"); // Optional: set resolution
    options.addArguments("--no-sandbox");   // Optional: useful in CI environments
    options.addArguments("--disable-dev-shm-usage"); // Optional: avoid limited resource issues

    driver = new ChromeDriver(options);
    driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(10));
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
