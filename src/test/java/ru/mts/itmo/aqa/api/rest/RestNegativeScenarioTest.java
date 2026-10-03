package ru.mts.itmo.aqa.api.rest;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.emptyOrNullString;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;

import java.util.Map;
import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import ru.mts.itmo.aqa.api.support.ApiConfig;
import ru.mts.itmo.aqa.api.support.ApiSpecs;
import ru.mts.itmo.aqa.api.support.ApiTestBase;

class RestNegativeScenarioTest extends ApiTestBase {

    private static final String UNKNOWN_LOCAL_ID = "local-00000000-0000-0000-0000-000000000000";

    static Stream<Arguments> rejectedRequests() {
        return Stream.of(
                Arguments.of(
                        "GET of a sandbox id that was never created",
                        "GET", "/posts/" + UNKNOWN_LOCAL_ID, null,
                        404, "not found"),
                Arguments.of(
                        "PATCH of a sandbox id that was never created",
                        "PATCH", "/posts/" + UNKNOWN_LOCAL_ID, Map.of("title", "anything"),
                        404, "not found"),
                Arguments.of(
                        "GET with an id that is not a valid identifier",
                        "GET", "/posts/not-an-id", null,
                        400, "Invalid ID format"),
                Arguments.of(
                        "GET of a seeded id that does not exist",
                        "GET", "/posts/999999", null,
                        404, "not found"),
                Arguments.of(
                        "login with a wrong password",
                        "POST", "/auth/login",
                        Map.of("username", ApiConfig.USERNAME, "password", "definitely-not-the-password"),
                        401, "Invalid username or password"),
                Arguments.of(
                        "login with an unknown username",
                        "POST", "/auth/login",
                        Map.of("username", "no-such-user", "password", ApiConfig.PASSWORD),
                        401, "Invalid username or password"),
                Arguments.of(
                        "login without any credentials",
                        "POST", "/auth/login", Map.of(),
                        400, "required"),
                Arguments.of(
                        "request to a route the API does not expose",
                        "GET", "/no-such-collection", null,
                        404, "Cannot find"));
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("rejectedRequests")
    @DisplayName("An invalid request is refused with a status code and an explanation")
    void invalidRequestIsRefusedWithAnExplanation(
            String scenario,
            String method,
            String path,
            Map<String, Object> body,
            int expectedStatus,
            String expectedErrorFragment) {

        ApiSpecs.rest()
                .body(body == null ? "" : body)
            .when()
                .request(method, path)
            .then()
                .statusCode(expectedStatus)
                .body("status", not(nullValue()))
                .body("error", not(emptyOrNullString()))
                .body("error", containsString(expectedErrorFragment))
                .body("message", not(emptyOrNullString()));
    }

    @Test
    @DisplayName("A post created without a title comes back with the omitted field missing")
    void omittedFieldIsAbsentFromTheCreatedResource() {
        ApiSpecs.rest()
                .body(Map.of("body", "a post submitted without a title", "user_id", 1))
            .when()
                .post("/posts")
            .then()
                .statusCode(201)
                .body("id", not(emptyOrNullString()))
                .body("body", not(emptyOrNullString()))
                .body("title", nullValue());
    }
}
