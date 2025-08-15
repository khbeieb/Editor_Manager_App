package com.project.ui;

import com.project.base.BaseTest;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.options.LoadState;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class AuthorsPageTest extends BaseTest {

  @Test
  void shouldDisplayAuthorsLibraryTitle() {
    // Navigate to authors page using helper
    navigateTo("/authors");

    // Wait until the network is idle
    page.waitForLoadState(LoadState.NETWORKIDLE);

    // Locate the title by data-testid
    Locator titleLocator = page.locator("[data-testid='authors-title']");

    // Get and trim the text content
    String actualTitle = titleLocator.textContent().trim();
    System.out.println("✅ Found title: " + actualTitle);

    // Assert title contains expected text
    assertTrue(
      actualTitle.contains("Authors Library"),
      "Expected 'Authors Library' in title but got: " + actualTitle
    );
  }
}
