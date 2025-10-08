package org.mobelite.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.util.List;

public class AuthorsListPage {

  private final WebDriver driver;

  private final By title = By.cssSelector("[data-testid='authors-title']");
  private final By addAuthorButton = By.cssSelector("[data-testid='add-author-button']");
  private final By refreshButton = By.cssSelector("[data-testid='refresh-authors-button']");
  private final By searchInput = By.cssSelector("[data-testid='search-input']");
  private final By nationalityFilter = By.cssSelector("[data-testid='nationality-filter']");
  private final By sortByFilter = By.cssSelector("[data-testid='sort-by-filter']");
  private final By sortOrderFilter = By.cssSelector("[data-testid='sort-order-filter']");
  private final By authorsTable = By.cssSelector("[data-testid='authors-table']");
  private final By emptyMessage = By.cssSelector("[data-testid='empty-message']");
  private final By errorMessage = By.cssSelector("[data-testid='error-message']");

  public AuthorsListPage(WebDriver driver) {
    this.driver = driver;
  }

  public String getTitle() {
    return driver.findElement(title).getText();
  }

  public boolean isEmptyMessageVisible() {
    return !driver.findElements(emptyMessage).isEmpty() && driver.findElement(emptyMessage).isDisplayed();
  }

  public boolean isErrorVisible() {
    return !driver.findElements(errorMessage).isEmpty() && driver.findElement(errorMessage).isDisplayed();
  }

  public boolean isTableVisible() {
    return !driver.findElements(authorsTable).isEmpty() && driver.findElement(authorsTable).isDisplayed();
  }

  public void clickRefresh() {
    driver.findElement(refreshButton).click();
  }

  public void searchAuthor(String name) {
    WebElement input = driver.findElement(searchInput);
    input.clear();
    input.sendKeys(name);
  }

  public String getFirstRowName() {
    List<WebElement> names = driver.findElements(By.cssSelector("[data-testid^='author-name-']"));
    return names.isEmpty() ? "" : names.get(0).getText();
  }

  public String getFirstRowNationality() {
    List<WebElement> nationalities = driver.findElements(By.cssSelector("[data-testid^='author-nationality-']"));
    return nationalities.isEmpty() ? "" : nationalities.get(0).getText();
  }

  public void selectSortBy(String sortBy) {
    driver.findElement(sortByFilter).click();
    driver.findElement(By.xpath("//span[contains(text(),'" + sortBy + "')]")).click();
  }

  public void selectSortOrder(String order) {
    driver.findElement(sortOrderFilter).click();
    driver.findElement(By.xpath("//span[contains(text(),'" + order + "')]")).click();
  }
}
