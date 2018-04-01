Feature: Steps scenarios

  Scenario: Empty response
    Given I call GET "http://localhost:8080"
    Then The response status should be 200
    And The response is empty

  Scenario: With data
    Given I call POST "http://localhost:8080/withData" with data:
    """
      test: "test"
    """
    Then The response status should be 200
    And The response should be:
    """
      foo: "bar"
    """
    And The response should contain "foo"
    And The response should contain "foo" with value "bar"
    And The response should not contain "foo" with value "wee"
    And The response should not contain "bar"

  Scenario: With data from file
    Given I call POST "http://localhost:8080/withData" with data from "json.json"
    Then The response status should be 200
    And The response should be:
    """
      foo: "bar"
    """
    And The response should contain "foo"
    And The response should contain "foo" with value "bar"
    And The response should not contain "foo" with value "wee"
    And The response should not contain "bar"

  Scenario: With data from file
    Given I call POST "http://localhost:8080/withData" with data from "json-array.json"
    Then The response status should be 200
    And The response should be:
    """
      foo: "bar"
    """
    And The response should contain "foo"
    And The response should contain "foo" with value "bar"
    And The response should not contain "foo" with value "wee"
    And The response should not contain "bar"

  Scenario: With params
    Given I call GET "http://localhost:8080/withParams" with query params:
      | param | paramValue |
    Then The response status should be 200
    And The response should contain empty array

  Scenario: With headers
    Given I set headers to:
      | MyHeader | MyHeaderValue |
    Given I call GET "http://localhost:8080/withHeaders"
    Then The response status should be 200
    And The response headers should contain "MyHeader"
    And The response headers should not contain "foo"
    And The response headers should contain "MyHeader" with value "MyHeaderValue"
    And The response headers should not contain "MyHeader" with value "foo"
    And The response headers should contain "MultiHeader" with value "MultiHeaderValue1"
    And The response headers should not contain "MultiHeader" with value "foo"

  Scenario: With array
    Given I call GET "http://localhost:8080/withArray"
    Then The response status should be 200
    And The response should contain array with size 3
    And The response should contain 3 entities
    And The response should contain at least 2 entity
    And The response should contain at most 4 entities
    And The response should contain more than 2 entities
    And The response should contain less than 4 entities
    And The response should be array:
    """
      - foo: "bar"
      - foo: 3
      - foos:
        - bar
        - wee
    """
    And Response entity "[0]" should contain "foo"
    And Response entity "[0]" should not contain "bar"
    And Response entity "[0]" should contain "foo" with value "bar"
    And Response entity "[0]" should not contain "foo" with value "wee"
    And Response entity "[2].foos" should contain array:
    """
      - bar
      - wee
    """
    And Response entity "[2].foos" should contain 2 entities
    And Response entity "[2].foos" should contain at least 1 entity
    And Response entity "[2].foos" should contain at most 3 entities
    And Response entity "[2].foos" should contain more than 1 entities
    And Response entity "[2].foos" should contain less than 3 entities

  Scenario: Access response object
    Given I call GET "http://localhost:8080/response"
    Then The response status should be 200
    And The response should be:
    """
      foo: "bar"
    """
    Then I call GET "http://localhost:8080/response/$.foo"
    Then The response status should be 200
    And The response should be:
    """
      bar: "wee"
    """

  Scenario: With file
    Given I call POST "http://localhost:8080/withFile" with file "file" from "file.txt"
    Then The response status should be 200
    And The response is empty

  Scenario: With empty file
    Given I call POST "http://localhost:8080/withFile" with empty file "file"
    Then The response status should be 200
    And The response is empty