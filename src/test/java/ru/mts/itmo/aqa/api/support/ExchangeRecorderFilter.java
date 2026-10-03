package ru.mts.itmo.aqa.api.support;

import io.restassured.filter.Filter;
import io.restassured.filter.FilterContext;
import io.restassured.response.Response;
import io.restassured.specification.FilterableRequestSpecification;
import io.restassured.specification.FilterableResponseSpecification;
import io.restassured.specification.QueryableRequestSpecification;
import java.nio.charset.StandardCharsets;

public class ExchangeRecorderFilter implements Filter {

    private static final ThreadLocal<LastExchange> LAST_EXCHANGE = new ThreadLocal<>();

    @Override
    public Response filter(
            FilterableRequestSpecification requestSpec,
            FilterableResponseSpecification responseSpec,
            FilterContext filterContext) {

        Response response = filterContext.next(requestSpec, responseSpec);
        LAST_EXCHANGE.set(capture(requestSpec, response));
        return response;
    }

    public static LastExchange last() {
        return LAST_EXCHANGE.get();
    }

    public static void reset() {
        LAST_EXCHANGE.remove();
    }

    private static LastExchange capture(FilterableRequestSpecification request, Response response) {
        return new LastExchange(
                request.getMethod(),
                request.getURI(),
                headersOf(request),
                bodyOf(request),
                response.getStatusCode(),
                response.getHeaders().toString(),
                safeBody(response));
    }

    private static String headersOf(QueryableRequestSpecification request) {
        String headers = request.getHeaders().toString();
        return headers.isBlank() ? "<no request headers>" : headers;
    }

    private static String bodyOf(QueryableRequestSpecification request) {
        Object body = request.getBody();
        if (body == null) {
            return null;
        }
        String text = body instanceof byte[] raw
                ? new String(raw, StandardCharsets.UTF_8)
                : String.valueOf(body);
        return text.isBlank() ? null : text;
    }

    private static String safeBody(Response response) {
        try {
            return response.asString();
        } catch (RuntimeException unreadableBody) {
            return "<response body unavailable: " + unreadableBody.getMessage() + ">";
        }
    }
}
