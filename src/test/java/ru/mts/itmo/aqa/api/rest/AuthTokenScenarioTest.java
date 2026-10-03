package ru.mts.itmo.aqa.api.rest;

import static org.hamcrest.Matchers.containsStringIgnoringCase;
import static org.hamcrest.Matchers.emptyOrNullString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.matchesPattern;
import static org.hamcrest.Matchers.not;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import io.restassured.path.json.JsonPath;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import ru.mts.itmo.aqa.api.support.ApiConfig;
import ru.mts.itmo.aqa.api.support.ApiSpecs;
import ru.mts.itmo.aqa.api.support.ApiTestBase;

class AuthTokenScenarioTest extends ApiTestBase {

    private static final String JWT_PATTERN = "[A-Za-z0-9_-]+\\.[A-Za-z0-9_-]+\\.[A-Za-z0-9_-]+";

    @Test
    @DisplayName("POST /auth/login answers 200 with a non-empty access token")
    void loginIssuesUsableAccessToken() {
        ApiSpecs.rest()
                .body(Map.of("username", ApiConfig.USERNAME, "password", ApiConfig.PASSWORD))
            .when()
                .post("/auth/login")
            .then()
                .statusCode(200)
                .body("access_token", not(emptyOrNullString()))
                .body("access_token", matchesPattern(JWT_PATTERN))
                .body("token_type", equalTo("Bearer"))
                .body("expires_in", greaterThan(0));
    }

    @Test
    @DisplayName("Every login issues a fresh token")
    void everyLoginIssuesAFreshToken() {
        assertNotEquals(
                login().getString("access_token"),
                login().getString("access_token"),
                "the stand must not hand out a cached token");
    }

    @Test
    @DisplayName("The token from /auth/login opens /auth/me and identifies the logged in user")
    void accessTokenOpensTheProtectedProfile() {
        JsonPath login = login();

        ApiSpecs.authorizedWith(login.getString("access_token"))
            .when()
                .get("/auth/me")
            .then()
                .statusCode(200)
                .body("id", not(emptyOrNullString()))
                .body("username", not(emptyOrNullString()))
                .body("email", not(emptyOrNullString()))
                .body("id", equalTo(login.get("user.id")))
                .body("username", equalTo(login.getString("user.username")))
                .body("email", equalTo(login.getString("user.email")));
    }

    @Test
    @DisplayName("GET /auth/me without an Authorization header is refused")
    void requestWithoutAuthorizationHeaderIsRefused() {
        ApiSpecs.rest()
            .when()
                .get("/auth/me")
            .then()
                .statusCode(401)
                .body("status", equalTo(401))
                .body("error", not(emptyOrNullString()))
                .body("message", containsStringIgnoringCase("bearer"));
    }

    @ParameterizedTest(name = "token \"{0}\" is refused")
    @ValueSource(strings = {
            "not-a-token",
            "Basic-not-a-token",
            "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1c2VySWQiOjk5OSwidXNlcm5hbWUiOiJnaG9zdCJ9.forged-signature"
    })
    @DisplayName("A malformed or forged token is refused")
    void invalidAccessTokensAreRefused(String invalidToken) {
        ApiSpecs.authorizedWith(invalidToken)
            .when()
                .get("/auth/me")
            .then()
                .statusCode(401)
                .body("status", equalTo(401))
                .body("error", not(emptyOrNullString()))
                .body("message", containsStringIgnoringCase("bearer"));
    }

    private JsonPath login() {
        return ApiSpecs.rest()
                .body(Map.of("username", ApiConfig.USERNAME, "password", ApiConfig.PASSWORD))
            .when()
                .post("/auth/login")
            .then()
                .statusCode(200)
                .body("access_token", not(emptyOrNullString()))
                .extract()
                .jsonPath();
    }
}
