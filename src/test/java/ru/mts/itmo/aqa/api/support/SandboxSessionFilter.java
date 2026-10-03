package ru.mts.itmo.aqa.api.support;

import io.restassured.filter.Filter;
import io.restassured.filter.FilterContext;
import io.restassured.http.Cookie;
import io.restassured.response.Response;
import io.restassured.specification.FilterableRequestSpecification;
import io.restassured.specification.FilterableResponseSpecification;

public class SandboxSessionFilter implements Filter {

    static final String IDENTITY_COOKIE = "pg_identity";

    private static final ThreadLocal<String> IDENTITY = new ThreadLocal<>();

    @Override
    public Response filter(
            FilterableRequestSpecification requestSpec,
            FilterableResponseSpecification responseSpec,
            FilterContext filterContext) {

        String identity = IDENTITY.get();
        if (identity != null) {
            requestSpec.cookie(IDENTITY_COOKIE, identity);
        }

        Response response = filterContext.next(requestSpec, responseSpec);

        Cookie issued = response.getDetailedCookie(IDENTITY_COOKIE);
        if (issued != null && issued.getValue() != null && !issued.getValue().isBlank()) {
            IDENTITY.set(issued.getValue());
        }
        return response;
    }

    public static void reset() {
        IDENTITY.remove();
    }
}
