package org.mobelite.editormanager.api;

import com.microsoft.playwright.*;
import com.microsoft.playwright.options.RequestOptions;
import org.junit.jupiter.api.*;

import java.time.LocalDate;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class AuthorApiTest {

    private static final String BASE_URL = System.getProperty("api.base.url", "http://localhost:8080");
    private static final String ENDPOINT = "/authors";
    private static final String CONTENT_TYPE_JSON = "application/json";

    static Playwright playwright;
    static APIRequestContext request;
    static String uniqueAuthorName;

    @BeforeAll
    static void setup() {
        playwright = Playwright.create();
        request = playwright.request().newContext(new APIRequest.NewContextOptions().setBaseURL(BASE_URL));
        uniqueAuthorName = "Test Author " + UUID.randomUUID();
    }

    @AfterAll
    static void teardown() {
        request.dispose();
        playwright.close();
    }

    // ✅ Positive test case
    @Test
    @Order(1)
    @DisplayName("Should create a new author successfully")
    void shouldCreateAuthorSuccessfully() {
        APIResponse response = createAuthor(uniqueAuthorName, "1975-05-20", "Testland");
        assertEquals(201, response.status(), "Expected HTTP 201 CREATED");
        assertTrue(response.text().contains(uniqueAuthorName), "Response should contain author's name");
    }

    // ❌ Duplicate author
    @Test
    @Order(2)
    @DisplayName("Should return error when author already exists")
    void shouldReturnErrorForDuplicateAuthor() {
        APIResponse response = createAuthor(uniqueAuthorName, "1975-05-20", "Testland");
        int status = response.status();
        String body = response.text();

        assertTrue(status == 409 || status == 500, "Expected 409 Conflict or 500 Internal Server Error");
        assertTrue(body.toLowerCase().contains("already exists"), "Error message should indicate duplication");
    }

    // ✅ Get all authors
    @Test
    @Order(3)
    @DisplayName("Should retrieve all authors")
    void shouldGetAllAuthors() {
        APIResponse response = request.get(ENDPOINT);
        String responseBody = response.text();

        assertEquals(200, response.status(), "Expected HTTP 200 OK");
        assertTrue(responseBody.contains("Authors fetched successfully"), "Should return success message");
        assertTrue(responseBody.contains(uniqueAuthorName), "Should include the previously added author");
    }

    // --- 🧪 Edge Cases ---

    @Test
    @Order(4)
    @DisplayName("Should fail when name is missing")
    void shouldFailWhenNameIsMissing() {
        String payload = generateAuthorPayload(null, "1980-01-01", "Tunisia");
        APIResponse response = sendRawPost(payload);
        assertEquals(400, response.status());
    }

    @Test
    @Order(5)
    @DisplayName("Should fail when birthDate is in the future")
    void shouldFailWhenBirthDateIsInFuture() {
        String futureDate = LocalDate.now().plusYears(1).toString();
        APIResponse response = createAuthor("Future Author " + UUID.randomUUID(), futureDate, "Utopia");
        assertEquals(400, response.status());
    }

    @Test
    @Order(6)
    @DisplayName("Should fail when nationality is blank")
    void shouldFailWhenNationalityIsBlank() {
        APIResponse response = createAuthor("Blank Nationality " + UUID.randomUUID(), "1970-01-01", "");
        assertEquals(400, response.status());
    }

    @Test
    @Order(7)
    @DisplayName("Should fail with completely empty payload")
    void shouldFailWithEmptyPayload() {
        APIResponse response = sendRawPost("{}");
        assertEquals(400, response.status());
    }

    // --- 🔧 Utility Methods ---

    private APIResponse createAuthor(String name, String birthDate, String nationality) {
        String payload = generateAuthorPayload(name, birthDate, nationality);
        return sendRawPost(payload);
    }

    private APIResponse sendRawPost(String payload) {
        return request.post(ENDPOINT, RequestOptions.create()
                .setHeader("Content-Type", CONTENT_TYPE_JSON)
                .setData(payload));
    }

    private String generateAuthorPayload(String name, String birthDate, String nationality) {
        return """
        {
          "name": %s,
          "birthDate": %s,
          "nationality": %s,
          "books": []
        }
        """.formatted(
                name == null ? null : "\"" + name + "\"",
                birthDate == null ? null : "\"" + birthDate + "\"",
                nationality == null ? null : "\"" + nationality + "\""
        );
    }
}