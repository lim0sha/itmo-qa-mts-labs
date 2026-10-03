package ru.mts.itmo.aqa.api.graphql;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.emptyOrNullString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import io.restassured.path.json.JsonPath;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.mts.itmo.aqa.api.support.ApiSpecs;
import ru.mts.itmo.aqa.api.support.ApiTestBase;

class GraphqlMutationTest extends ApiTestBase {

    private static final String MUTATION = """
            mutation CreatePerson($person: PersonInput!) {
              createPerson(person: $person) { id name age }
            }""";

    @Test
    @DisplayName("CreatePerson answers 200 with the submitted name, the submitted age and a generated id")
    void createPersonReturnsSubmittedFieldsAndGeneratedId() {
        String name = "Api Tester " + UUID.randomUUID();
        int age = 34;

        ApiSpecs.graphql()
                .body(payload(Map.of("name", name, "age", age)))
            .when()
                .post()
            .then()
                .statusCode(200)
                .body("errors", nullValue())
                .body("data.createPerson.id", not(emptyOrNullString()))
                .body("data.createPerson.name", equalTo(name))
                .body("data.createPerson.age", equalTo(age));
    }

    @Test
    @DisplayName("Every mutation issues a new person id")
    void everyMutationIssuesANewId() {
        String firstId = createPerson("first " + UUID.randomUUID(), 21).getString("data.createPerson.id");
        String secondId = createPerson("second " + UUID.randomUUID(), 22).getString("data.createPerson.id");

        assertNotEquals(firstId, secondId, "the stand must not reuse a person id");
    }

    @Test
    @DisplayName("Age is optional: the server fills in a value when the input omits it")
    void serverAssignsAgeWhenTheInputOmitsIt() {
        JsonPath person = createPersonWithoutAge("no age " + UUID.randomUUID());

        assertThat(person.get("data.createPerson.age"), notNullValue());
        assertThat(person.get("data.createPerson.age"), greaterThanOrEqualTo(0));
    }

    private JsonPath createPerson(String name, int age) {
        return ApiSpecs.graphql()
                .body(payload(Map.of("name", name, "age", age)))
            .when()
                .post()
            .then()
                .statusCode(200)
                .body("errors", nullValue())
                .body("data.createPerson.name", equalTo(name))
                .extract()
                .jsonPath();
    }

    private JsonPath createPersonWithoutAge(String name) {
        return ApiSpecs.graphql()
                .body(payload(Map.of("name", name)))
            .when()
                .post()
            .then()
                .statusCode(200)
                .body("errors", nullValue())
                .extract()
                .jsonPath();
    }

    private Map<String, Object> payload(Map<String, Object> person) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("operationName", "CreatePerson");
        body.put("query", MUTATION);
        body.put("variables", Map.of("person", person));
        return body;
    }
}
