package com.project.api.services;

import com.microsoft.playwright.APIRequestContext;
import com.microsoft.playwright.APIResponse;
import org.json.JSONObject;

public class BookService {

  private final APIRequestContext api;

  public BookService(APIRequestContext api) {
    this.api = api;
  }

  public int createBook(String title, String isbn, int authorId) {
    JSONObject bookData = new JSONObject()
      .put("title", title)
      .put("isbn", isbn)
      .put("publicationDate", "2025-01-01")
      .put("author", new JSONObject().put("id", authorId));

    return Integer.parseInt(com.project.api.utils.TestDataHelper.createEntity(api, "/books", bookData));
  }

  public APIResponse getBookByIsbn(String isbn) {
    return api.get("/books/isbn/" + isbn);
  }

  public APIResponse deleteBook(int bookId) {
    return api.delete("/books/" + bookId);
  }

  public APIResponse getAllBooks() {
    return api.get("/books");
  }

  public APIResponse createDuplicateBook(String isbn, int authorId) {
    JSONObject bookData = new JSONObject()
      .put("title", "Duplicate Book")
      .put("isbn", isbn)
      .put("publicationDate", "2025-01-01")
      .put("author", new JSONObject().put("id", authorId));

    return api.post("/books",
      com.microsoft.playwright.options.RequestOptions.create()
        .setHeader("Content-Type", "application/json")
        .setData(bookData.toString()));
  }
}
