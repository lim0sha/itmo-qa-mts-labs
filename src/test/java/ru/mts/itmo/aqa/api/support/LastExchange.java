package ru.mts.itmo.aqa.api.support;

public record LastExchange(
        String method,
        String uri,
        String requestHeaders,
        String requestBody,
        Integer statusCode,
        String responseHeaders,
        String responseBody) {

    public String render() {
        return """
                ----- last HTTP exchange -----
                request:  %s %s
                %s
                %s
                response: %s
                %s
                %s
                ----------------------------""".formatted(
                method,
                uri,
                requestHeaders,
                requestBody == null || requestBody.isBlank() ? "<no request body>" : requestBody,
                statusCode,
                responseHeaders,
                responseBody == null || responseBody.isBlank() ? "<empty response body>" : responseBody);
    }
}
