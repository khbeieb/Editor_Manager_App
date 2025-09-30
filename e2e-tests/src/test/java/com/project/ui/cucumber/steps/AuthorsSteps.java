package com.project.ui.cucumber.steps;

import com.microsoft.playwright.APIRequestContext;
import com.microsoft.playwright.Playwright;
import com.microsoft.playwright.Page;
import com.project.ui.cucumber.hooks.PlaywrightHooks;
import com.project.ui.pages.AuthorsListPage;
import com.project.utils.TestDataHelper;
import io.cucumber.java.After;
import io.cucumber.java.en.*;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;

public class AuthorsSteps {

  private AuthorsListPage authorsPage;
  private final Playwright playwright = PlaywrightHooks.playwright;
  private final Page page = PlaywrightHooks.page;

  // API context using base URL from hooks
  private final APIRequestContext api = playwright.request().newContext(
    new com.microsoft.playwright.APIRequest.NewContextOptions().setBaseURL(PlaywrightHooks.BASE_API_URL)
  );

  // Track created authors to clean up after each scenario
  public static List<String> createdAuthorIds = new ArrayList<>();

  @Given("the user navigates to the authors page")
  public void navigateToAuthorsPage() {
    page.navigate(PlaywrightHooks.BASE_UI_URL + "/authors");
    authorsPage = new AuthorsListPage(page);
    authorsPage.waitForAuthorsData();
  }

  @Then("the page title should contain {string}")
  public void pageTitleShouldContain(String expected) {
    assertThat(authorsPage.getTitle()).contains(expected);
  }

  @Then("the empty message should be visible")
  public void emptyMessageVisible() {
    assertThat(authorsPage.isEmptyMessageVisible()).isTrue();
  }

  @When("the user creates an author {string} with nationality {string}")
  public void createAuthor(String name, String nationality) {
    String authorId = TestDataHelper.createAuthor(api, name, nationality);
    if (authorId != null && !authorId.isEmpty()) {
      createdAuthorIds.add(authorId);
    }
  }

  @When("the user refreshes the authors list")
  public void refreshAuthors() {
    authorsPage.clickRefresh();
    authorsPage.waitForAuthorsData();
  }

  @When("the user searches for {string}")
  public void searchAuthor(String name) {
    authorsPage.searchAuthor(name);
    authorsPage.waitForAuthorsData();
  }

  @Then("the first author row should have name {string}")
  public void firstAuthorRowHasName(String expected) {
    assertThat(authorsPage.getFirstRowName()).isEqualTo(expected);
  }

  @Then("the first author row should have nationality {string}")
  public void firstAuthorRowHasNationality(String expected) {
    assertThat(authorsPage.getFirstRowNationality()).isEqualTo(expected);
  }

  @When("the user sorts by {string} in {string} order")
  public void sortAuthors(String sortBy, String order) {
    authorsPage.selectSortBy(sortBy);
    authorsPage.selectSortOrder(order);
  }

  @Then("the error message may be visible")
  public void errorMessageMayBeVisible() {
    if (authorsPage.isErrorVisible()) {
      assertThat(authorsPage.isErrorVisible()).isTrue();
    }
  }

  @After
  public void cleanupCreatedAuthors() {
    for (String authorId : new ArrayList<>(createdAuthorIds)) {
      try {
        TestDataHelper.deleteAuthor(api, authorId);
      } catch (Exception e) {
        System.err.println("Failed to delete author " + authorId + ": " + e.getMessage());
      }
    }
    createdAuthorIds.clear();
  }
}
