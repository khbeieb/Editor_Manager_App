package com.project.api.cucumber.steps;

import com.microsoft.playwright.APIRequestContext;
import com.microsoft.playwright.APIResponse;
import com.project.api.cucumber.fixtures.PlaywrightCucumberFixtures;
import com.project.api.services.AuthorService;
import com.project.api.services.BookService;
import com.project.api.utils.HttpStatus;
import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.cucumber.java.en.*;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class AuthorSteps {

  private APIRequestContext api() {
    return PlaywrightCucumberFixtures.getApi();
  }

  private AuthorService authorService;
  private BookService bookService;

  private Integer lastAuthorId;
  private Integer lastBookId;
  private APIResponse lastResponse;

  private final List<Integer> createdAuthorIds = new ArrayList<>();
  private final List<Integer> createdBookIds = new ArrayList<>();

  @Before
  public void setup() {
    authorService = new AuthorService(api());
    bookService = new BookService(api());
  }

  @Given("a test author exists with name {string}")
  public void aTestAuthorExistsWithName(String name) {
    lastAuthorId = authorService.createAuthor(name, "1990-01-01", "Unknown");
    createdAuthorIds.add(lastAuthorId);
  }

  @Given("a test author exists with a book")
  public void aTestAuthorExistsWithABook() {
    lastAuthorId = authorService.createAuthor("Author With Book", "1990-01-01", "Unknown");
    lastBookId = bookService.createBook("Sample Book", "isbn-" + System.nanoTime(), lastAuthorId);
    createdAuthorIds.add(lastAuthorId);
    createdBookIds.add(lastBookId);
  }

  @When("I create a new author with the name {string} and nationality {string}")
  public void iCreateANewAuthor(String name, String nationality) {
    lastAuthorId = authorService.createAuthor(name, "1990-01-01", nationality);
    createdAuthorIds.add(lastAuthorId);
  }

  @When("I create a new author along with books")
  public void iCreateANewAuthorWithBooks() {
    lastAuthorId = authorService.createAuthor("Author With Books", "1990-01-01", "Unknown");
    lastBookId = bookService.createBook("Book 1", "isbn-" + System.nanoTime(), lastAuthorId);
    createdBookIds.add(lastBookId);
    lastBookId = bookService.createBook("Book 2", "isbn-" + System.nanoTime(), lastAuthorId);
    createdBookIds.add(lastBookId);
    createdAuthorIds.add(lastAuthorId);
  }

  @Then("the author should be created successfully")
  public void theAuthorShouldBeCreatedSuccessfully() {
    assertNotNull(lastAuthorId, "Author ID should not be null after creation");
  }

  @Then("the author should have 2 books")
  public void theAuthorShouldHave2Books() {
    long count = createdBookIds.stream()
      .filter(id -> id != null)
      .count();
    assertEquals(2, count, "Author should have 2 books");
  }

  @When("I list authors")
  public void iListAuthors() {
    lastResponse = authorService.getAllAuthors();
  }

  @Then("the new author should appear in the list")
  public void theNewAuthorShouldAppearInTheList() {
    boolean exists = authorService.isAuthorInList(lastAuthorId);
    assertTrue(exists, "Author should appear in the list");
  }

  @When("I delete the test author")
  public void iDeleteTheTestAuthor() {
    lastResponse = authorService.deleteAuthor(lastAuthorId);
    createdAuthorIds.remove(lastAuthorId);
  }

  @Then("the author should not appear in the list")
  public void theAuthorShouldNotAppearInTheList() {
    boolean exists = authorService.isAuthorInList(lastAuthorId);
    assertFalse(exists, "Author should not appear in the list");
  }

  @When("I create an invalid author entry")
  public void iCreateAnInvalidAuthorEntry() {
    lastResponse = authorService.createInvalidAuthor();
  }

  @Then("the API should return a client error")
  public void theAPIShouldReturnAClientError() {
    System.out.println("-----> lastResponse: " + lastResponse.status() + " " + lastResponse.text());
    assertEquals(HttpStatus.BAD_REQUEST, lastResponse.status(), "Expected 400 error for invalid author data");
  }

  @When("I create another author with the same name")
  public void iCreateAnotherAuthorWithTheSameName() {
    lastResponse = authorService.createDuplicateAuthor();
  }

  @Then("the API should return a 500 error")
  public void theAPIShouldReturnA500Error() {
    assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, lastResponse.status(), "Expected 500 error");
  }

  @When("I create a test author with duplicate book ISBNs")
  public void iCreateTestAuthorWithDuplicateISBNs() {
    lastResponse = authorService.createAuthorWithDuplicateISBNs(createdAuthorIds, createdBookIds);
  }

  @After
  public void cleanup() {
    // Delete books first
    createdBookIds.forEach(id -> {
      try { bookService.deleteBook(id); } catch (Exception ignored) {}
    });
    createdBookIds.clear();

    // Then delete authors
    createdAuthorIds.forEach(id -> {
      try { authorService.deleteAuthor(id); } catch (Exception ignored) {}
    });
    createdAuthorIds.clear();
  }
}
