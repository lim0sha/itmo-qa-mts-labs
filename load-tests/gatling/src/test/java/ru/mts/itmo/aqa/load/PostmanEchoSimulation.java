package ru.mts.itmo.aqa.load;

import static io.gatling.javaapi.core.CoreDsl.csv;
import static io.gatling.javaapi.core.CoreDsl.feed;
import static io.gatling.javaapi.core.CoreDsl.global;
import static io.gatling.javaapi.core.CoreDsl.jsonPath;
import static io.gatling.javaapi.core.CoreDsl.rampUsers;
import static io.gatling.javaapi.core.CoreDsl.scenario;
import static io.gatling.javaapi.http.HttpDsl.http;
import static io.gatling.javaapi.http.HttpDsl.status;

import io.gatling.javaapi.core.FeederBuilder;
import io.gatling.javaapi.core.ScenarioBuilder;
import io.gatling.javaapi.core.Simulation;
import io.gatling.javaapi.http.HttpProtocolBuilder;
import java.time.Duration;

public class PostmanEchoSimulation extends Simulation {

    private static final int USERS = setting("users", "USERS", 10);
    private static final int RAMPUP = setting("rampup", "RAMPUP", USERS);
    private static final int DURATION = setting("duration", "DURATION", 60);
    private static final int THINK_TIME_MS = setting("thinkTime", "THINK_TIME", 1000);

    private static final HttpProtocolBuilder HTTP = http
            .baseUrl("https://postman-echo.com")
            .acceptHeader("application/json")
            .userAgentHeader("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 "
                    + "(KHTML, like Gecko) Chrome/125.0.0.0 Safari/537.36");

    private static final FeederBuilder<String> FEEDER = csv("posts.csv").circular();

    private static final ScenarioBuilder SCENARIO = scenario("Postman Echo browse")
            .forever().on(
                    feed(FEEDER)
                            .exec(http("GET /get - echo query")
                                    .get("/get")
                                    .queryParam("postId", "#{postId}")
                                    .queryParam("limit", "#{pageSize}")
                                    .check(status().is(200))
                                    .check(jsonPath("$.args.postId").isEL("#{postId}"))
                                    .check(jsonPath("$.args.limit").isEL("#{pageSize}")))
                            .pause(Duration.ofMillis(THINK_TIME_MS))
                            .exec(http("GET /headers - echo headers")
                                    .get("/headers")
                                    .header("X-Post-Id", "#{postId}")
                                    .header("X-Page-Size", "#{pageSize}")
                                    .check(status().is(200))
                                    .check(jsonPath("$.headers.x-post-id").isEL("#{postId}"))
                                    .check(jsonPath("$.headers.x-page-size").isEL("#{pageSize}")))
                            .pause(Duration.ofMillis(THINK_TIME_MS)));

    {
        setUp(SCENARIO.injectOpen(rampUsers(USERS).during(RAMPUP)))
                .maxDuration(Duration.ofSeconds(DURATION))
                .assertions(global().failedRequests().count().is(0L))
                .protocols(HTTP);
    }

    private static int setting(String property, String environment, int fallback) {
        String value = System.getProperty(property);
        if (value == null || value.isBlank()) {
            value = System.getenv(environment);
        }
        return value == null || value.isBlank() ? fallback : Integer.parseInt(value);
    }
}
