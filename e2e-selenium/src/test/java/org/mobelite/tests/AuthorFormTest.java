package org.mobelite.tests;

import io.qameta.allure.*;
import org.mobelite.base.BaseTest;
import org.mobelite.pages.AuthorFormPage;
import org.testng.Assert;
import org.testng.annotations.*;

import java.io.IOException;

@Epic("Authors Form")
@Feature("Author Management")
public class AuthorFormTest extends BaseTest {

  private AuthorFormPage formPage;

  @BeforeMethod
  public void setUpForm() throws Exception {
    // super.setUp();
    navigateTo("/authors/new");
    formPage = new AuthorFormPage(driver);
    formPage.waitForForm();
  }

  @AfterMethod(alwaysRun = true)
  public void cleanUp() {
  }

  @Test(priority = 1, description = "Should show validation errors for empty fields")
  @Story("Validation")
  @Severity(SeverityLevel.NORMAL)
  public void shouldShowValidationErrorsForEmptyFields() {
    formPage.triggerEmptyValidation();
    Assert.assertTrue(formPage.isNameErrorVisible(), "Expected name error to be visible");
    Assert.assertTrue(formPage.isNationalityErrorVisible(), "Expected nationality error to be visible");
  }

  @Test(priority = 2, description = "Should add book to author form")
  @Story("Books")
  @Severity(SeverityLevel.NORMAL)
  public void shouldAddBookToAuthor() {
    formPage.addBook("Book One", "5161515115", "01/01/2020");
    Assert.assertEquals(formPage.getFirstBookTitle(), "Book One");
  }

  @Test(priority = 3, description = "Should remove book from author form")
  @Story("Books")
  @Severity(SeverityLevel.NORMAL)
  public void shouldRemoveBookFromAuthor() {
    formPage.addBook("Book To Remove", "978-3-16-148410-0", "01/01/2020");
    formPage.removeBookAndConfirm(0);
    Assert.assertTrue(formPage.isBookListEmpty(), "Expected book to be removed from the form");
  }

  @Test(priority = 4, description = "Should submit author form successfully")
  @Story("Form Submission")
  @Severity(SeverityLevel.CRITICAL)
  public void shouldSubmitAuthorFormSuccessfully() throws IOException {
    formPage.fillAuthorForm("New Author", "French");
    formPage.saveAuthor();

    Assert.assertTrue(formPage.isAuthorCreatedToastVisible(), "Expected toast message not found");

    // formPage.takeLocalScreenshot("AuthorForm_Submission.png");
  }

  @Test(priority = 5, description = "Should cancel author form and return to list")
  @Story("Form Navigation")
  @Severity(SeverityLevel.MINOR)
  public void shouldCancelAuthorForm() {
    formPage.cancelForm();
    formPage.confirmDialog();
    Assert.assertTrue(formPage.isOnAuthorsListPage(), "Expected to navigate back to authors list");
  }
}
