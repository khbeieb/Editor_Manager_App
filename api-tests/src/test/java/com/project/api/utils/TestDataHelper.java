package com.project.api.utils;

import com.microsoft.playwright.APIRequestContext;
import com.microsoft.playwright.APIResponse;
import com.microsoft.playwright.options.RequestOptions;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Map;

public class TestDataHelper {
  private static final ObjectMapper objectMapper = new ObjectMapper();

  // --- Author-specific helpers ---

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

  public static void deleteAuthor(APIRequestContext api, String authorId) {
    if (authorId == null) return;
    api.delete("/authors/" + authorId);
  }

  // --- Generic entity helpers ---

  public static String createEntity(APIRequestContext api, String endpoint, JSONObject entityData) {
    APIResponse response = api.post(endpoint,
      RequestOptions.create()
        .setData(entityData.toString())
        .setHeader("Content-Type", "application/json")
    );

    if (response.status() != 201) {
      throw new RuntimeException("Failed to create entity at " + endpoint + ": " + response.text());
    }

    try {
      JsonNode json = objectMapper.readTree(response.body());
      return json.get("data").get("id").asText();
    } catch (IOException e) {
      throw new RuntimeException("Failed to parse entity creation response: " + response.text(), e);
    }
  }

  public static JSONArray getAllEntities(APIRequestContext api, String endpoint) {
    APIResponse response = api.get(endpoint);
    if (response.status() != 200) {
      throw new RuntimeException("Failed to get entities from " + endpoint + ": " + response.text());
    }
    JSONObject json = new JSONObject(response.text());
    return json.getJSONArray("data");
  }

  public static void deleteEntity(APIRequestContext api, String endpoint, String id) {
    if (id == null) return;
    APIResponse response = api.delete(endpoint + "/" + id);
    if (response.status() != 200 && response.status() != 204) {
      throw new RuntimeException("Failed to delete entity at " + endpoint + "/" + id + ": " + response.text());
    }
  }

  public static String generateValidIsbn() {
    return "ISBN" + (System.nanoTime() % 10000000000000L);
  }
}
