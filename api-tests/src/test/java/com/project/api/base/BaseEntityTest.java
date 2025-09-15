package com.project.api.base;

import com.microsoft.playwright.APIResponse;
import com.project.api.utils.HttpStatus;
import org.everit.json.schema.Schema;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.jupiter.api.AfterEach;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public abstract class BaseEntityTest extends BaseApiTest {

  protected static final List<TestEntity> createdEntities = new ArrayList<>();

  protected record TestEntity(String type, Long id, String endpoint, JSONObject data) { }

  @AfterEach
  void cleanupEntitiesAfterEach() {
    cleanupCreatedEntities();
  }

  protected static void cleanupCreatedEntities() {
    for (int i = createdEntities.size() - 1; i >= 0; i--) {
      TestEntity entity = createdEntities.get(i);
      try {
        APIResponse response = api.delete(entity.endpoint + "/" + entity.id);
        if (response.status() == HttpStatus.OK || response.status() == HttpStatus.NO_CONTENT) {
          System.out.println("♻️ Cleanup: deleted " + entity.type + " (ID=" + entity.id + ")");
        } else {
          System.out.println("⚠️ Cleanup warning: failed to delete " + entity.type + " (ID=" + entity.id + "), status: " + response.status());
        }
      } catch (Exception e) {
        System.out.println("⚠️ Cleanup error for " + entity.type + " (ID=" + entity.id + "): " + e.getMessage());
      }
    }
    createdEntities.clear();
  }

  protected JSONObject createEntity(String endpoint, JSONObject entityData, Schema schema) {
    APIResponse response = api.post(endpoint,
      com.microsoft.playwright.options.RequestOptions.create()
        .setData(entityData.toString())
        .setHeader("Content-Type", "application/json")
    );

    assertEquals(HttpStatus.CREATED, response.status(),
      "Entity creation failed. Status: " + response.status() + ", Body: " + response.text());

    JSONObject createdEntity = new JSONObject(response.text()).getJSONObject("data");

    if (schema != null) {
      schema.validate(createdEntity);
    }

    String entityType = endpoint.replaceAll("^/", "");
    createdEntities.add(new TestEntity(entityType, createdEntity.getLong("id"), endpoint, createdEntity));

    return createdEntity;
  }

  protected JSONObject getEntityById(String endpoint, Long id) {
    APIResponse response = api.get(endpoint + "/" + id);
    assertEquals(HttpStatus.OK, response.status(), "GET " + endpoint + "/" + id + " failed with status: " + response.status());
    return new JSONObject(response.text()).getJSONObject("data");
  }

  protected JSONArray getAllEntities(String endpoint) {
    APIResponse response = api.get(endpoint);
    assertEquals(HttpStatus.OK, response.status(), "GET " + endpoint + " failed with status: " + response.status());
    return new JSONObject(response.text()).getJSONArray("data");
  }

  protected void deleteEntity(String endpoint, Long id) {
    APIResponse response = api.delete(endpoint + "/" + id);
    assertTrue(response.status() == HttpStatus.OK || response.status() == HttpStatus.NO_CONTENT,
      "DELETE " + endpoint + "/" + id + " failed with status: " + response.status());
  }

  protected boolean entityExistsInList(JSONArray entities, String idField, Object idValue) {
    for (int i = 0; i < entities.length(); i++) {
      JSONObject entity = entities.getJSONObject(i);
      if (entity.get(idField).equals(idValue)) {
        return true;
      }
    }
    return false;
  }

  protected JSONObject findEntityInList(JSONArray entities, String field, Object value) {
    for (int i = 0; i < entities.length(); i++) {
      Object fieldValue = entities.getJSONObject(i).get(field);

      if (fieldValue instanceof Number && value instanceof Number) {
        if (((Number) fieldValue).longValue() == ((Number) value).longValue()) {
          return entities.getJSONObject(i);
        }
      } else if (fieldValue.equals(value)) {
        return entities.getJSONObject(i);
      }
    }
    return null;
  }

  protected String generateValidIsbn() {
    return "ISBN" + (System.nanoTime() % 10000000000000L);
  }
}
