package es.rubenjgarcia.cucumber.rest.steps;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.skyscreamer.jsonassert.JSONAssert.assertEquals;
import static org.springframework.test.util.AssertionErrors.assertNotEquals;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.JsonPath;
import cucumber.api.DataTable;
import cucumber.api.java8.En;
import cucumber.api.java8.StepdefBody.A2;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.net.URI;
import java.nio.charset.Charset;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minidev.json.JSONArray;
import org.apache.commons.io.IOUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;
import org.skyscreamer.jsonassert.JSONParser;
import org.skyscreamer.jsonassert.comparator.ArraySizeComparator;
import org.springframework.core.NestedExceptionUtils;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.test.web.reactive.server.EntityExchangeResult;
import org.springframework.test.web.reactive.server.HeaderAssertions;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.test.web.reactive.server.WebTestClient.BodyContentSpec;
import org.springframework.test.web.reactive.server.WebTestClient.RequestBodySpec;
import org.springframework.test.web.reactive.server.WebTestClient.ResponseSpec;
import org.springframework.web.util.UriComponentsBuilder;
import org.yaml.snakeyaml.Yaml;

public class RestSteps implements En {

  private static final String HTTP_METHODS = "GET|POST|PUT|HEAD|DELETE|OPTIONS|PATCH|TRACE";
  private static final String COUNT_COMPARISON = "(?: (less than|more than|at least|at most))?";

  private WebTestClient webClient;
  private HttpHeaders headers;

  private ResponseSpec responseSpec;
  private BodyContentSpec expectBody;
  private HeaderAssertions headerAssertions;

  /**
   * Steps definitions.
   */
  public RestSteps() {
    Before(() -> {
      webClient = WebTestClient.bindToServer().build();
      responseSpec = null;
    });

    Given("^I call (" + HTTP_METHODS + ") \"([^\"]*)\"$", (A2<String, String>) this::call);

    Given("^I call (" + HTTP_METHODS + ") \"([^\"]*)\" with data(?:[:])?$", (String httpMethodString, String path, String data) -> {
      Yaml yaml = new Yaml();
      Object obj = yaml.load(data);
      call(httpMethodString, path, obj);
    });

    Given("^I call (" + HTTP_METHODS + ") \"([^\"]*)\" with data from \"([^\"]*)\"$",
        (String httpMethodString, String path, String from) -> {
          final String resource = IOUtils.resourceToString("/" + from, Charset.defaultCharset());
          ObjectMapper mapper = new ObjectMapper();
          JsonNode json = mapper.readTree(resource);
          call(httpMethodString, path, json);
        }
    );

    Given("^I call (" + HTTP_METHODS + ") \"([^\"]*)\" with query params(?:[:])?$",
        (String httpMethodString, String path, DataTable paramsTable) -> {
          final URI uri = paramsTable.getPickleRows().stream()
              .reduce(UriComponentsBuilder.fromUriString(path),
                  (ub, r) -> ub.queryParam(r.getCells().get(0).getValue(), r.getCells().get(1).getValue()), (ub1, ub2) -> ub1)
              .build()
              .toUri();
          call(httpMethodString, uri, null);
        }
    );

    Given("^I call (" + HTTP_METHODS + ") \"([^\"]*)\" with file \"([^\"]*)\" from \"([^\"]*)\"$",
        (String httpMethodString, String path, String file, String from) -> {
          FileSystemResource fileSystemResource = new FileSystemResource(new File(RestSteps.class.getResource("/").getPath() + from));
          callWithFile(httpMethodString, path, file, fileSystemResource);
        }
    );

    Given("^I call (" + HTTP_METHODS + ") \"([^\"]*)\" with empty file \"([^\"]*)\"$",
        (String httpMethodString, String path, String file) ->
            callWithFile(httpMethodString, path, file, new InputStreamResource(new ByteArrayInputStream(new byte[] {})))
    );

    Given("^I set headers to(?:[:])?$", (DataTable headersTable) -> {
      headers = new HttpHeaders();
      headersTable.getPickleRows().forEach(r -> headers.set(r.getCells().get(0).getValue(), r.getCells().get(1).getValue()));
    });

    Then("^The response status should be (\\d+)$", (Integer status) -> {
      withAssertion(() -> responseSpec.expectStatus().isEqualTo(status));
    });

    Then("^The response is empty$", () -> {
      initExpectBody();
      withAssertion(() -> expectBody.isEmpty());
    });

    Then("^The response should be(?:[:])?$", (String data) -> {
      Yaml yaml = new Yaml();
      Map<String, Object> map = yaml.load(data);
      JSONObject jsonObject = new JSONObject(map);

      initExpectBody();
      final byte[] responseBody = withAssertion(() -> expectBody.jsonPath("$").isNotEmpty().returnResult().getResponseBody());
      withAssertion(v -> {
        try {
          JSONAssert.assertEquals(jsonObject.toString(), new String(responseBody), false);
        } catch (JSONException e) {
          throw new AssertionError("Error comparing json string", e);
        }
      });
    });

    Then("^The response should contain empty array$", () -> assertResponseListSize(0));

    Then("^The response should contain array with size (\\d+)$", this::assertResponseListSize);

    Then("^The response should be array(?:[:])?$", (String data) -> {
      Yaml yaml = new Yaml();
      Object dataObj = yaml.load(data);
      assertTrue(List.class.isAssignableFrom(dataObj.getClass()));
      initExpectBody();
      final byte[] responseBody = withAssertion(() -> expectBody.jsonPath("$").isArray().returnResult().getResponseBody());
      final JSONArray jsonArray = JsonPath.compile("$").read(new String(responseBody));
      withAssertion(v -> assertTrue(jsonArray.containsAll((Collection<?>) dataObj)));
    });

    Then("^The response should contain \"([^\"]*)\"$", (String key) -> {
      initExpectBody();
      withAssertion(() -> expectBody.jsonPath("$." + key).exists());
    });

    Then("^The response should not contain \"([^\"]*)\"$", (String key) -> {
      initExpectBody();
      withAssertion(() -> expectBody.jsonPath("$." + key).doesNotExist());
    });

    Then("^The response should contain \"([^\"]*)\" with value \"([^\"]*)\"$", (String key, Object value) -> {
      initExpectBody();
      withAssertion(() -> expectBody.jsonPath("$." + key).isEqualTo(value));
    });

    Then("^The response should not contain \"([^\"]*)\" with value \"([^\"]*)\"$", (String key, Object value) -> {
      initExpectBody();
      final byte[] responseBody = withAssertion(() -> expectBody.jsonPath("$." + key).exists().returnResult().getResponseBody());
      final Object actual = JsonPath.compile("$." + key).read(new String(responseBody));
      withAssertion(v -> assertNotEquals(key, value, actual));
    });

    Then("^The response should contain" + COUNT_COMPARISON + " (\\d+) entit(?:ies|y)$",
        (String comparisonAction, Integer count) -> {
          initExpectBody();
          final byte[] responseBody =
              withAssertion(() -> expectBody.jsonPath("$").isArray().returnResult().getResponseBody());
          final JSONArray jsonArray = JsonPath.compile("$").read(new String(responseBody));
          withAssertion(v -> compareCounts(Optional.ofNullable(comparisonAction).orElse(""), count, jsonArray.size()));
        }
    );

    Then("^Response entity \"([^\"]*)\" should contain \"([^\"]*)\"$", (String entity, String key) -> {
      initExpectBody();
      withAssertion(() -> expectBody.jsonPath("$." + entity + "." + key).exists());
    });

    Then("^Response entity \"([^\"]*)\" should not contain \"([^\"]*)\"$", (String entity, String key) -> {
      initExpectBody();
      withAssertion(() -> expectBody.jsonPath("$." + entity + "." + key).doesNotExist());
    });

    Then("^Response entity \"([^\"]*)\" should contain \"([^\"]*)\" with value \"([^\"]*)\"$",
        (String entity, String key, Object value) -> {
          initExpectBody();
          withAssertion(() -> expectBody.jsonPath("$." + entity + "." + key).isEqualTo(value));
        }
    );

    Then("^Response entity \"([^\"]*)\" should not contain \"([^\"]*)\" with value \"([^\"]*)\"$",
        (String entity, String key, Object value) -> {
          initExpectBody();
          final byte[] responseBody = withAssertion(
              () -> expectBody.jsonPath("$." + entity + "." + key).exists().returnResult().getResponseBody());
          final Object actual = JsonPath.compile("$." + entity + "." + key).read(new String(responseBody));
          withAssertion(v -> assertNotEquals(key, value, actual));
        }
    );

    Then("^Response entity \"([^\"]*)\" should contain array(?:[:])?$", (String entity, String data) -> {
      Yaml yaml = new Yaml();
      Object dataObj = yaml.load(data);
      assertTrue(List.class.isAssignableFrom(dataObj.getClass()));
      initExpectBody();
      final byte[] responseBody = withAssertion(() -> expectBody.jsonPath("$." + entity).isArray().returnResult().getResponseBody());
      final JSONArray jsonArray = JsonPath.compile("$." + entity).read(new String(responseBody));
      withAssertion(v -> assertTrue(jsonArray.containsAll((Collection<?>) dataObj)));
    });

    Then("^Response entity \"([^\"]*)\" should contain" + COUNT_COMPARISON + " (\\d+) entit(?:ies|y)$",
        (String entityName, String comparisonAction, Integer count) -> {
          initExpectBody();
          final byte[] responseBody =
              withAssertion(() -> expectBody.jsonPath("$." + entityName).isArray().returnResult().getResponseBody());
          final JSONArray jsonArray = JsonPath.compile("$." + entityName).read(new String(responseBody));
          withAssertion(v -> compareCounts(Optional.ofNullable(comparisonAction).orElse(""), count, jsonArray.size()));
        }
    );

    Then("^The response headers should contain \"([^\"]*)\"$", (String header) -> {
      initHeaders();
      withAssertion(() -> headerAssertions.exists(header));
    });

    Then("^The response headers should not contain \"([^\"]*)\"$", (String header) -> {
      initHeaders();
      withAssertion(() -> headerAssertions.doesNotExist(header));
    });

    Then("^The response headers should contain \"([^\"]*)\" with value \"([^\"]*)\"$", (String header, String value) -> {
      initExpectBody();
      final HttpHeaders responseHeaders = expectBody.returnResult().getResponseHeaders();
      withAssertion(v -> {
        final List<String> headerValue = responseHeaders.get(header);
        assertTrue("Response header " + header + " has value " + headerValue + "; expected to have value " + value,
            headerValue.contains(value));
      });
    });

    Then("^The response headers should not contain \"([^\"]*)\" with value \"([^\"]*)\"$", (String header, String value) -> {
      initExpectBody();
      final HttpHeaders responseHeaders = expectBody.returnResult().getResponseHeaders();
      withAssertion(v -> {
        final List<String> headerValue = responseHeaders.get(header);
        assertFalse("Response header " + header + " was not expected to have " + value + " value", headerValue.contains(value));
      });
    });
  }

  private void call(String httpMethodString, String path) throws JSONException {
    call(httpMethodString, URI.create(path), null);
  }

  private void call(String httpMethodString, String path, Object requestObj) throws JSONException {
    call(httpMethodString, URI.create(path), requestObj);
  }

  private void call(String httpMethodString, URI uri, Object requestObj) throws JSONException {
    HttpMethod httpMethod = HttpMethod.valueOf(httpMethodString.toUpperCase());

    if (httpMethod.equals(HttpMethod.GET) && requestObj != null) {
      throw new IllegalArgumentException("You can't pass data in a GET call");
    }

    final String path = uri.toString();
    if (path.contains("$.")) {
      initExpectBody();
      Pattern pattern = Pattern.compile("(((\\$.*)\\/.*)|(\\$.*))");
      Matcher matcher = pattern.matcher(path);
      assertTrue(matcher.find());
      final String jsonPath = matcher.group(0);
      final byte[] responseBody = expectBody.jsonPath(jsonPath).exists().returnResult().getResponseBody();
      final String value = ((JSONObject) JSONParser.parseJSON(new String(responseBody))).getString(jsonPath.replace("$.", ""));
      uri = URI.create(path.replace(jsonPath, value));
    }

    final RequestBodySpec requestBodySpec = webClient.method(httpMethod).uri(uri);
    if (requestObj != null) {
      requestBodySpec.syncBody(requestObj);
      requestBodySpec.contentType(MediaType.APPLICATION_JSON);
    }

    if (headers != null) {
      headers.forEach((key, value) -> requestBodySpec.header(key, value.toArray(new String[0])));
    }

    responseSpec = requestBodySpec.exchange();
    expectBody = null;
    headers = null;
  }

  private void callWithFile(String httpMethodString, String path, String file, Resource resource) {
    HttpMethod httpMethod = HttpMethod.valueOf(httpMethodString.toUpperCase());

    if (httpMethod.equals(HttpMethod.GET)) {
      throw new IllegalArgumentException("You can't submit a file in a GET call");
    }

    MultipartBodyBuilder builder = new MultipartBodyBuilder();
    builder.part(file, resource);

    responseSpec = webClient.method(httpMethod)
        .uri(path)
        .syncBody(builder.build())
        .exchange();
    expectBody = null;
  }

  private void assertResponseListSize(int size) throws JSONException {
    initExpectBody();
    final byte[] responseBody = withAssertion(() -> expectBody.jsonPath("$").isArray().returnResult().getResponseBody());
    assertEquals("[" + size + "]", new String(responseBody), new ArraySizeComparator(JSONCompareMode.STRICT_ORDER));
  }

  private void compareCounts(String comparison, int expected, int actual) {
    switch (comparison) {
      case "at least":
        assertTrue("Expected to have at least " + expected + " but has " + actual, actual >= expected);
        break;
      case "at most":
        assertTrue("Expected to have at most " + expected + " but has " + actual, actual <= expected);
        break;
      case "more than":
        assertTrue("Expected to have more than " + expected + " but has " + actual, actual > expected);
        break;
      case "less than":
        assertTrue("Expected to have less than " + expected + " but has " + actual, actual < expected);
        break;
      default:
        assertEquals("Expected to have " + expected + " but has " + actual, expected, actual);
    }
  }

  private void initExpectBody() {
    assertNotNull(responseSpec);
    expectBody = expectBody == null ? responseSpec.expectBody() : expectBody;
  }

  private void initHeaders() {
    assertNotNull(responseSpec);
    headerAssertions = headerAssertions == null ? responseSpec.expectHeader() : headerAssertions;
  }

  private <T> T withAssertion(Supplier<T> supplier) {
    try {
      return supplier.get();
    } catch (AssertionError error) {
      initExpectBody();
      final EntityExchangeResult<byte[]> entityExchangeResult = expectBody.returnResult();
      throw new AssertionError(NestedExceptionUtils.getMostSpecificCause(error).getMessage() + "\n" + entityExchangeResult);
    }
  }

  private void withAssertion(Consumer<Void> consumer) {
    try {
      consumer.accept(null);
    } catch (AssertionError error) {
      initExpectBody();
      final EntityExchangeResult<byte[]> entityExchangeResult = expectBody.returnResult();
      throw new AssertionError(NestedExceptionUtils.getMostSpecificCause(error).getMessage() + "\n" + entityExchangeResult);
    }
  }
}
