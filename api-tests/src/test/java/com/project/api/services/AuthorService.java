package com.project.api.services;

import com.microsoft.playwright.APIRequestContext;
import com.microsoft.playwright.APIResponse;
import com.microsoft.playwright.options.RequestOptions;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class AuthorService {

  private final APIRequestContext api;
  private final BookService bookService;

  public AuthorService(APIRequestContext api) {
    this.api = api;
    this.bookService = new BookService(api);
  }

  /**
   * Create a new author and return its ID (happy path)
   */
  public int createAuthor(String name, String birthDate, String nationality) {
    APIResponse resp = createAuthorRaw(name, birthDate, nationality);
    if (resp.status() != 201) {
      throw new RuntimeException("Failed to create author: " + resp.text());
    }
    JSONObject json = new JSONObject(resp.text());
    return json.getJSONObject("data").getInt("id");
  }

  /**
   * Create a new author and return the raw APIResponse (for negative tests)
   */
  public APIResponse createAuthorRaw(String name, String birthDate, String nationality) {
    JSONObject authorData = new JSONObject()
      .put("name", name)
      .put("birthDate", birthDate)
      .put("nationality", nationality);

    return api.post("/authors",
      RequestOptions.create()
        .setHeader("Content-Type", "application/json")
        .setData(authorData.toString())
    );
  }

  /**
   * Add a book to an existing author
   */
  public void addBookToAuthor(int authorId, String title, String isbn) {
    bookService.createBook(title, isbn, authorId);
  }

  /**
   * Fetch all authors
   */
  public APIResponse getAllAuthors() {
    return api.get("/authors");
  }

  /**
   * Fetch all books for a given author
   */
  public List<JSONObject> getBooksByAuthor(int authorId) {
    APIResponse response = getAllAuthors();
    JSONObject json = new JSONObject(response.text());

    if (!json.has("data") || json.isNull("data")) return List.of();

    JSONArray authors = json.getJSONArray("data");

    for (int i = 0; i < authors.length(); i++) {
      JSONObject author = authors.getJSONObject(i);
      if (author.getInt("id") == authorId) {
        if (!author.has("books") || author.isNull("books")) return List.of();
        JSONArray books = author.getJSONArray("books");
        return IntStream.range(0, books.length())
          .mapToObj(books::getJSONObject)
          .collect(Collectors.toList());
      }
    }
    return List.of();
  }

  /**
   * Check if an author exists
   */
  public boolean isAuthorInList(int authorId) {
    APIResponse response = getAllAuthors();
    JSONObject json = new JSONObject(response.text());

    if (!json.has("data") || json.isNull("data")) return false;

    JSONArray data = json.getJSONArray("data");
    return IntStream.range(0, data.length())
      .mapToObj(data::getJSONObject)
      .anyMatch(obj -> obj.getInt("id") == authorId);
  }

  /**
   * Delete an author
   */
  public APIResponse deleteAuthor(int authorId) {
    return api.delete("/authors/" + authorId);
  }

  /**
   * Create an invalid author entry (for testing 4xx/5xx response)
   */
  public APIResponse createInvalidAuthor() {
    JSONObject invalidAuthor = new JSONObject()
      .put("birthDate", "1990-01-01")
      .put("nationality", "Unknown");
    return api.post("/authors",
      RequestOptions.create()
        .setHeader("Content-Type", "application/json")
        .setData(invalidAuthor.toString())
    );
  }

  /**
   * Create a duplicate author (for testing 500 response)
   */
  public APIResponse createDuplicateAuthor() {
    String name = "Dup Author";

    // First author (ignore exception)
    try {
      createAuthor(name, "1990-01-01", "Unknown");
    } catch (Exception ignored) {}

    // Second author → expect duplicate
    return createAuthorRaw(name, "1990-01-01", "Unknown");
  }

  /**
   * Create an author and attempt to add books with duplicate ISBNs
   */
  public APIResponse createAuthorWithDuplicateISBNs(List<Integer> createdAuthorIds, List<Integer> createdBookIds) {
    int authorId = createAuthor("Author Duplicate ISBNs", "1990-01-01", "Unknown");
    createdAuthorIds.add(authorId); // track author

    String isbn = "isbn-" + System.nanoTime();

    // Add first book
    int firstBookId = bookService.createBook("Book 1", isbn, authorId);
    createdBookIds.add(firstBookId); // track book

    // Attempt duplicate ISBN (won't create a new book)
    JSONObject duplicateBook = new JSONObject()
      .put("title", "Book 2")
      .put("isbn", isbn);

    return api.post("/authors/" + authorId + "/books",
      RequestOptions.create()
        .setHeader("Content-Type", "application/json")
        .setData(duplicateBook.toString())
    );
  }
}
