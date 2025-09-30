package com.project.base;

import com.microsoft.playwright.APIRequestContext;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.LoadState;
import com.project.config.PlaywrightFactory;
import io.qameta.allure.Allure;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;

import java.io.ByteArrayInputStream;

public abstract class BaseTest {

  protected static BrowserContext context;
  protected static Page page;
  protected static APIRequestContext api;

  // Base URLs for UI and API
    protected static final String BASE_UI_URL = System.getenv().getOrDefault("E2E_BASE_URL_UI", "http://frontend:4200");

    protected static final String BASE_API_URL = System.getenv().getOrDefault("E2E_BASE_URL_API", "http://backend:8080");
   //TODO: Remove after testing
//  protected static final String BASE_UI_URL = "localhost:4200";
//   protected static final String BASE_API_URL = "localhost:8080";
  @BeforeAll
  static void globalSetup() {
    // Initialize browser via factory
    PlaywrightFactory.initBrowser(true); // true = headless

    // Create context & page for UI tests
    context = PlaywrightFactory.createContext();
    page = context.newPage();

    // Create API request context
    api = PlaywrightFactory.createApiRequestContext(BASE_API_URL);
  }

  @AfterAll
  static void globalTeardown() {
    if (context != null) {
      context.close();
    }
    if (api != null) {
      api.dispose();
    }
    PlaywrightFactory.close();
  }

  /**
   * Navigate to a relative path on the UI using BASE_UI_URL.
   * @param relativePath e.g. "/authors" or "/books"
   */
  protected void navigateTo(String relativePath) {
    page.navigate(BASE_UI_URL + relativePath);
    page.waitForLoadState(LoadState.NETWORKIDLE);

  }

  protected void takeScreenshot(String name) {
    var screenshot = page.screenshot(
      new Page.ScreenshotOptions().setFullPage(true)
    );

    Allure.addAttachment(name, new ByteArrayInputStream(screenshot));
  }
}
