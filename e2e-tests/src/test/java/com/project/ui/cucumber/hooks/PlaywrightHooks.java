package com.project.ui.cucumber.hooks;

import com.microsoft.playwright.*;
import io.cucumber.java.After;
import io.cucumber.java.Before;

public class PlaywrightHooks {
  public static Playwright playwright;
  public static Browser browser;
  public static Page page;

  // Base URLs from environment variables, fallback to defaults
  public static final String BASE_UI_URL = System.getenv().getOrDefault("E2E_BASE_URL_UI", "http://frontend:4200");
  public static final String BASE_API_URL = System.getenv().getOrDefault("E2E_BASE_URL_API", "http://backend:8080");
//  public static final String BASE_UI_URL = "http://localhost:4200";
//  public static final String BASE_API_URL = "http://localhost:8080";
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
