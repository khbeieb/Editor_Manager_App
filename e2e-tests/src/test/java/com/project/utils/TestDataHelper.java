package com.project.utils;

import com.microsoft.playwright.APIRequestContext;
import com.microsoft.playwright.APIResponse;
import com.microsoft.playwright.options.RequestOptions;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.Map;

public class TestDataHelper {
  private static final ObjectMapper objectMapper = new ObjectMapper();

  /**
   * Creates an author via API and returns its ID.
   */
  public static String createAuthor(APIRequestContext api, String name, String nationality) {
    APIResponse response = api.post("/authors",
      RequestOptions.create().setData(Map.of(
        "name", name,
        "birthDate", "1980-01-01",
        "nationality", nationality
      ))
    );

    if (response.status() != 201 && response.status() != 200) {
      throw new RuntimeException("Failed to create author: " + response.text());
    }

    try {
      JsonNode json = objectMapper.readTree(response.body());
      return json.get("data").get("id").asText();
    } catch (IOException e) {
      throw new RuntimeException("Failed to parse author creation response: " + response.text(), e);
    }
  }

  /**
   * Deletes an author by ID.
   */
  public static void deleteAuthor(APIRequestContext api, String authorId) {
    if (authorId == null) return;
    api.delete("/authors/" + authorId);
  }
}
