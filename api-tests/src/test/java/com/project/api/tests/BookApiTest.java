package com.project.api.tests;

import com.microsoft.playwright.APIResponse;
import com.microsoft.playwright.options.RequestOptions;
import com.project.api.base.BaseEntityTest;
import org.everit.json.schema.Schema;
import io.qameta.allure.*;
import org.everit.json.schema.loader.SchemaLoader;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.jupiter.api.*; // Import required for @Tag

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@Epic("Book API Integration Tests")
public class BookApiTest extends BaseEntityTest {

  private static Schema BOOK_SCHEMA;

  @BeforeAll
  static void loadSchemas() throws Exception {
    JSONObject rawBookSchema = new JSONObject(
      Files.readString(Path.of("src/test/resources/schemas/book-schema.json"))
    );
    BOOK_SCHEMA = SchemaLoader.load(rawBookSchema);
  }

  @AfterAll
  static void cleanUpAll() {
    // Global cleanup (BaseEntityTest tracks createdEntities)
    cleanupCreatedEntities();
    System.out.println("🧹 All test-created entities cleaned up.");
  }

  @Test
  @Order(1)
  @Feature("Book Creation")
  @Story("Create Book with Author")
  @Severity(SeverityLevel.CRITICAL)
  @Description("Creates an author and then creates a book linked to that author")
  @Tag("smoke") // Critical, basic flow check
  @Tag("integration")
  void shouldCreateBookWithAuthor() {
    JSONObject authorData = new JSONObject()
      .put("name", "Test Author " + System.currentTimeMillis())
      .put("birthDate", "1980-01-01")
      .put("nationality", "Testland");

    JSONObject createdAuthor = createEntity("/authors", authorData, null);
    Long authorId = createdAuthor.getLong("id");

    String isbn = generateValidIsbn();
    String title = "Test Book " + System.currentTimeMillis();
    JSONObject bookData = new JSONObject()
      .put("title", title)
      .put("isbn", isbn)
      .put("publicationDate", "2025-07-29")
      .put("author", new JSONObject().put("id", authorId));

    JSONObject createdBook = createEntity("/books", bookData, BOOK_SCHEMA);

    assertEquals(isbn, createdBook.getString("isbn"));
    assertEquals(title, createdBook.getString("title"));

    System.out.println("✅ Author created (ID=" + authorId + "), Book created (ISBN=" + isbn + ")");
  }

  @Test
  @Order(2)
  @Feature("Book Retrieval")
  @Story("List All Books")
  @Severity(SeverityLevel.NORMAL)
  @Description("Retrieves all books and verifies the created book appears in the list")
  @Tag("regression") // Standard read operation
  @Tag("integration")
  void shouldListAllBooks() {
    JSONObject authorData = new JSONObject()
      .put("name", "List Test Author " + System.currentTimeMillis())
      .put("birthDate", "1975-05-15")
      .put("nationality", "Listland");

    JSONObject author = createEntity("/authors", authorData, null);

    String testIsbn = generateValidIsbn();
    JSONObject bookData = new JSONObject()
      .put("title", "List Test Book")
      .put("isbn", testIsbn)
      .put("publicationDate", "2025-01-01")
      .put("author", new JSONObject().put("id", author.getLong("id")));

    createEntity("/books", bookData, BOOK_SCHEMA);

    JSONArray books = getAllEntities("/books");
    assertFalse(books.isEmpty(), "Books list should not be empty");

    JSONObject foundBook = findEntityInList(books, "isbn", testIsbn);
    assertNotNull(foundBook, "Created book should appear in list");
    assertEquals(testIsbn, foundBook.getString("isbn"));

    System.out.println("✅ Found created book in list: " + testIsbn);
  }

  @Test
  @Order(3)
  @Feature("Book Retrieval")
  @Story("Get Book by ISBN")
  @Severity(SeverityLevel.CRITICAL)
  @Description("Retrieves a book by its ISBN")
  @Tag("smoke") // Critical lookup by unique identifier
  @Tag("integration")
  void shouldGetBookByIsbn() {
    JSONObject authorData = new JSONObject()
      .put("name", "ISBN Test Author " + System.currentTimeMillis())
      .put("birthDate", "1990-12-25")
      .put("nationality", "ISBNland");

    JSONObject author = createEntity("/authors", authorData, null);

    String testIsbn = generateValidIsbn();
    JSONObject bookData = new JSONObject()
      .put("title", "ISBN Test Book")
      .put("isbn", testIsbn)
      .put("publicationDate", "2025-03-15")
      .put("author", new JSONObject().put("id", author.getLong("id")));

    JSONObject createdBook = createEntity("/books", bookData, BOOK_SCHEMA);

    APIResponse response = api.get("/books/isbn/" + testIsbn);
    assertEquals(200, response.status(), "GET /books/isbn/" + testIsbn + " failed");

    JSONObject fetchedBook = new JSONObject(response.text()).getJSONObject("data");
    assertEquals(testIsbn, fetchedBook.getString("isbn"));
    assertEquals(createdBook.getLong("id"), fetchedBook.getLong("id"));

    System.out.println("✅ Successfully fetched book by ISBN: " + testIsbn);
  }


  @Test
  @Order(4)
  @Feature("Book Management")
  @Story("Delete Book")
  @Severity(SeverityLevel.CRITICAL)
  @Description("Deletes a book and verifies it no longer exists")
  @Tag("regression") // Critical cleanup/management flow
  @Tag("integration")
  void shouldDeleteBook() {
    JSONObject authorData = new JSONObject()
      .put("name", "Delete Test Author " + System.currentTimeMillis())
      .put("birthDate", "1970-04-20")
      .put("nationality", "Deleteland");

    JSONObject author = createEntity("/authors", authorData, null);

    String testIsbn = generateValidIsbn();
    JSONObject bookData = new JSONObject()
      .put("title", "Book to Delete")
      .put("isbn", testIsbn)
      .put("publicationDate", "2025-02-14")
      .put("author", new JSONObject().put("id", author.getLong("id")));

    JSONObject createdBook = createEntity("/books", bookData, BOOK_SCHEMA);
    Long bookId = createdBook.getLong("id");

    deleteEntity("/books", bookId);

    JSONArray remainingBooks = getAllEntities("/books");
    assertFalse(entityExistsInList(remainingBooks, "id", bookId), "Book should not exist after deletion");

    createdEntities.removeIf(entity -> entity.id().equals(bookId));

    System.out.println("✅ Book deleted successfully (ID=" + bookId + ")");
  }

  @Test
  @Order(5)
  @Feature("Book Validation")
  @Story("Invalid Book Creation")
  @Severity(SeverityLevel.NORMAL)
  @Description("Verifies proper error handling for invalid book data")
  @Tag("regression") // Validation/error handling test
  void shouldRejectInvalidBookData() {
    JSONObject invalidBook = new JSONObject()
      .put("title", "Incomplete Book");

    APIResponse response = api.post("/books",
      RequestOptions.create()
        .setData(invalidBook.toString())
    );

    assertTrue(response.status() >= 400 && response.status() < 500,
      "Invalid book data should return 4xx, got: " + response.status());

    System.out.println("✅ Invalid book data properly rejected with status: " + response.status());
  }

  @Test
  @Order(6)
  @Feature("Book Validation")
  @Story("Duplicate ISBN")
  @Severity(SeverityLevel.NORMAL)
  @Description("Verifies that duplicate ISBNs are properly handled")
  @Tag("regression") // Business logic/constraint validation
  void shouldHandleDuplicateIsbn() {
    JSONObject authorData = new JSONObject()
      .put("name", "Duplicate Test Author " + System.currentTimeMillis())
      .put("birthDate", "1988-11-30")
      .put("nationality", "Duplicateland");

    JSONObject author = createEntity("/authors", authorData, null);

    String duplicateIsbn = generateValidIsbn();
    JSONObject firstBookData = new JSONObject()
      .put("title", "First Book")
      .put("isbn", duplicateIsbn)
      .put("publicationDate", "2025-01-01")
      .put("author", new JSONObject().put("id", author.getLong("id")));

    createEntity("/books", firstBookData, BOOK_SCHEMA);

    JSONObject secondBookData = new JSONObject()
      .put("title", "Second Book")
      .put("isbn", duplicateIsbn) // same ISBN
      .put("publicationDate", "2025-02-01")
      .put("author", new JSONObject().put("id", author.getLong("id")));

    APIResponse response = api.post("/books",
      RequestOptions.create()
        .setData(secondBookData.toString())
    );

    // Expect 500
    assertEquals(500, response.status(),
      "Duplicate ISBN should return 500, got: " + response.status());

    JSONObject apiResp = new JSONObject(response.text());
    assertTrue(apiResp.getString("message").contains("already exists"),
      "Error message should indicate duplicate ISBN");

    System.out.println("✅ Duplicate ISBN properly rejected with status: " + response.status());
  }
}
