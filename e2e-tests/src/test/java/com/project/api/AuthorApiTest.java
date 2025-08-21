package com.project.api;

import com.microsoft.playwright.APIResponse;
import com.microsoft.playwright.options.RequestOptions;
import com.project.api.base.BaseEntityTest;
import io.qameta.allure.*;
import org.everit.json.schema.Schema;
import org.everit.json.schema.loader.SchemaLoader;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.jupiter.api.*;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@Epic("Authors API")
public class AuthorApiTest extends BaseEntityTest {

  private static Schema AUTHOR_SCHEMA;

  @BeforeAll
  static void loadSchemas() throws Exception {
    JSONObject rawSchema = new JSONObject(
      Files.readString(Path.of("src/test/resources/schemas/author-schema.json"))
    );
    AUTHOR_SCHEMA = SchemaLoader.load(rawSchema);
  }

  @Test
  @Order(1)
  @Feature("Author Creation")
  @Story("Create Simple Author")
  @Severity(SeverityLevel.CRITICAL)
  @Description("Creates an author with no books")
  void shouldCreateAuthor() {
    JSONObject authorData = new JSONObject()
      .put("name", "Author " + System.currentTimeMillis())
      .put("birthDate", "1970-05-05")
      .put("nationality", "Testland");

    JSONObject createdAuthor = createEntity("/authors", authorData, AUTHOR_SCHEMA);

    assertEquals(authorData.getString("name"), createdAuthor.getString("name"));
    assertEquals(authorData.getString("nationality"), createdAuthor.getString("nationality"));

    System.out.println("✅ Created author ID=" + createdAuthor.getLong("id"));
  }

  @Test
  @Order(2)
  @Feature("Author Creation")
  @Story("Create Author with Books")
  @Severity(SeverityLevel.CRITICAL)
  @Description("Creates an author along with multiple books")
  void shouldCreateAuthorWithBooks() {
    String isbn1 = generateValidIsbn();
    String isbn2 = generateValidIsbn();

    JSONObject authorData = new JSONObject()
      .put("name", "Cascade Author " + System.currentTimeMillis())
      .put("birthDate", "1980-10-10")
      .put("nationality", "Cascadeland")
      .put("books", new JSONArray()
        .put(new JSONObject()
          .put("title", "Cascade Book 1")
          .put("isbn", isbn1)
          .put("publicationDate", "2025-01-01"))
        .put(new JSONObject()
          .put("title", "Cascade Book 2")
          .put("isbn", isbn2)
          .put("publicationDate", "2025-02-01"))
      );

    JSONObject createdAuthor = createEntity("/authors", authorData, AUTHOR_SCHEMA);

    JSONArray books = createdAuthor.getJSONArray("books");
    assertEquals(2, books.length(), "Author should have 2 books");

    boolean foundIsbn1 = false;
    for (int i = 0; i < books.length(); i++) {
      JSONObject book = books.getJSONObject(i);
      if (book.getString("isbn").equals(isbn1)) {
        foundIsbn1 = true;
        break;
      }
    }
    assertTrue(foundIsbn1, "First book with ISBN " + isbn1 + " must exist");

    boolean foundIsbn2 = false;
    for (int i = 0; i < books.length(); i++) {
      JSONObject book = books.getJSONObject(i);
      if (book.getString("isbn").equals(isbn2)) {
        foundIsbn2 = true;
        break;
      }
    }
    assertTrue(foundIsbn2, "Second book with ISBN " + isbn2 + " must exist");

    System.out.println("✅ Created author with books. Author ID=" + createdAuthor.getLong("id"));
  }

  @Test
  @Order(3)
  @Feature("Author Retrieval")
  @Story("List Authors")
  @Severity(SeverityLevel.NORMAL)
  @Description("Checks that a created author appears in GET /authors")
  void shouldListAuthors() {
    JSONObject authorData = new JSONObject()
      .put("name", "List Author " + System.currentTimeMillis())
      .put("birthDate", "1985-02-10")
      .put("nationality", "Listland");

    JSONObject createdAuthor = createEntity("/authors", authorData, AUTHOR_SCHEMA);
    Long id = createdAuthor.getLong("id");

    JSONArray authors = getAllEntities("/authors");
    System.out.println("📋 Authors list response (" + authors.length() + " total):");
    for (int i = 0; i < authors.length(); i++) {
      System.out.println("   → " + authors.getJSONObject(i).toString(2));
    }

    JSONObject found = findEntityInList(authors, "id", id);

    if (found == null) {
      System.err.println("❌ Author with ID=" + id + " was not found in GET /authors");
    }

    assertNotNull(found, "Created author must appear in GET /authors");
    assertEquals(id, found.getLong("id"));

    System.out.println("✅ Found author in list ID=" + id);
  }

  @Test
  @Order(4)
  @Feature("Author Management")
  @Story("Delete Author with Books")
  @Severity(SeverityLevel.CRITICAL)
  @Description("Deletes an author and all their books")
  void shouldDeleteAuthor() {
    String isbn = generateValidIsbn();

    JSONObject authorData = new JSONObject()
      .put("name", "Delete Author " + System.currentTimeMillis())
      .put("birthDate", "1999-09-09")
      .put("nationality", "Deleteland")
      .put("books", new JSONArray()
        .put(new JSONObject()
          .put("title", "Book To Delete")
          .put("isbn", isbn)
          .put("publicationDate", "2025-01-01"))
      );

    JSONObject createdAuthor = createEntity("/authors", authorData, AUTHOR_SCHEMA);
    Long authorId = createdAuthor.getLong("id");

    // Delete author
    deleteEntity("/authors", authorId);

    // Verify deletion
    JSONArray authors = getAllEntities("/authors");
    assertFalse(entityExistsInList(authors, "id", authorId), "Author should not exist after deletion");

    createdEntities.removeIf(entity -> entity.id().equals(authorId)); // avoid double cleanup
    System.out.println("✅ Deleted author with ID=" + authorId + " and their books");
  }

  @Test
  @Order(5)
  @Feature("Author Validation")
  @Story("Invalid Author Data")
  @Severity(SeverityLevel.NORMAL)
  @Description("Verifies validation errors for invalid author input")
  void shouldRejectInvalidAuthorData() {
    // Missing required name
    JSONObject invalidAuthor = new JSONObject()
      .put("birthDate", "2000-01-01")
      .put("nationality", "Nowhere");

    APIResponse response = api.post("/authors",
      RequestOptions.create()
        .setData(invalidAuthor.toString())
    );

    assertTrue(response.status() >= 400 && response.status() < 500,
      "Invalid author must return 4xx, got: " + response.status());

    System.out.println("✅ Invalid author data rejected with status: " + response.status());
  }

  @Test
  @Order(6)
  @Feature("Author Validation")
  @Story("Duplicate Author Name")
  @Severity(SeverityLevel.NORMAL)
  @Description("Verifies duplicate author names are rejected")
  void shouldRejectDuplicateAuthorName() {
    String duplicateName = "Dup Author " + System.currentTimeMillis();

    JSONObject authorData = new JSONObject()
      .put("name", duplicateName)
      .put("birthDate", "1977-07-07")
      .put("nationality", "DupLand");

    createEntity("/authors", authorData, AUTHOR_SCHEMA);

    APIResponse response = api.post("/authors",
      RequestOptions.create()
        .setData(authorData.toString())
    );

    assertEquals(500, response.status(),
      "Duplicate author name must return 500, got: " + response.status());

    System.out.println("✅ Duplicate author name properly rejected with status: " + response.status());
  }

  @Test
  @Order(7)
  @Feature("Author Validation")
  @Story("Duplicate Book ISBN in Author Cascade")
  @Severity(SeverityLevel.NORMAL)
  @Description("Verifies author creation fails if books contain duplicate ISBNs")
  void shouldRejectAuthorWithDuplicateBookIsbn() {
    String duplicateIsbn = generateValidIsbn();

    JSONObject authorData = new JSONObject()
      .put("name", "Bad Cascade Author " + System.currentTimeMillis())
      .put("birthDate", "1980-08-08")
      .put("nationality", "DupISBNland")
      .put("books", new JSONArray()
        .put(new JSONObject()
          .put("title", "Book1")
          .put("isbn", duplicateIsbn)
          .put("publicationDate", "2025-01-01"))
        .put(new JSONObject()
          .put("title", "Book2")
          .put("isbn", duplicateIsbn) // duplicate
          .put("publicationDate", "2025-01-02"))
      );

    System.out.println("📌 Author request payload:\n" + authorData.toString(2));

    APIResponse response = api.post("/authors",
      RequestOptions.create()
        .setData(authorData.toString())
    );

    System.out.println("📋 API response status: " + response.status());
    System.out.println("📋 API response body:\n" + response.text());

    assertEquals(500, response.status(),
      "Duplicate ISBNs in author cascade must return 500, got: " + response.status());
  }

  @AfterAll
  static void cleanUpAll() {
    cleanupCreatedEntities();
    System.out.println("🧹 Cleaned up all test-created authors & books");
  }
}
