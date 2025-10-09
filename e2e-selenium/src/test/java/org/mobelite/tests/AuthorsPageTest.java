package org.mobelite.tests;

import io.qameta.allure.*;
import org.mobelite.base.BaseTest;
import org.mobelite.pages.AuthorsListPage;
import org.mobelite.utils.TestDataHelper;
import org.testng.Assert;
import org.testng.annotations.*;

import java.net.MalformedURLException;

@Epic("Authors Page")
@Feature("Author Management")
public class AuthorsPageTest extends BaseTest {
  private AuthorsListPage authorsPage;
  private String createdAuthorId;

  @BeforeMethod
  public void setUpPage() throws Exception {
    super.setUp();
    navigateTo("/authors");
    authorsPage = new AuthorsListPage(driver);
  }

  @AfterMethod(alwaysRun = true)
  public void cleanUp() {
    if (createdAuthorId != null) {
      TestDataHelper.deleteAuthor(createdAuthorId);
      createdAuthorId = null;
    }
  }

  @Test(priority = 1, description = "Should display authors page title")
  @Story("Page Rendering")
  @Severity(SeverityLevel.CRITICAL)
  public void shouldDisplayAuthorsLibraryTitle() {
    Assert.assertTrue(authorsPage.getTitle().contains("Authors Library"));
  }

  @Test(priority = 2, description = "Should show empty state when no authors")
  @Story("Empty State")
  public void shouldShowEmptyMessageWhenNoAuthors() {
    Assert.assertTrue(authorsPage.isEmptyMessageVisible());
  }

  @Test(priority = 3, description = "Should create and display new author in table")
  @Story("CRUD Operations")
  public void shouldCreateAndDisplayAuthorInTable() {
    createdAuthorId = TestDataHelper.createAuthor("Test Author", "French");
    authorsPage.clickRefresh();
    Assert.assertEquals(authorsPage.getFirstRowName(), "Test Author");
    Assert.assertEquals(authorsPage.getFirstRowNationality(), "French");
  }

  @Test(priority = 4, description = "Should filter authors by name")
  @Story("Filtering")
  public void shouldFilterAuthorsByName() {
    createdAuthorId = TestDataHelper.createAuthor("Unique Author", "German");
    authorsPage.clickRefresh();
    authorsPage.searchAuthor("Unique");
    Assert.assertEquals(authorsPage.getFirstRowName(), "Unique Author");
  }

  @Test(priority = 5, description = "Should sort authors")
  @Story("Sorting")
  public void shouldSortAuthors() {
    TestDataHelper.createAuthor("AAA Author", "French");
    createdAuthorId = TestDataHelper.createAuthor("ZZZ Author", "French");
    authorsPage.clickRefresh();
    authorsPage.selectSortBy("Name");
    authorsPage.selectSortOrder("Ascending");
    Assert.assertEquals(authorsPage.getFirstRowName(), "AAA Author");
  }

  @Test(priority = 6, description = "Should handle error state gracefully")
  @Story("Error Handling")
  public void shouldHandleErrorStateGracefully() {
    Assert.assertTrue(authorsPage.isErrorVisible() || authorsPage.isTableVisible());
  }
}
