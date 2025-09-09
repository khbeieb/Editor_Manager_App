package com.project.ui.pages;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.WaitForSelectorState;

public class AuthorFormPage {
  private final Page page;

  // --- Author Fields ---
  private final Locator nameInput;
  private final Locator nationalityInput;
  private final Locator birthDateInput;
  private final Locator saveAuthorButton;
  private final Locator cancelButton;

  // --- Book Fields ---
  private final Locator bookTitleInput;
  private final Locator bookIsbnInput;
  private final Locator bookPublicationDatePicker;
  private final Locator addBookButton;

  // --- Toast and Errors ---
  private final Locator toastMessages;
  private final Locator nameError;
  private final Locator nationalityError;

  public AuthorFormPage(Page page) {
    this.page = page;

    this.nameInput = page.locator("[data-testid='author-name-input']");
    this.nationalityInput = page.locator("[data-testid='author-nationality-input']");
    this.birthDateInput = page.locator("[data-testid='author-birthdate-picker'] input");
    this.saveAuthorButton = page.locator("[data-testid='save-author-button']");
    this.cancelButton = page.locator("[data-testid='cancel-button']");

    this.bookTitleInput = page.locator("[data-testid='book-title-input']");
    this.bookIsbnInput = page.locator("[data-testid='book-isbn-input']");
    this.bookPublicationDatePicker = page.locator("[data-testid='book-publication-date-picker'] input");
    this.addBookButton = page.locator("[data-testid='add-book-button']");

    this.toastMessages = page.locator("[data-testid='toast-messages']");
    this.nameError = page.locator("[data-testid='name-error']");
    this.nationalityError = page.locator("[data-testid='nationality-error']");
  }

  // --- Utility wrappers ---
  private void waitForVisible(Locator locator) {
    locator.waitFor(new Locator.WaitForOptions()
      .setState(WaitForSelectorState.VISIBLE)
      .setTimeout(5000));
  }

  private void clickWhenReady(Locator locator) {
    waitForVisible(locator);
    locator.click();
  }

  private void fillWhenReady(Locator locator, String value) {
    waitForVisible(locator);
    locator.fill(value);
  }

  // --- Public API for tests ---
  public Locator getNameInput() { return nameInput; }
  public Locator getNationalityInput() { return nationalityInput; }
  public Locator getSaveAuthorButton() { return saveAuthorButton; }
  public Locator getCancelButton() { return cancelButton; }
  public Locator getBookTitleInput() { return bookTitleInput; }
  public Locator getBookIsbnInput() { return bookIsbnInput; }
  public Locator getAddBookButton() { return addBookButton; }
  public Locator getToastMessages() { return toastMessages; }
  public Locator getNameError() { return nameError; }
  public Locator getNationalityError() { return nationalityError; }

  public Locator getBookRow(int index) {
    return page.locator("[data-testid='book-row-" + index + "']");
  }

  public void clickRemoveBook(int index) {
    clickWhenReady(page.locator("[data-testid='remove-book-button-" + index + "']"));
  }

  public void waitForForm() {
    page.waitForSelector("[data-testid='author-form']",
      new Page.WaitForSelectorOptions().setState(WaitForSelectorState.VISIBLE));
  }

  public void addBook(String title, String isbn, String publicationDate) {
    fillWhenReady(bookTitleInput, title);
    fillWhenReady(bookIsbnInput, isbn);

    clickWhenReady(bookPublicationDatePicker);

    // Wait for calendar
    Locator panel = page.locator(".p-datepicker-panel");
    waitForVisible(panel);

    // Pick any non-disabled day (skip "today" if needed)
    Locator day = panel.locator("td span.p-datepicker-day:not(.p-disabled)").nth(1);
    if (day.count() == 0) {
      throw new RuntimeException("No selectable date found in datepicker");
    }
    day.click();

    clickWhenReady(addBookButton);

    // Wait for first book row
    waitForVisible(getBookRow(0));
  }

  public void setBirthDate() {
    clickWhenReady(birthDateInput);

    Locator panel = page.locator(".p-datepicker-panel");
    waitForVisible(panel);

    Locator day = panel.locator("td span.p-datepicker-day:not(.p-disabled)").nth(1);
    if (day.count() == 0) {
      throw new RuntimeException("No selectable birthdate available in datepicker");
    }
    day.click();
  }
}
