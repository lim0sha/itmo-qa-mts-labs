package ru.mts.itmo.aqa.api.rest;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.matchesPattern;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.emptyOrNullString;

import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.mts.itmo.aqa.api.support.ApiSpecs;
import ru.mts.itmo.aqa.api.support.ApiTestBase;

class ChainedPostPatchGetTest extends ApiTestBase {

    private static final int AUTHOR_ID = 1;

    @Test
    @DisplayName("POST /posts answers 201 with a generated id and echoes the submitted payload")
    void postCreatesPostAndReturnsGeneratedId() {
        String title = "created " + UUID.randomUUID();
        String text = "body of the created post";

        ApiSpecs.rest()
                .body(Map.of("title", title, "body", text, "user_id", AUTHOR_ID))
            .when()
                .post("/posts")
            .then()
                .statusCode(201)
                .body("id", matchesPattern("local-[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}"))
                .body("title", equalTo(title))
                .body("body", equalTo(text))
                .body("user_id", equalTo(AUTHOR_ID));
    }

    @Test
    @DisplayName("POST -> extract id -> PATCH -> GET keeps the patched title and the untouched fields")
    void patchedPostIsReadBackWithOnlyTitleChanged() {
        String originalTitle = "original title " + UUID.randomUUID();
        String patchedTitle = "patched title " + UUID.randomUUID();
        String originalBody = "body that the patch must not touch";

        String postId = createPost(originalTitle, originalBody);

        ApiSpecs.rest()
                .pathParam("id", postId)
                .body(Map.of("title", patchedTitle))
            .when()
                .patch("/posts/{id}")
            .then()
                .statusCode(200)
                .body("id", equalTo(postId))
                .body("title", equalTo(patchedTitle));

        ApiSpecs.rest()
                .pathParam("id", postId)
            .when()
                .get("/posts/{id}")
            .then()
                .statusCode(200)
                .body("id", equalTo(postId))
                .body("title", equalTo(patchedTitle))
                .body("body", equalTo(originalBody))
                .body("user_id", equalTo(AUTHOR_ID));
    }

    @Test
    @DisplayName("Two runs of the scenario never receive the same generated id")
    void everyCreatedPostGetsItsOwnId() {
        String firstId = createPost("first " + UUID.randomUUID(), "body");
        String secondId = createPost("second " + UUID.randomUUID(), "body");

        ApiSpecs.rest()
                .pathParam("id", firstId)
            .when()
                .get("/posts/{id}")
            .then()
                .statusCode(200)
                .body("id", not(equalTo(secondId)));

        ApiSpecs.rest()
                .pathParam("id", secondId)
            .when()
                .get("/posts/{id}")
            .then()
                .statusCode(200)
                .body("id", not(emptyOrNullString()));
    }

    private String createPost(String title, String text) {
        return ApiSpecs.rest()
                .body(Map.of("title", title, "body", text, "user_id", AUTHOR_ID))
            .when()
                .post("/posts")
            .then()
                .statusCode(201)
                .body("id", not(emptyOrNullString()))
                .extract()
                .path("id");
    }
}
