package com.project.config;

import com.microsoft.playwright.*;

public class PlaywrightFactory {

  private static Playwright playwright;
  private static Browser browser;

  /**
   * Initialize Playwright and launch a browser instance.
   * @param browserName chromium, firefox, webkit
   * @param headless true = headless mode
   */
  public static void initBrowser(String browserName, boolean headless) {
    if (playwright == null) {
      playwright = Playwright.create();
    }

    if (browser == null) {
      switch (browserName.toLowerCase()) {
        case "firefox":
          browser = playwright.firefox().launch(new BrowserType.LaunchOptions().setHeadless(headless));
          break;
        case "webkit":
          browser = playwright.webkit().launch(new BrowserType.LaunchOptions().setHeadless(headless));
          break;
        default:
          browser = playwright.chromium().launch(new BrowserType.LaunchOptions().setHeadless(headless));
          break;
      }
    }
  }

  /** Create a fresh isolated context (for parallel tests). */
  public static BrowserContext createContext() {
    if (browser == null) throw new IllegalStateException("Browser not initialized. Call initBrowser() first.");
    return browser.newContext();
  }

  /** Create a new Page in a fresh context. */
  public static Page createPage() {
    return createContext().newPage();
  }

  /** Create APIRequestContext for API tests. */
  public static APIRequestContext createApiRequestContext(String baseUrl) {
    return playwright.request().newContext(
      new APIRequest.NewContextOptions().setBaseURL(baseUrl)
    );
  }

  /** Close browser and Playwright. */
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
