package com.tests;

import com.microsoft.playwright.*;
import com.microsoft.playwright.options.LoadState;
import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class HomePageTest {
  static Playwright playwright;
  static Browser browser;

  @BeforeAll
  static void setUp() {
    playwright = Playwright.create();
    browser = playwright.chromium().launch(
      new BrowserType.LaunchOptions().setHeadless(true)
    );
  }

  @AfterAll
  static void tearDown() {
    browser.close();
    playwright.close();
  }

  @Test
  void shouldLoadHomePage() {
    BrowserContext context = browser.newContext();
    Page page = context.newPage();

    String baseUrl = System.getenv().getOrDefault("E2E_BASE_URL", "http://localhost:4200");
    page.navigate(baseUrl);
    page.waitForLoadState(LoadState.NETWORKIDLE); // Ensure page fully loaded

    Locator title = page.locator("[data-testid='authors-title']");

    String text = title.textContent().toLowerCase();
    System.out.println("✅ Title found: " + text);
    assertTrue(text.contains("authors library"), "Expected title to contain 'authors library'");
  }
}
