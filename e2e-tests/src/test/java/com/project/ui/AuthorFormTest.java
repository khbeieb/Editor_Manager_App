package com.project.ui;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.options.LoadState;
import com.project.base.BaseTest;
import com.project.ui.pages.AuthorFormPage;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Story;
import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@Epic("Authors Form")
@Feature("Author Management")
public class AuthorFormTest extends BaseTest {

  private AuthorFormPage formPage;

  @BeforeEach
  void setUp() {
    navigateTo("/authors/new");
    page.waitForLoadState(LoadState.NETWORKIDLE);

    formPage = new AuthorFormPage(page);
    formPage.waitForForm();
  }

  @AfterEach
  void tearDown() {
    takeScreenshot("AuthorFormTest");
  }

  @Test
  @Order(1)
  @Tag("smoke")       // ✅ quick validation
  @Story("Validation")
  void shouldShowValidationErrorsForEmptyFields() {
    formPage.getNameInput().focus();
    formPage.getNationalityInput().focus();
    formPage.getNameInput().blur();
    formPage.getNationalityInput().blur();

    assertTrue(formPage.getNameError().isVisible());
    assertTrue(formPage.getNationalityError().isVisible());
  }

  @Test
  @Order(2)
  @Tag("regression")  // ✅ more detailed regression scenario
  @Story("Books")
  void shouldAddBookToAuthor() {
    formPage.addBook("Book One", "5161515115", "01/01/2020");

    assertEquals("Book One",
      formPage.getBookRow(0).locator("[data-testid='book-title-0']").innerText());
  }

  @Test
  @Order(3)
  @Tag("regression")
  @Story("Books")
  void shouldRemoveBookFromAuthor() {
    formPage.addBook("Book To Remove", "978-3-16-148410-0", "01/01/2020");

    formPage.clickRemoveBook(0);

    Locator dialogMask = page.locator(".p-dialog-mask");
    dialogMask.waitFor(new Locator.WaitForOptions()
      .setState(com.microsoft.playwright.options.WaitForSelectorState.VISIBLE)
      .setTimeout(5000));
    dialogMask.locator("button.p-confirmdialog-accept-button").click();

    formPage.getBookRow(0).waitFor(new Locator.WaitForOptions()
      .setState(com.microsoft.playwright.options.WaitForSelectorState.DETACHED));

    assertFalse(formPage.getBookRow(0).isVisible());
  }

  @Test
  @Order(4)
  @Tag("smoke")       // ✅ fast critical path: form submission
  @Story("Form Submission")
  void shouldSubmitAuthorFormSuccessfully() {
    formPage.getNameInput().fill("New Author");
    formPage.getNationalityInput().fill("French");

    formPage.setBirthDate();
    formPage.getSaveAuthorButton().click();

    Locator toast = formPage.getToastMessages().locator(".p-toast-message");
    toast.waitFor();
    assertTrue(toast.innerText().contains("Author Created"));
  }

  @Test
  @Order(5)
  @Tag("regression")
  @Story("Form Navigation")
  void shouldCancelAuthorForm() {
    formPage.getCancelButton().click();

    Locator dialogMask = page.locator(".p-dialog-mask");
    dialogMask.waitFor();

    dialogMask.locator("button.p-confirmdialog-accept-button").click();

    assertTrue(page.url().contains("/authors"));
  }
}
