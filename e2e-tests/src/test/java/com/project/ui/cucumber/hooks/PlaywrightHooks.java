package com.project.ui.cucumber.hooks;

import com.microsoft.playwright.*;
import io.cucumber.java.After;
import io.cucumber.java.Before;

public class PlaywrightHooks {
  public static Playwright playwright;
  public static Browser browser;
  public static Page page;

  // Base URLs from Maven system properties (set by run-e2e.sh based on run mode)
  // Local mode: http://localhost:4200 / http://localhost:8080
  // Docker mode: http://frontend:4200 / http://backend:8080
  // Fallback to environment variables for backward compatibility
  public static final String BASE_UI_URL = System.getProperty("e2e.base.url.ui", 
    System.getenv().getOrDefault("E2E_BASE_URL_UI", "http://localhost:4200"));
  public static final String BASE_API_URL = System.getProperty("e2e.base.url.api", 
    System.getenv().getOrDefault("E2E_BASE_URL_API", "http://localhost:8080"));
  @Before
  public void setUp() {
    playwright = Playwright.create();
    browser = playwright.chromium().launch(new BrowserType.LaunchOptions().setHeadless(true));
    page = browser.newPage();
  }

  @After
  public void tearDown() {
    if (page != null) page.close();
    if (browser != null) browser.close();
    if (playwright != null) playwright.close();
  }
}
