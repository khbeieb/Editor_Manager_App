Feature: Authors API

  @smoke
  Scenario: Create a simple author
    When I create a new author with the name "Author" and nationality "Testland"
    Then the author should be created successfully

  @smoke
  Scenario: List authors
    Given a test author exists with name "List Author"
    When I list authors
    Then the new author should appear in the list

  @smoke
  Scenario: Delete author
    Given a test author exists with a book
    When I delete the test author
    Then the author should not appear in the list


  @regression
  Scenario: Create an author with books
    When I create a new author along with books
    Then the author should have 2 books

  @regression
  Scenario: Reject invalid author data
    When I create an invalid author entry
    Then the API should return a client error

  @regression
  Scenario: Reject duplicate author name
    Given a test author exists with name "Dup Author"
    When I create another author with the same name
    Then the API should return a 500 error

  @regression
  Scenario: Reject author with duplicate ISBNs
    When I create a test author with duplicate book ISBNs
    Then the API should return a 500 error
