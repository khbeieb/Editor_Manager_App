package com.project.api.cucumber.fixtures;

import com.microsoft.playwright.*;
import io.cucumber.java.After;
import io.cucumber.java.AfterAll;
import io.cucumber.java.Before;
import java.util.Arrays;

public class PlaywrightCucumberFixtures {

  protected static final String BASE_API_URL = "http://localhost:8080";
    // System.getenv().getOrDefault("E2E_BASE_URL_API", "http://backend:8080");

  // ThreadLocal ensures each scenario/thread gets its own instance
  private static final ThreadLocal<Playwright> playwright = ThreadLocal.withInitial(() -> {
    Playwright pw = Playwright.create();
    pw.selectors().setTestIdAttribute("data-test");
    return pw;
  });

  private static final ThreadLocal<Browser> browser = ThreadLocal.withInitial(() ->
    playwright.get().chromium().launch(new BrowserType.LaunchOptions()
      .setHeadless(true)
      .setArgs(Arrays.asList("--no-sandbox", "--disable-extensions", "--disable-gpu")))
  );

  private static final ThreadLocal<BrowserContext> browserContext = new ThreadLocal<>();
  private static final ThreadLocal<Page> page = new ThreadLocal<>();
  private static final ThreadLocal<APIRequestContext> api = new ThreadLocal<>();

  /**
   * Runs before each scenario to create a new context, page, and API context
   */
  @Before(order = 100)
  public void setUpScenario() {
    BrowserContext context = browser.get().newContext();
    browserContext.set(context);
    page.set(context.newPage());

    APIRequest.NewContextOptions options = new APIRequest.NewContextOptions()
      .setBaseURL(BASE_API_URL);
    api.set(playwright.get().request().newContext(options));
  }

  /**
   * Runs after each scenario to clean up the browser context, page, and API context
   */
  @After(order = 100)
  public void tearDownScenario() {
    if (browserContext.get() != null) {
      browserContext.get().close();
      browserContext.remove();
    }

    if (page.get() != null) {
      page.remove();
    }

    if (api.get() != null) {
      api.get().dispose();
      api.remove();
    }
  }

  /**
   * Runs once after all scenarios to close the browser and Playwright
   */
  @AfterAll
  public static void tearDownAll() {
    if (browser.get() != null) {
      browser.get().close();
      browser.remove();
    }

    if (playwright.get() != null) {
      playwright.get().close();
      playwright.remove();
    }
  }

  // Static getters for step definitions
  public static Page getPage() {
    return page.get();
  }

  public static BrowserContext getBrowserContext() {
    return browserContext.get();
  }

  public static APIRequestContext getApi() {
    return api.get();
  }
}
