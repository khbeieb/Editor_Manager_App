package org.mobelite.base;

import io.qameta.allure.Attachment;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.testng.ITestResult;
import org.testng.annotations.*;

import java.net.MalformedURLException;
import java.net.URL;
import java.time.Duration;

public class BaseTest {

  protected WebDriver driver;
  protected static final String BASE_URL = System.getenv().getOrDefault("E2E_BASE_URL_UI", "http://frontend:4200");

  @BeforeMethod(alwaysRun = true)
  public void setUp() throws MalformedURLException {
    ChromeOptions options = new ChromeOptions();
    options.addArguments("--headless"); // ✅ safer for Docker
    options.addArguments("--no-sandbox");
    options.addArguments("--disable-dev-shm-usage");
    options.addArguments("--disable-gpu");
    options.addArguments("--window-size=1920,1080");

    driver = new RemoteWebDriver(new URL("http://selenium:4444"), options);
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
