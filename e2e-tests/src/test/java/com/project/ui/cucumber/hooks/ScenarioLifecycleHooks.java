package com.project.ui.cucumber.hooks;

import com.microsoft.playwright.APIRequest;
import com.microsoft.playwright.APIRequestContext;
import com.project.ui.cucumber.steps.AuthorsSteps;
import com.project.utils.TestDataHelper;
import io.cucumber.java.After;
import io.cucumber.java.AfterStep;
import io.cucumber.java.Scenario;

import java.nio.file.Paths;
import java.util.ArrayList;

public class ScenarioLifecycleHooks {

  @After
  public void cleanupCreatedAuthors() {
    if (AuthorsSteps.createdAuthorIds.isEmpty()) return;

    try {
      // Use same Playwright instance as in tests
      APIRequestContext tempApi = PlaywrightHooks.playwright.request().newContext(
        new APIRequest.NewContextOptions().setBaseURL(PlaywrightHooks.BASE_API_URL)
      );

      for (String authorId : new ArrayList<>(AuthorsSteps.createdAuthorIds)) {
        try {
          TestDataHelper.deleteAuthor(tempApi, authorId);
        } catch (Exception e) {
          System.err.println("⚠️ Failed to delete author " + authorId + ": " + e.getMessage());
        }
      }
    } catch (Exception e) {
      System.err.println("⚠️ Cleanup error: " + e.getMessage());
    }

    AuthorsSteps.createdAuthorIds.clear();
  }

  @AfterStep
  public void takeScreenshotOnFailure(Scenario scenario) {
    if (scenario.isFailed() && PlaywrightHooks.page != null) {
      try {
        // Attach screenshot to Cucumber report
        byte[] screenshot = PlaywrightHooks.page.screenshot(
          new com.microsoft.playwright.Page.ScreenshotOptions().setFullPage(true)
        );
        scenario.attach(screenshot, "image/png", "screenshot");

        // Save screenshot locally
        PlaywrightHooks.page.screenshot(
          new com.microsoft.playwright.Page.ScreenshotOptions()
            .setPath(Paths.get("target/screenshots/" + scenario.getName().replace(" ", "_") + ".png"))
            .setFullPage(true)
        );
      } catch (Exception e) {
        System.err.println("⚠️ Failed to capture screenshot: " + e.getMessage());
      }
    }
  }
}
