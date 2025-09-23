package com.project.api.cucumber.steps;

import com.microsoft.playwright.APIRequestContext;
import com.microsoft.playwright.APIResponse;
import com.project.api.cucumber.fixtures.PlaywrightCucumberFixtures;
import com.project.api.services.BookService;
import com.project.api.services.AuthorService;
import com.project.api.utils.HttpStatus;
import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.cucumber.java.en.*;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class BookSteps {

  private APIRequestContext api() {
    return PlaywrightCucumberFixtures.getApi();
  }

  private BookService bookService;
  private AuthorService authorService;

  private Integer lastAuthorId;
  private Integer lastBookId;
  private String lastBookIsbn;
  private APIResponse lastResponse;

  private final List<Integer> createdBookIds = new ArrayList<>();
  private final List<Integer> createdAuthorIds = new ArrayList<>();

  @Before
  public void setup() {
    bookService = new BookService(api());
    authorService = new AuthorService(api());
  }

  @Given("an author exists with name {string}")
  public void an_author_exists_with_name(String authorName) {
    lastAuthorId = authorService.createAuthor(authorName, "1990-01-01", "Unknown");
    createdAuthorIds.add(lastAuthorId);
  }

  @When("I create a book titled {string}")
  public void i_create_a_book_titled(String title) {
    lastBookIsbn = "isbn-" + System.nanoTime();
    lastBookId = bookService.createBook(title, lastBookIsbn, lastAuthorId);
    createdBookIds.add(lastBookId);
  }

  @Then("the book should be created successfully")
  public void the_book_should_be_created_successfully() {
    assertNotNull(lastBookId, "Book ID should not be null after creation");
  }

  @Given("a book already exists with ISBN {string}")
  public void a_book_already_exists_with_isbn(String isbn) {
    lastBookId = bookService.createBook("Existing Book", isbn, lastAuthorId);
    lastBookIsbn = isbn;
    createdBookIds.add(lastBookId);
  }

  @When("I try to create another book with ISBN {string}")
  public void i_try_to_create_another_book_with_isbn(String isbn) {
    lastResponse = bookService.createDuplicateBook(isbn, lastAuthorId);
  }

  @Then("the API should reject the request")
  public void the_api_should_reject_the_request() {
    assertTrue(lastResponse.status() >= 400, "Expected client or server error status code");
  }

  @When("I get all books")
  public void i_get_all_books() {
    lastResponse = bookService.getAllBooks();
  }

  @Then("the response should contain a list of books")
  public void the_response_should_contain_a_list_of_books() {
    JSONObject json = new JSONObject(lastResponse.text());
    assertTrue(json.has("data") && !json.getJSONArray("data").isEmpty(), "Books list should not be empty");
  }

  @When("I get the book by its ISBN")
  public void i_get_the_book_by_its_isbn() {
    lastResponse = bookService.getBookByIsbn(lastBookIsbn);
    assertEquals(200, lastResponse.status(), "Failed to retrieve book by ISBN");
  }

  @Then("the correct book details should be returned")
  public void the_correct_book_details_should_be_returned() {
    JSONObject book = new JSONObject(lastResponse.text()).getJSONObject("data");
    assertEquals(lastBookId.intValue(), book.getInt("id"));
    assertEquals(lastBookIsbn, book.getString("isbn"));
    assertNotNull(book.getJSONObject("author"));
  }

  @When("I delete the book")
  public void i_delete_the_book() {
    lastResponse = bookService.deleteBook(lastBookId);
    assertEquals(HttpStatus.OK, lastResponse.status(), "Book deleted successfully");
  }

  @Then("the book should not exist anymore")
  public void the_book_should_not_exist_anymore() {
    lastResponse = bookService.getBookByIsbn(lastBookIsbn);
    assertEquals(HttpStatus.NOT_FOUND, lastResponse.status(), "Book still exists after deletion");
    createdBookIds.remove(lastBookId);
  }

  @After
  public void cleanup() {
    createdBookIds.forEach(id -> {
      try { bookService.deleteBook(id); } catch (Exception ignored) {}
    });
    createdBookIds.clear();

    createdAuthorIds.forEach(id -> {
      try { authorService.deleteAuthor(id); } catch (Exception ignored) {}
    });
    createdAuthorIds.clear();
  }
}
