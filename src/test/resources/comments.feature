@E2E
Feature: comment management

  Scenario: client adds and retrieves comments for a photo
    Given a user named "Serena" with email serena@test.com exists
    And a photo exists in the system
    When the client adds a comment "Nice job with this Lab!" to that photo
    Then the comment response status code is 201
    And the comment text is "Nice job with this Lab!"