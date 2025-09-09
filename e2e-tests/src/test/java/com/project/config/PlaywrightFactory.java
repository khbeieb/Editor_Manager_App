package com.project.config;

import com.microsoft.playwright.*;

public class PlaywrightFactory {

  private static Playwright playwright;
  private static Browser browser;

  /**
   * Initialize Playwright and launch a browser instance.
   */
  public static void initBrowser(boolean headless) {
    if (playwright == null) {
      playwright = Playwright.create();
    }

    if (browser == null) {
      browser = playwright.chromium().launch(
        new BrowserType.LaunchOptions()
          .setHeadless(headless)
      );
    }
  }

  /**
   * Create a new isolated BrowserContext
   */
  public static BrowserContext createContext() {
    if (browser == null) {
      throw new IllegalStateException("Browser not initialized. Call initBrowser() first.");
    }
    return browser.newContext();
  }

  /**
   * Create a new Page inside a fresh context.
   */
  public static Page createPage() {
    return createContext().newPage();
  }

  /**
   * Create a Playwright APIRequestContext for API testing.
   */
  public static APIRequestContext createApiRequestContext(String baseUrl) {
    return playwright.request().newContext(
      new APIRequest.NewContextOptions().setBaseURL(baseUrl)
    );
  }

  /**
   * Clean up all Playwright resources.
   */
  public static void close() {
    if (browser != null) {
      browser.close();
      browser = null;
    }
    if (playwright != null) {
      playwright.close();
      playwright = null;
    }
  }
}
