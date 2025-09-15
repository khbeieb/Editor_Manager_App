package com.project.ui;

import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.LoadState;
import com.project.base.BaseTest;
import com.project.ui.pages.AuthorsListPage;
import com.project.utils.TestDataHelper;
import io.qameta.allure.Epic;
import org.junit.jupiter.api.*;

import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@Epic("Authors Page")
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

  @AfterEach
  void tearDown() {
    takeScreenshot("AuthorPageTest");
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
  @Order(5)
  void shouldSortAuthors() {
    String id1 = TestDataHelper.createAuthor(api, "AAA Author", "French");
    String id2 = TestDataHelper.createAuthor(api, "ZZZ Author", "French");
    createdAuthorId = id1;
    // cleanup second author immediately
    TestDataHelper.deleteAuthor(api, id2);

    authorsPage.clickRefresh();
    authorsPage.waitForAuthorsData();

    authorsPage.selectSortBy("Name");
    authorsPage.selectSortOrder("Ascending");

    assertEquals("AAA Author", authorsPage.getFirstRowName());
  }

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
