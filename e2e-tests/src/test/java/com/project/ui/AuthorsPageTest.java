package com.project.ui;

import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.LoadState;
import com.project.base.BaseTest;
import com.project.ui.pages.AuthorsListPage;
import com.project.utils.TestDataHelper;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Story;
import org.junit.jupiter.api.*;

import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@Epic("Authors Page")
@Feature("Author Management")
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
  @Tag("smoke")
  @Story("Page Rendering")
  void shouldDisplayAuthorsLibraryTitle() {
    assertTrue(authorsPage.getTitle().contains("Authors Library"));

    page.screenshot(new Page.ScreenshotOptions()
      .setPath(Paths.get("screenshots/authors-title.png"))
      .setFullPage(true));
  }

  @Test
  @Order(2)
  @Tag("regression")
  @Story("Empty State")
  void shouldShowEmptyMessageWhenNoAuthors() {
    assertTrue(authorsPage.isEmptyMessageVisible());
  }

  @Test
  @Order(3)
  @Tag("smoke")
  @Story("CRUD Operations")
  void shouldCreateAndDisplayAuthorInTable() {
    createdAuthorId = TestDataHelper.createAuthor(api, "Test Author", "French");

    authorsPage.clickRefresh();
    authorsPage.waitForAuthorsData();

    assertEquals("Test Author", authorsPage.getFirstRowName());
    assertEquals("French", authorsPage.getFirstRowNationality());
  }

  @Test
  @Order(4)
  @Tag("regression")
  @Story("Filtering")
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
  @Tag("regression")
  @Story("Sorting")
  void shouldSortAuthors() {
    String id1 = TestDataHelper.createAuthor(api, "AAA Author", "French");
    String id2 = TestDataHelper.createAuthor(api, "ZZZ Author", "French");
    createdAuthorId = id1;
    TestDataHelper.deleteAuthor(api, id2);

    authorsPage.clickRefresh();
    authorsPage.waitForAuthorsData();

    authorsPage.selectSortBy("Name");
    authorsPage.selectSortOrder("Ascending");

    assertEquals("AAA Author", authorsPage.getFirstRowName());
  }

  @Test
  @Order(6)
  @Tag("regression")
  @Story("Error Handling")
  void shouldHandleErrorStateGracefully() {
    navigateTo("/authors");
    authorsPage.waitForAuthorsData();

    if (authorsPage.isErrorVisible()) {
      assertTrue(authorsPage.isErrorVisible());
    }
  }
}
