package com.project.base;

import com.microsoft.playwright.APIRequestContext;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.LoadState;
import com.project.config.PlaywrightFactory;
import io.qameta.allure.Allure;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

import java.io.ByteArrayInputStream;

public abstract class BaseTest {

  protected BrowserContext context;
  protected Page page;
  protected APIRequestContext api;

  protected static final String BASE_UI_URL = System.getenv().getOrDefault("E2E_BASE_URL_UI", "http://frontend:4200");
  protected static final String BASE_API_URL = System.getenv().getOrDefault("E2E_BASE_URL_API", "http://backend:8080");

//  protected static final String BASE_UI_URL = "http://localhost:4200";
//  protected static final String BASE_API_URL = "http://localhost:8080";

  @BeforeEach
  void setup() {
    // Read browser from Maven property or default to chromium
    String browserName = System.getProperty("browser", "chromium");

    // Initialize Playwright & browser (singleton)
    PlaywrightFactory.initBrowser(browserName, true);

    // Each test gets its own context/page for isolation
    context = PlaywrightFactory.createContext();
    page = context.newPage();

    // API context
    api = PlaywrightFactory.createApiRequestContext(BASE_API_URL);
  }

  @AfterEach
  void teardown() {
    if (context != null) context.close();
    if (api != null) api.dispose();
  }

  protected void navigateTo(String path) {
    page.navigate(BASE_UI_URL + path);
    page.waitForLoadState(LoadState.NETWORKIDLE);
  }

  protected void takeScreenshot(String name) {
    var screenshot = page.screenshot(new Page.ScreenshotOptions().setFullPage(true));
    Allure.addAttachment(name, new ByteArrayInputStream(screenshot));
  }
}
