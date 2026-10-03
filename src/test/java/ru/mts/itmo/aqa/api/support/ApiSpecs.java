package ru.mts.itmo.aqa.api.support;

import static io.restassured.RestAssured.given;
import static io.restassured.http.ContentType.JSON;

import io.restassured.filter.Filter;
import io.restassured.specification.RequestSpecification;

public final class ApiSpecs {

    private static final Filter SANDBOX_SESSION = new SandboxSessionFilter();
    private static final Filter EXCHANGE_RECORDER = new ExchangeRecorderFilter();

    private ApiSpecs() {
    }

    public static RequestSpecification rest() {
        return given()
                .filters(SANDBOX_SESSION, EXCHANGE_RECORDER)
                .baseUri(ApiConfig.REST_BASE_URI)
                .contentType(JSON)
                .accept(JSON)
                .header("User-Agent", ApiConfig.USER_AGENT);
    }

    public static RequestSpecification graphql() {
        return given()
                .filter(EXCHANGE_RECORDER)
                .baseUri(ApiConfig.GRAPHQL_URI)
                .contentType(JSON)
                .accept(JSON)
                .header("User-Agent", ApiConfig.USER_AGENT);
    }

    public static RequestSpecification authorizedWith(String accessToken) {
        return rest().header("Authorization", "Bearer " + accessToken);
    }
}
