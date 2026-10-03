package ru.mts.itmo.aqa.api.graphql;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.emptyOrNullString;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import ru.mts.itmo.aqa.api.support.ApiSpecs;
import ru.mts.itmo.aqa.api.support.ApiTestBase;

class GraphqlNegativeScenarioTest extends ApiTestBase {

    private static final String CREATE_PERSON = """
            mutation CreatePerson($person: PersonInput!) {
              createPerson(person: $person) { id name age }
            }""";

    static Stream<Arguments> malformedOperations() {
        return Stream.of(
                Arguments.of(
                        "unknown root field",
                        200,
                        "Cannot query field",
                        Map.of("query", "{ nonExistentField }")),
                Arguments.of(
                        "unknown nested field",
                        200,
                        "Cannot query field",
                        Map.of("query", "{ request { nonExistent } }")),
                Arguments.of(
                        "capitalised mutation field name",
                        200,
                        "Did you mean",
                        Map.of("query",
                                "mutation { CreatePerson(person: {name: \"x\", age: 1}) { id } }")),
                Arguments.of(
                        "unknown subscription field",
                        200,
                        "Did you mean",
                        Map.of("query", "subscription { unknownGreetings }")),
                Arguments.of(
                        "truncated query",
                        200,
                        "Syntax Error",
                        Map.of("query", "{ hello")),
                Arguments.of(
                        "empty query",
                        200,
                        "Syntax Error",
                        Map.of("query", "")),
                Arguments.of(
                        "required variable omitted",
                        400,
                        "was not provided",
                        Map.of(
                                "operationName", "CreatePerson",
                                "query", CREATE_PERSON)),
                Arguments.of(
                        "age sent as a string instead of an Int",
                        400,
                        "Int cannot represent",
                        createPersonWith(Map.of("name", "String age", "age", "33"))),
                Arguments.of(
                        "name sent as a number instead of a String",
                        400,
                        "String cannot represent",
                        createPersonWith(Map.of("name", 42, "age", 30))),
                Arguments.of(
                        "operationName that does not match the document",
                        400,
                        "Could not determine what operation to execute",
                        Map.of(
                                "operationName", "NoSuchOperation",
                                "query", "{ hello }")));
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("malformedOperations")
    @DisplayName("A rejected operation answers with errors and without data")
    void rejectedOperationReportsErrorsInsteadOfData(
            String scenario,
            int expectedStatus,
            String expectedMessageFragment,
            Map<String, Object> payload) {

        ApiSpecs.graphql()
                .body(payload)
            .when()
                .post()
            .then()
                .statusCode(expectedStatus)
                .body("errors", not(empty()))
                .body("errors[0].message", containsString(expectedMessageFragment))
                .body("errors[0].message", not(emptyOrNullString()))
                .body("data", nullValue());
    }

    private static Map<String, Object> createPersonWith(Map<String, Object> person) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("operationName", "CreatePerson");
        payload.put("query", CREATE_PERSON);
        payload.put("variables", Map.of("person", person));
        return payload;
    }
}
