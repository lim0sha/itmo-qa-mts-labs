package ru.mts.itmo.aqa.api;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.emptyOrNullString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.mts.itmo.aqa.api.support.ApiConfig;
import ru.mts.itmo.aqa.api.support.ApiSpecs;
import ru.mts.itmo.aqa.api.support.ApiTestBase;

class ApiInfrastructureTest extends ApiTestBase {

    @Test
    @DisplayName("REST stand returns a populated post collection at the configured base URI")
    void restStandIsReachableAtConfiguredBaseUri() {
        ApiSpecs.rest()
                .queryParam("limit", 1)
            .when()
                .get("/posts")
            .then()
                .statusCode(200)
                .body("data", hasSize(1))
                .body("data[0].id", not(emptyOrNullString()))
                .body("pagination.total", not(emptyOrNullString()));
    }

    @Test
    @DisplayName("GraphQL stand accepts a query with the shared user agent")
    void graphqlStandIsReachableAtConfiguredEndpoint() {
        ApiSpecs.graphql()
                .body(Map.of("query", "{ hello }"))
            .when()
                .post()
            .then()
                .statusCode(200)
                .body("data.hello", not(emptyOrNullString()))
                .body("errors", nullValue());
    }

    @Test
    @DisplayName("A resource created by one request stays visible to the next request of the same test")
    void resourceStaysVisibleAcrossRequestsOfTheSameScenario() {
        String createdId = ApiSpecs.rest()
                .body(Map.of(
                        "title", "infrastructure probe",
                        "body", "created before the sandbox filter is exercised",
                        "user_id", 1))
            .when()
                .post("/posts")
            .then()
                .statusCode(201)
                .extract()
                .path("id");

        ApiSpecs.rest()
                .pathParam("id", createdId)
            .when()
                .get("/posts/{id}")
            .then()
                .statusCode(200)
                .body("id", equalTo(createdId))
                .body("title", equalTo("infrastructure probe"));
    }

    @Test
    @DisplayName("Without overrides the configuration points at the stands named in the handout")
    void defaultsPointAtTheHandoutEndpoints() {
        assumeTrue(System.getProperty("api.rest.baseUri") == null, "REST base URI overridden");
        assumeTrue(System.getenv("API_REST_BASE_URI") == null, "REST base URI overridden");
        assumeTrue(System.getProperty("api.graphql.uri") == null, "GraphQL URI overridden");
        assumeTrue(System.getenv("API_GRAPHQL_URI") == null, "GraphQL URI overridden");

        assertThat(ApiConfig.REST_BASE_URI, equalTo("https://playground.nileslabs.com/api/v1"));
        assertThat(ApiConfig.GRAPHQL_URI, equalTo("https://graphql.postman-echo.com/graphql"));
        assertThat(ApiConfig.USER_AGENT, containsString("Mozilla/5.0"));
    }
}
