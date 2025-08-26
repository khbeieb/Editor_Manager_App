package com.project.ui.pages;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.WaitForSelectorState;

public class AuthorsListPage {
  private final Page page;

  // Locators
  private final Locator title;
  private final Locator addAuthorButton;
  private final Locator refreshButton;
  private final Locator searchInput;
  private final Locator nationalityFilter;
  private final Locator sortByFilter;
  private final Locator sortOrderFilter;
  private final Locator authorsTable;
  private final Locator emptyMessage;
  private final Locator loadingSpinner;
  private final Locator errorMessage;

  public AuthorsListPage(Page page) {
    this.page = page;

    this.title = page.locator("[data-testid='authors-title']");
    this.addAuthorButton = page.locator("[data-testid='add-author-button']");
    this.refreshButton = page.locator("[data-testid='refresh-authors-button']");
    this.searchInput = page.locator("[data-testid='search-input']");
    this.nationalityFilter = page.locator("[data-testid='nationality-filter']");
    this.sortByFilter = page.locator("[data-testid='sort-by-filter']");
    this.sortOrderFilter = page.locator("[data-testid='sort-order-filter']");
    this.authorsTable = page.locator("[data-testid='authors-table']");
    this.emptyMessage = page.locator("[data-testid='empty-message']");
    this.loadingSpinner = page.locator("[data-testid='loading-spinner']");
    this.errorMessage = page.locator("[data-testid='error-message']");
  }

  // --- Actions ---
  public String getTitle() {
    return title.innerText();
  }

  public Locator getLoadingSpinner() {
    return this.loadingSpinner;
  }

  public void waitForTable() {
    page.waitForSelector("[data-testid='authors-table']");
  }

  /**
   * Wait until the loading spinner disappears from the DOM.
   */
  public void waitForLoadingToFinish() {
    page.waitForSelector("[data-testid='loading-spinner']",
      new Page.WaitForSelectorOptions().setState(WaitForSelectorState.DETACHED));
  }

  /**
   * Wait until either the authors table or the empty message is rendered.
   */
  public void waitForTableOrEmpty() {
    page.waitForSelector("[data-testid='authors-table'], [data-testid='empty-message']",
      new Page.WaitForSelectorOptions().setState(WaitForSelectorState.ATTACHED));
  }

  public void waitForAuthorsData() {
    // Wait until either table, empty message, or error is attached and visible
    page.waitForSelector("[data-testid='authors-table'], [data-testid='empty-message'], [data-testid='error-message']",
      new Page.WaitForSelectorOptions().setState(WaitForSelectorState.VISIBLE));
  }

  public String getFirstRowName() {
    return page.locator("[data-testid^='author-name-']").first().innerText();
  }

  public String getFirstRowNationality() {
    return page.locator("[data-testid^='author-nationality-']").first().innerText();
  }

  public void clickAddAuthor() {
    addAuthorButton.click();
  }

  public void clickRefresh() {
    refreshButton.click();
  }

  public void searchAuthor(String name) {
    searchInput.fill(name);
  }

  /**
   * Select a nationality from the dropdown
   */
  public void selectNationality(String nationality) {
    nationalityFilter.click(); // open dropdown
    page.locator(".p-dropdown-item")
      .filter(new Locator.FilterOptions().setHasText(nationality))
      .click(); // select option
  }

  /**
   * Select a sort option from the dropdown
   */
  public void selectSortBy(String sortBy) {
    sortByFilter.click();
    page.locator(".p-dropdown-item")
      .filter(new Locator.FilterOptions().setHasText(sortBy))
      .click();
  }

  /**
   * Select sort order (ascending/descending)
   */
  public void selectSortOrder(String order) {
    sortOrderFilter.click();
    page.locator(".p-dropdown-item")
      .filter(new Locator.FilterOptions().setHasText(order))
      .click();
  }

  public boolean isTableVisible() {
    return authorsTable.isVisible();
  }

  public boolean isEmptyMessageVisible() {
    return emptyMessage.isVisible();
  }

  public boolean isLoadingVisible() {
    return loadingSpinner.isVisible();
  }

  public boolean isErrorVisible() {
    return errorMessage.isVisible();
  }
}
