package com.project.base;

import com.microsoft.playwright.APIRequest;
import com.microsoft.playwright.APIRequestContext;
import com.microsoft.playwright.Playwright;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;

/**
 * Base class for API tests using Playwright Java.
 * Handles Playwright initialization and APIRequestContext creation.
 */
public abstract class BaseApiTest {

  protected static Playwright playwright;
  protected static APIRequestContext api;

  // Base API URL, can be overridden via environment variables
  protected static final String BASE_API_URL =
    System.getenv().getOrDefault("E2E_BASE_URL_API", "http://backend:8080");

  @BeforeAll
  static void globalSetup() {
    // Initialize Playwright (required to create API contexts)
    playwright = Playwright.create();

    // Create API request context with base URL
    api = playwright.request().newContext(
      new APIRequest.NewContextOptions()
        .setBaseURL(BASE_API_URL)
        .setExtraHTTPHeaders(
          java.util.Map.of(
            "Accept", "application/json",
            "Content-type", "application/json"
          )
        )
    );
  }

  @AfterAll
  static void globalTeardown() {
    if (api != null) {
      api.dispose();
    }
    if (playwright != null) {
      playwright.close();
    }
  }
}
