package org.mobelite.utils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class TestDataHelper {
  private static final String BASE_API_URL = "http://backend:8080";
  //private static final String BASE_API_URL = "http://localhost:8080";

  private static final HttpClient client = HttpClient.newHttpClient();

  public static String createAuthor(String name, String nationality) {
    String body = String.format("""
            {
              "name": "%s",
              "birthDate": "1980-01-01",
              "nationality": "%s"
            }
        """, name, nationality);

    HttpRequest request = HttpRequest.newBuilder()
      .uri(URI.create(BASE_API_URL + "/authors"))
      .header("Content-Type", "application/json")
      .POST(HttpRequest.BodyPublishers.ofString(body))
      .build();

    try {
      HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
      if (response.statusCode() != 200 && response.statusCode() != 201) {
        throw new RuntimeException("Failed to create author: " + response.body());
      }

      // Parse the actual ID from response JSON
      ObjectMapper mapper = new ObjectMapper();
      JsonNode json = mapper.readTree(response.body());
      return json.get("data").get("id").asText();

    } catch (IOException | InterruptedException e) {
      throw new RuntimeException(e);
    }
  }

  public static void deleteAuthor(String authorId) {
    if (authorId == null) return;
    HttpRequest request = HttpRequest.newBuilder()
      .uri(URI.create(BASE_API_URL + "/authors/" + authorId))
      .DELETE()
      .build();

    try {
      client.send(request, HttpResponse.BodyHandlers.ofString());
    } catch (IOException | InterruptedException e) {
      throw new RuntimeException(e);
    }
  }
}
