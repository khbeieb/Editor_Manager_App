package com.project.api;

import com.microsoft.playwright.APIResponse;
import com.project.base.BaseApiTest;
import io.qameta.allure.*;
import org.everit.json.schema.Schema;
import org.everit.json.schema.loader.SchemaLoader;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class BookApiTest extends BaseApiTest {

  private static Schema BOOK_SCHEMA;
  private static Schema BOOK_ARRAY_SCHEMA;

  @BeforeAll
  static void loadSchemas() throws Exception {
    // Load single book schema
    JSONObject rawBookSchema = new JSONObject(
      Files.readString(Path.of("src/test/resources/schemas/book-schema.json"))
    );
    BOOK_SCHEMA = SchemaLoader.load(rawBookSchema);

    // Define array schema based on single book schema
    JSONObject arraySchemaJson = new JSONObject()
      .put("type", "array")
      .put("items", rawBookSchema);
    BOOK_ARRAY_SCHEMA = SchemaLoader.load(arraySchemaJson);
  }

  @Test
  @Epic("Books API")
  @Feature("Retrieve Books")
  @Story("Get all books from API")
  @Severity(SeverityLevel.CRITICAL)
  @Description("Verifies that GET /books returns all books and matches the schema")
  void shouldReturnAllBooks() {
    APIResponse response = api.get("/books");
    assertEquals(200, response.status(), "Expected status 200");

    // Parse top-level ApiResponse
    JSONObject apiResponse = new JSONObject(response.text());
    JSONArray books = apiResponse.getJSONArray("data");

    // Validate each book individually
    for (int i = 0; i < books.length(); i++) {
      BOOK_SCHEMA.validate(books.getJSONObject(i));
    }

    System.out.println("✅ All books passed schema validation. Total books: " + books.length());
  }

  @Test
  void shouldCreateBook() {
    String isbn = "isbn-" + System.currentTimeMillis();
    JSONObject newBook = new JSONObject()
      .put("title", "My Test Book")
      .put("isbn", isbn)
      .put("publicationDate", "2025-07-29")
      .put("author", new JSONObject().put("id", 3));

    APIResponse response = api.post("/books",
      com.microsoft.playwright.options.RequestOptions.create()
        .setData(newBook.toString())
        .setHeader("Content-Type", "application/json")
    );

    assertEquals(201, response.status(), "Expected status 201");

    // The backend wraps the created book in ApiResponse
    JSONObject apiResp = new JSONObject(response.text());
    JSONObject createdBook = apiResp.getJSONObject("data");

    BOOK_SCHEMA.validate(createdBook);

    System.out.println("✅ Book created and validated: " + createdBook.getString("title"));
  }
}
