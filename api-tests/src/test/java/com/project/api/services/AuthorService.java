package com.project.api.services;

import com.microsoft.playwright.APIRequestContext;
import org.json.JSONObject;
import com.project.api.utils.TestDataHelper;

public class AuthorService {

  private final APIRequestContext api;

  public AuthorService(APIRequestContext api) {
    this.api = api;
  }

  public int createAuthor(String name, String birthDate, String nationality) {
    JSONObject authorData = new JSONObject()
      .put("name", name)
      .put("birthDate", birthDate)
      .put("nationality", nationality);

    return Integer.parseInt(TestDataHelper.createEntity(api, "/authors", authorData));
  }

  public void deleteAuthor(int authorId) {
    api.delete("/authors/" + authorId);
  }
}
