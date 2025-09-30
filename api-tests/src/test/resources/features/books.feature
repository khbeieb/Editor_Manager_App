Feature: Book management

  @smoke @regression @integration
  Scenario: Create a new book with an author
    Given an author exists with name "Cucumber Author"
    When I create a book titled "Cucumber Book"
    Then the book should be created successfully

  @regression @integration
  Scenario: Prevent duplicate ISBN
    Given an author exists with name "Duplicate Author"
    And a book already exists with ISBN "1234567890"
    When I try to create another book with ISBN "1234567890"
    Then the API should reject the request

  @regression @integration
  Scenario: Retrieve all books
    Given an author exists with name "List Author"
    And I create a book titled "Book 1"
    And I create a book titled "Book 2"
    When I get all books
    Then the response should contain a list of books

  @smoke @regression @integration
  Scenario: Retrieve a book by ISBN
    Given an author exists with name "Specific Author"
    And I create a book titled "Specific Book"
    When I get the book by its ISBN
    Then the correct book details should be returned

  @regression @integration
  Scenario: Delete a book
    Given an author exists with name "Author To Delete"
    And I create a book titled "Book To Delete"
    When I delete the book
    Then the book should not exist anymore
