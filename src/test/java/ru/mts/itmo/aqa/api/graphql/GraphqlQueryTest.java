package ru.mts.itmo.aqa.api.graphql;

import static org.hamcrest.Matchers.emptyOrNullString;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.Matchers.startsWith;

import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.mts.itmo.aqa.api.support.ApiSpecs;
import ru.mts.itmo.aqa.api.support.ApiTestBase;

class GraphqlQueryTest extends ApiTestBase {

    @Test
    @DisplayName("A simple query answers 200 with greeting data and no errors")
    void simpleQueryReturnsGreetingWithoutErrors() {
        ApiSpecs.graphql()
                .body(Map.of("query", "{ hello }"))
            .when()
                .post()
            .then()
                .statusCode(200)
                .body("errors", nullValue())
                .body("data.hello", not(emptyOrNullString()))
                .body("data.hello", startsWith("Hello"));
    }

    @Test
    @DisplayName("A query may select several root fields at once and each one is filled in")
    void querySelectingSeveralFieldsFillsThemAll() {
        ApiSpecs.graphql()
                .body(Map.of("query", "{ hello request { headers } }"))
            .when()
                .post()
            .then()
                .statusCode(200)
                .body("errors", nullValue())
                .body("data.hello", not(emptyOrNullString()))
                .body("data.request.headers", not(emptyOrNullString()));
    }
}
