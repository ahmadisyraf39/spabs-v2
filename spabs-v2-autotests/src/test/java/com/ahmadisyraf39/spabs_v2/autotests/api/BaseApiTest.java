package com.ahmadisyraf39.spabs_v2.autotests.api;

import io.restassured.RestAssured;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.config.JsonConfig;
import io.restassured.config.RestAssuredConfig;
import io.restassured.http.ContentType;
import io.restassured.path.json.config.JsonPathConfig;
import io.restassured.specification.RequestSpecification;
import org.testng.annotations.BeforeClass;

import static io.restassured.RestAssured.given;

public abstract class BaseApiTest {

    protected static final String LOGIN_ENDPOINT = "/auth/login";

    @BeforeClass
    public void configureRestAssured() {
        RestAssured.baseURI = "http://localhost:8080/api/v1";

        RestAssured.config = RestAssuredConfig.config().jsonConfig(
                JsonConfig.jsonConfig().numberReturnType(JsonPathConfig.NumberReturnType.BIG_DECIMAL));
    }

    protected RequestSpecification authSpec(String token) {
        return new RequestSpecBuilder()
                .addHeader("Authorization", "Bearer " + token)
                .setContentType(ContentType.JSON)
                .build();
    }

    protected String loginBody(String email, String password) {
        return """
            {
              "email": "%s",
              "password": "%s"
            }
            """.formatted(email, password);
    }

    protected String loginAndGetToken(String email, String password) {

        String body = loginBody(email, password);

        return given()
                    .contentType(ContentType.JSON)
                    .body(body)
                .when()
                    .post(LOGIN_ENDPOINT)
                .then()
                    .log().ifValidationFails()
                    .statusCode(200)
                    .extract()
                    .path("token");

    }

}
