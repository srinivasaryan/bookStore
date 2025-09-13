Feature: Search book

  Scenario: User searches a book by title
    Given the bookstore has "Clean Code" by "Robert C. Martin"
    When the user searches for "Clean Code"
    Then the system shows "Clean Code" with author name "Robert C. Martin"
