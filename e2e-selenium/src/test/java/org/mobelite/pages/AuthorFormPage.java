package org.mobelite.pages;

import org.openqa.selenium.*;
import org.openqa.selenium.io.FileHandler;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.File;
import java.io.IOException;
import java.time.Duration;
import java.util.List;

public class AuthorFormPage {

  private final WebDriver driver;
  private final WebDriverWait wait;

  // --- Author Fields ---
  private final By nameInput = By.cssSelector("[data-testid='author-name-input']");
  private final By nationalityInput = By.cssSelector("[data-testid='author-nationality-input']");
  private final By birthDateInput = By.cssSelector("[data-testid='author-birth-date-picker'] input");
  private final By saveAuthorButton = By.cssSelector("[data-testid='save-author-button']");
  private final By cancelButton = By.cssSelector("[data-testid='cancel-button']");

  // --- Book Fields ---
  private final By bookTitleInput = By.cssSelector("[data-testid='book-title-input']");
  private final By bookIsbnInput = By.cssSelector("[data-testid='book-isbn-input']");
  private final By bookPublicationDatePicker = By.cssSelector("[data-testid='book-publication-date-picker'] input");
  private final By addBookButton = By.cssSelector("[data-testid='add-book-button']");
  private final By bookRows = By.cssSelector("[data-testid^='book-row-']");

  // --- Toasts and Errors ---
  private final By toastMessages = By.cssSelector("[data-testid='toast-messages'] .p-toast-message");
  private final By nameError = By.cssSelector("[data-testid='name-error']");
  private final By nationalityError = By.cssSelector("[data-testid='nationality-error']");

  public AuthorFormPage(WebDriver driver) {
    this.driver = driver;
    this.wait = new WebDriverWait(driver, Duration.ofSeconds(10));
  }

  // --- Helpers ---
  private WebElement waitForVisible(By locator) {
    return wait.until(ExpectedConditions.visibilityOfElementLocated(locator));
  }

  private void clickWhenReady(By locator) {
    waitForVisible(locator).click();
  }

  private void fillWhenReady(By locator, String value) {
    WebElement el = waitForVisible(locator);
    el.clear();
    el.sendKeys(value);
  }

  // --- Public API ---
  public void waitForForm() {
    wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("[data-testid='author-form']")));
  }

  public void triggerEmptyValidation() {
    clickWhenReady(nameInput);
    clickWhenReady(nationalityInput);
    clickWhenReady(nameInput); // trigger blur
  }

  public boolean isNameErrorVisible() {
    return !driver.findElements(nameError).isEmpty() && driver.findElement(nameError).isDisplayed();
  }

  public boolean isNationalityErrorVisible() {
    return !driver.findElements(nationalityError).isEmpty() && driver.findElement(nationalityError).isDisplayed();
  }

  public void addBook(String title, String isbn, String publicationDate) {
    fillWhenReady(bookTitleInput, title);
    fillWhenReady(bookIsbnInput, isbn);
    clickWhenReady(bookPublicationDatePicker);

    // Wait for calendar
    By panel = By.cssSelector(".p-datepicker-panel");
    waitForVisible(panel);

    List<WebElement> days = driver.findElements(By.cssSelector(".p-datepicker-panel td span.p-datepicker-day:not(.p-disabled)"));
    if (days.isEmpty()) throw new RuntimeException("No selectable date found in datepicker");
    days.get(1).click();

    clickWhenReady(addBookButton);
    waitForVisible(By.cssSelector("[data-testid='book-row-0']"));
  }

  public void clickRemoveBook(int index) {
    clickWhenReady(By.cssSelector("[data-testid='remove-book-button-" + index + "']"));
  }

  public void confirmDialog() {
    WebElement dialogMask = driver.findElement(By.cssSelector(".p-dialog-mask"));
    dialogMask.findElement(By.cssSelector("button.p-confirmdialog-accept-button")).click();
  }

  public void removeBookAndConfirm(int index) {
    clickRemoveBook(index);
    confirmDialog();

    // Wait for book row to disappear
    new WebDriverWait(driver, Duration.ofSeconds(5))
      .until(ExpectedConditions.invisibilityOfElementLocated(
        By.cssSelector("[data-testid='book-row-" + index + "']")
      ));
  }

  public boolean isBookListEmpty() {
    return driver.findElements(By.cssSelector("[data-testid^='book-row-']")).isEmpty();
  }

  /**
   * Returns the title of the first book row in the form table.
   */
  public String getFirstBookTitle() {
    By firstBookTitle = By.cssSelector("[data-testid='book-row-0'] [data-testid='book-title-0']");
    wait.until(ExpectedConditions.visibilityOfElementLocated(firstBookTitle));
    return driver.findElement(firstBookTitle).getText().trim();
  }

  public void setBirthDate() {
    WebElement input = wait.until(ExpectedConditions.elementToBeClickable(birthDateInput));
    input.click();

    By panel = By.cssSelector(".p-datepicker-panel");
    wait.until(ExpectedConditions.visibilityOfElementLocated(panel));

    List<WebElement> days = driver.findElements(By.cssSelector(".p-datepicker-panel td span.p-datepicker-day:not(.p-disabled)"));
    if (!days.isEmpty()) days.get(1).click();
  }

  public void fillAuthorForm(String name, String nationality) {
    fillWhenReady(nameInput, name);
    fillWhenReady(nationalityInput, nationality);
    setBirthDate();
  }

  public void saveAuthor() {
    clickWhenReady(saveAuthorButton);
  }

  public boolean isAuthorCreatedToastVisible() {
    WebElement toast = waitForVisible(toastMessages);
    return toast.getText().contains("Author Created");
  }

  public void cancelForm() {
    clickWhenReady(cancelButton);
  }

  public boolean isOnAuthorsListPage() {
    return driver.getCurrentUrl().contains("/authors");
  }

  /** Save screenshot locally for debugging. */
  public void takeLocalScreenshot(String filename) throws IOException {
    File srcFile = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
    File destFile = new File(System.getProperty("user.dir") + "/screenshots/" + filename);
    destFile.getParentFile().mkdirs();
    FileHandler.copy(srcFile, destFile);
    System.out.println("📸 Screenshot saved at: " + destFile.getAbsolutePath());
  }
}
