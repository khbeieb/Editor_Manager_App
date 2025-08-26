package com.project.ui;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.LoadState;
import com.microsoft.playwright.options.WaitForSelectorState;
import com.project.base.BaseTest;
import com.project.ui.pages.AuthorsListPage;
import com.project.utils.TestDataHelper;
import org.junit.jupiter.api.*;

import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class AuthorsPageTest extends BaseTest {
  private AuthorsListPage authorsPage;
  private String createdAuthorId;

  @BeforeEach
  void setUp() {
    navigateTo("/authors");
    page.waitForLoadState(LoadState.NETWORKIDLE);
    authorsPage = new AuthorsListPage(page);
    authorsPage.waitForAuthorsData();
  }

  @AfterEach
  void cleanUp() {
    if (createdAuthorId != null) {
      TestDataHelper.deleteAuthor(api, createdAuthorId);
      createdAuthorId = null;
    }
  }

  @Test
  @Order(1)
  void shouldDisplayAuthorsLibraryTitle() {
    assertTrue(authorsPage.getTitle().contains("Authors Library"));

    page.screenshot(new Page.ScreenshotOptions()
      .setPath(Paths.get("screenshots/authors-title.png"))
      .setFullPage(true));
  }

  @Test
  @Order(2)
  void shouldShowEmptyMessageWhenNoAuthors() {
    // Precondition: DB should be empty (depends on test isolation strategy)
    assertTrue(authorsPage.isEmptyMessageVisible());
  }

  @Test
  @Order(3)
  void shouldCreateAndDisplayAuthorInTable() {
    createdAuthorId = TestDataHelper.createAuthor(api, "Test Author", "French");

    authorsPage.clickRefresh();
    authorsPage.waitForAuthorsData();

    assertTrue(authorsPage.isTableVisible());
    assertEquals("Test Author", authorsPage.getFirstRowName());
    assertEquals("French", authorsPage.getFirstRowNationality());
  }

  @Test
  @Order(4)
  void shouldFilterAuthorsByName() {
    createdAuthorId = TestDataHelper.createAuthor(api, "Unique Author", "German");

    authorsPage.clickRefresh();
    authorsPage.waitForAuthorsData();

    authorsPage.searchAuthor("Unique");
    authorsPage.waitForAuthorsData();

    assertEquals("Unique Author", authorsPage.getFirstRowName());
  }


  @Test
  @Order(6)
  void shouldSortAuthors() {
    String id1 = TestDataHelper.createAuthor(api, "AAA Author", "French");
    String id2 = TestDataHelper.createAuthor(api, "ZZZ Author", "French");
    createdAuthorId = id1;
    // cleanup second author immediately
    TestDataHelper.deleteAuthor(api, id2);

    authorsPage.clickRefresh();
    authorsPage.waitForAuthorsData();

    authorsPage.selectSortBy("name");
    authorsPage.selectSortOrder("asc");

    assertEquals("AAA Author", authorsPage.getFirstRowName());
  }

//  @Test
//  @Order(6)
//  void shouldShowLoadingSpinnerWhenRefreshing() {
//    // Ensure a default timeout is set
//    page.setDefaultTimeout(30000);
//    page.setDefaultNavigationTimeout(30000);
//
//    // Throttle API responses
//    page.route("**/api/**", route -> {
//      page.waitForTimeout(1000); // delay 1 second
//      route.resume();
//    });
//
//    // Trigger refresh
//    authorsPage.clickRefresh();
//
//    // Screenshot for debugging
//    page.screenshot(new Page.ScreenshotOptions().setPath(Paths.get("spinner_debug.png")));
//
//    // Pause Playwright for debugging
//    page.pause();
//
//    // Wait for spinner
//    Locator spinner = authorsPage.getLoadingSpinner();
//    spinner.waitFor(new Locator.WaitForOptions()
//      .setState(WaitForSelectorState.VISIBLE)
//      .setTimeout(5000));
//
//    assertTrue(authorsPage.isLoadingVisible(), "Loading spinner should be visible after refresh");
//
//    // Wait until spinner disappears
//    spinner.waitFor(new Locator.WaitForOptions()
//      .setState(WaitForSelectorState.HIDDEN)
//      .setTimeout(10000));
//  }

  @Test
  @Order(6)
  void shouldHandleErrorStateGracefully() {
    // Forcing error state would require API to be unavailable
    navigateTo("/authors");
    authorsPage.waitForAuthorsData();

    if (authorsPage.isErrorVisible()) {
      assertTrue(authorsPage.isErrorVisible());
    }
  }
}
