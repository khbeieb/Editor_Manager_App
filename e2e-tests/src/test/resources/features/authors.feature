Feature: Authors Page

  @smoke
  Scenario: Display authors page title
    Given the user navigates to the authors page
    Then the page title should contain "Authors Library"

  @smoke
  Scenario: Show empty message when no authors
    Given the user navigates to the authors page
    Then the empty message should be visible

  @regression
  Scenario: Create and display author in table
    Given the user navigates to the authors page
    When the user creates an author "Test Author" with nationality "French"
    And the user refreshes the authors list
    Then the first author row should have name "Test Author"
    And the first author row should have nationality "French"

  @regression
  Scenario: Filter authors by name
    Given the user navigates to the authors page
    When the user creates an author "Unique Author" with nationality "German"
    And the user refreshes the authors list
    And the user searches for "Unique"
    Then the first author row should have name "Unique Author"

  @regression
  Scenario: Sort authors by name
    Given the user navigates to the authors page
    When the user creates an author "AAA Author" with nationality "French"
    And the user creates an author "ZZZ Author" with nationality "French"
    And the user refreshes the authors list
    And the user sorts by "Name" in "Ascending" order
    Then the first author row should have name "AAA Author"

  @smoke @regression
  Scenario: Handle error state gracefully
    Given the user navigates to the authors page
    Then the error message may be visible
