package ru.mts.itmo.aqa.api.support;

public final class ApiConfig {

    public static final String REST_BASE_URI = setting(
            "api.rest.baseUri", "API_REST_BASE_URI", "https://playground.nileslabs.com/api/v1");

    public static final String GRAPHQL_URI = setting(
            "api.graphql.uri", "API_GRAPHQL_URI", "https://graphql.postman-echo.com/graphql");

    public static final String USERNAME = setting("api.username", "API_USERNAME", "Bret");

    public static final String PASSWORD = setting("api.password", "API_PASSWORD", "Password@123");

    public static final String USER_AGENT = setting(
            "api.userAgent",
            "API_USER_AGENT",
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 "
                    + "(KHTML, like Gecko) Chrome/125.0.0.0 Safari/537.36");

    private ApiConfig() {
    }

    private static String setting(String systemProperty, String environmentVariable, String fallback) {
        String fromProperty = System.getProperty(systemProperty);
        if (isNotBlank(fromProperty)) {
            return fromProperty.trim();
        }
        String fromEnvironment = System.getenv(environmentVariable);
        if (isNotBlank(fromEnvironment)) {
            return fromEnvironment.trim();
        }
        return fallback;
    }

    private static boolean isNotBlank(String value) {
        return value != null && !value.isBlank();
    }
}
