# Cucumber REST Steps

With this library you can use [Cucumber](https://cucumber.io) to test REST endpoints

## Steps in this library

* `^I call (GET|POST|PUT|HEAD|DELETE|OPTIONS|PATCH|TRACE) "([^"]*)"$` -> Make a request to a URL with the given HTTP method

   `Given I call GET "https://github.com"`

* `^I call (GET|POST|PUT|HEAD|DELETE|OPTIONS|PATCH|TRACE) "([^"]*)" with data(?:[:])?$` -> Make a request to a URL with the given HTTP method and with the given json body. The json body will be parsed from a [doc string](https://cucumber.io/docs/reference#doc-strings) in [yaml](http://yaml.org/) format

   ```
   Given I call POST "https://github.com" with data:
   """
     foo: "bar"
     wee:
       - "yi"
   """
   ```

   This example will make a POST request to https://github.com with a json body `{"foo": "bar", "wee": ["yi"]}`

* `^I call (GET|POST|PUT|HEAD|DELETE|OPTIONS|PATCH|TRACE) "([^"]*)" with data from "([^"]*)"$` -> Make a request to a URL with the given HTTP method and use the given file to get the json body

   ```
   Given I call POST "https://github.com" with data from file "json.json"
   ```

   This example will make a POST request to https://github.com obtaining the json body from the file *json.json*

* `^I call (GET|POST|PUT|HEAD|DELETE|OPTIONS|PATCH|TRACE) "([^"]*)" with query params(?:[:])?$`  -> Make a request to a URL with the given HTTP method and with the query params parsed from a [data table](https://cucumber.io/docs/reference#data-tables)

   ```
   Given I call GET "https://github.com" with query params:
     | param | value |
   ```

   This example will make a GET request to https://github.com?param=value

* `^I call (GET|POST|PUT|HEAD|DELETE|OPTIONS|PATCH|TRACE) "([^"]*)" with file "([^"]*)" from "([^"]*)"$` -> Make a request to a URL with the given HTTP method and with a file from the classpath

   `Given I call POST "https://github.com" with file "myfile" from "files/file.txt"`

   This example will make a POST request to https://github.com with a file with name *myfile* and content from *files/file.txt*

* `^I call (GET|POST|PUT|HEAD|DELETE|OPTIONS|PATCH|TRACE) "([^"]*)" with empty file "([^"]*)"$` -> Make a request to a URL with the given HTTP method and with an empty file. This step is useful to test what happens when you submit an empty file to your endpoint

   `Given I call POST "https://github.com" with empty file "myfile"`

   This example will make a POST request to https://github.com with a file with name *myfile* and empty content

* `^I set headers to(?:[:])?$` -> Set the request headers from the [data table](https://cucumber.io/docs/reference#data-tables). You should use this step before any of the previous steps

   ```
   Given I set headers to:
     | header | value |
   ```

   This example put a header *header* with value *value* to the request

* `^The response status should be (\d+)$` -> Asserts that the given response status is equals to a number

   `Then The response status should be 200`

   This example asserts that the response status is 200 (OK)

* `^The response is empty$` -> Asserts that the given response is empty

   `Then The response is empty`

* `^The response should be(?:[:])?$` -> Asserts that the response is equals to the given json. The json will be parsed from a [doc string](https://cucumber.io/docs/reference#doc-strings) in [yaml](http://yaml.org/) format

   ```
   Then The response should be:
   """
      foo: "bar"
   """
   ```

   This example asserts that the response is `{"foo": "bar"}`

* `^The response should contain empty array$` -> Asserts that the response is an empty array

   `Then The response should contain empty array`

   This example asserts that the response is `[]`

* `^The response should contain array with size (\d+)$` -> Asserts that the response is an array and has the given size

   `Then The response should contain array with size 3`

   This example asserts that the response is an array and has 3 elements

* `^The response should be array(?:[:])?$` -> Asserts that the response is the given array. The array will be parsed from a [doc string](https://cucumber.io/docs/reference#doc-strings) in [yaml](http://yaml.org/) format

   ```
   Then The response should be array:
   """
     - "foo"
     - "bar"
   """
   ```

   This example asserts that the response is the array `["foo", "bar"]`

* `^The response should contain "([^"]*)"$` -> Asserts that the response contains the given key

   `Then The response should contain "foo"`

   This example asserts that the response contains the key *foo* with any value

* `^The response should not contain "([^"]*)"$` -> Asserts that the response doesn't contain the given key

   `Then The response should not contain "foo"`

   This example asserts that the response doesn't contain the key *foo*

* `^The response should contain "([^"]*)" with value "([^"]*)"$` -> Asserts that the response contains the given key with the given value

   `Then The response should contain "foo" with value "bar"`

   This example asserts that the response contains the key *foo* with value "bar"

* `^The response should not contain "([^"]*)" with value "([^"]*)"$` -> Asserts that the response contains the given key but the value is not the given value

   `Then The response should not contain "foo" with value "bar"`

   This example asserts that the response contains the key *foo* but its value is not "bar"

* `^The response should contain(?: (less than|more than|at least|at most))? (\\d+) entit(?:ies|y)$` -> Asserts that the response is an array and has less than, more than, at least or at most entities

   `Then The response should contains 3 entities`

   This example asserts that the response is an array with 3 elements

   `Then The response should contains less than 3 entities`

   This example asserts that the response is an array with less than 3 elements

   `Then The response should contains more than 3 entities`

   This example asserts that the response is an array with more than 3 elements

   `Then The response should contains at least 3 entities`

   This example asserts that the response is an array with at least 3 elements

   `Then The response should contains at most 3 entities`

   This example asserts that the response is an array with at most 3 elements

* `^Response entity "([^"]*)" should contain "([^"]*)"$` -> Asserts that the given entity from the response contains the given key

   `Then Response entity "foo" should contain "bar"`

   This example asserts that the response entity *foo* contains the key *bar* with any value

* `^Response entity "([^"]*)" should not contain "([^"]*)"$` -> Asserts that the given entity from the response doesn't contain the given key

   `Then Response entity "foo" should not contain "bar"`

   This example asserts that the response entity *foo* doesn't contain the key *bar*

* `^Response entity "([^"]*)" should contain "([^"]*)" with value "([^"]*)"$` -> Asserts that the given entity from the response contains the given key with the given value

   `Then Response entity "foo" should contain "bar" with value "wee"`

   This example asserts that the response entity *foo* contains the key *bar* with value "wee"

* `^Response entity "([^"]*)" should not contain "([^"]*)" with value "([^"]*)"$` -> Asserts that the given entity from the response contains the given key but the value is not the given value

   `Then Response entity "foo" should not contain "bar" with value "wee"`

   This example asserts that the response entity *foo* contains the key *bar* but the value is not "wee"

* `^Response entity \"([^\"]*)\" should contain array(?:[:])?$` -> Asserts that the given response entity is the given array. The array will be parsed from a [doc string](https://cucumber.io/docs/reference#doc-strings) in [yaml](http://yaml.org/) format

   ```
   Then Response entity "foo" should contain array:
   """
     - "bar"
     - "wee"
   """
   ```

   This example asserts that the response entity *foo* contains the array `["bar", "wee"]`

* `^Response entity \"([^\"]*)\" should contain(?: (less than|more than|at least|at most))? (\\d+) entit(?:ies|y)$` -> Assert than the given entity from the response is an array and has less than, more than, at least or at most entities

   `Then Response entity "foo" should contains 3 entities`

   This example asserts that the response entity *foo* is an array with 3 elements

   `Then Response entity "foo" should contains less than 3 entities`

   This example asserts that the response entity *foo* is an array with less than 3 elements

   `Then Response entity "foo" should contains more than 3 entities`

   This example asserts that the response entity *foo* is an array with more than 3 elements

   `Then Response entity "foo" should contains at least 3 entities`

   This example asserts that the response entity *foo* is an array with at least 3 elements

   `Then Response entity "foo" should contains at most 3 entities`

   This example asserts that the response entity *foo* is an array with at most 3 elements

* `^The response headers should contain "([^"]*)"$` -> Asserts that the response headers contain the given header

   `The response headers should contain "foo"`

   This example asserts that the response headers contain a header *foo* with any value

* `^The response headers should not contain "([^"]*)"$` -> Asserts that the response headers don't contain the given header

   `The response headers should not contain "foo"`

   This example asserts that the response headers don't contain a header *foo*

* `^The response headers should contain "([^"]*)" with value "([^"]*)"$` -> Asserts that the response headers contain the given header and any of its values is the given value

   `The response headers should contain "foo" with value "bar"`

   This example asserts that the response headers contain a header *foo* and has *bar* value

* `^The response headers should not contain "([^"]*)" with value "([^"]*)"$` -> Asserts that the response headers contain the given header and none of its values is the given value

   `The response headers should not contain "foo" with value "bar"`

   This example asserts that the response headers contain a header *foo* and hasn`t *bar* as value

* You can access to the response object and use it to chain another request

   ```
   Given I call GET "https://github.com"
   Then I call GET "https://github.com/$.foo.bar"
   ```

   In this example we made a request and then we made another request using the value of *foo.bar* from the first response
   
## Installation

Add dependency to your `pom.xml`

```xml
<dependency>
  <groupId>es.rubenjgarcia</groupId>
  <artifactId>cucumber-rest-steps</artifactId>
  <version>1.1.0-SNAPSHOT</version>
  <scope>test</scope>
</dependency>
```

Create the test class with the package glue

```java
import cucumber.api.CucumberOptions;
import cucumber.api.junit.Cucumber;
import org.junit.runner.RunWith;

@RunWith(Cucumber.class)
@CucumberOptions(glue = {"es.rubenjgarcia.cucumber.rest.steps"}, features = "classpath:features")
public class CucumberTestCase {

}
```
