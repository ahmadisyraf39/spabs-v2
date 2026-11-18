package com.ahmadisyraf39.spabs_v2.autotests.api;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@Test
public class AuthTests {

    private static final String LOGIN_ENDPOINT = "/auth/login";
    private static final String AUTH_ME_ENDPOINT = "/auth/me";

    private static final String VALID_EMAIL = "superadmin@spabs.example";
    private static final String VALID_PASSWORD = "ChangeMe123!";

    @BeforeClass
    public void setup() {
        RestAssured.baseURI = "http://localhost:8080/api/v1";
    }

    // Login Tests
    public void login_withValidCredentials_returnsToken() {

        String body = loginBody(VALID_EMAIL, VALID_PASSWORD);

        given()
            .contentType(ContentType.JSON)
            .body(body)
        .when()
            .post(LOGIN_ENDPOINT)
        .then()
            .log().ifValidationFails()
            .statusCode(200)
            .body("token", notNullValue())
            .body("tokenType", equalTo("Bearer"));

    }

    public void login_withWrongPassword_returns401() {

        String body = loginBody(VALID_EMAIL, "WrongPassword123!");

        given()
            .contentType(ContentType.JSON)
            .body(body)
        .when()
            .post(LOGIN_ENDPOINT)
        .then()
            .log().ifValidationFails()
            .statusCode(401)
            .body("message", equalTo("Invalid email or password"));


    }

    public void login_withNonExistentEmail_returns401() {

        String body = loginBody("nonexistent@spabs.example", VALID_PASSWORD);

        given()
            .contentType(ContentType.JSON)
            .body(body)
        .when()
            .post(LOGIN_ENDPOINT)
        .then()
            .log().ifValidationFails()
            .statusCode(401)
            .body("message", equalTo("Invalid email or password"));

    }

    public void login_withInvalidEmailFormat_returns400() {

        String body = loginBody("invalid-email-format", VALID_PASSWORD);

        given()
            .contentType(ContentType.JSON)
            .body(body)
        .when()
            .post(LOGIN_ENDPOINT)
        .then()
            .log().ifValidationFails()
            .statusCode(400)
            .body("message", equalTo("email: must be a well-formed email address"));

    }

    public void login_withBlankPassword_returns400() {

        String body = loginBody(VALID_EMAIL, "");

        given()
            .contentType(ContentType.JSON)
            .body(body)
        .when()
            .post(LOGIN_ENDPOINT)
        .then()
            .log().ifValidationFails()
            .statusCode(400)
            .body("message", equalTo("password: must not be blank"));

    }

    // Get auth/me tests
    public void me_withValidToken_returnsCurrentUser() {
        String token = loginAndGetToken(VALID_EMAIL, VALID_PASSWORD);

        given()
            .header("Authorization", "Bearer " + token)
        .when()
            .get(AUTH_ME_ENDPOINT)
        .then()
            .log().ifValidationFails()
            .statusCode(200)
            .body("email", equalTo(VALID_EMAIL))
            .body("role", equalTo("SUPER_ADMIN"));

    }

    public void me_withoutToken_returns401() {

        given()
        .when()
            .get(AUTH_ME_ENDPOINT)
        .then()
            .log().ifValidationFails()
            .statusCode(401);

    }

    public void me_withEmptyAuthorizationHeader_returns401() {

        given()
            .header("Authorization", "")
        .when()
            .get(AUTH_ME_ENDPOINT)
        .then()
            .log().ifValidationFails()
            .statusCode(401);

    }

    public void me_withInvalidToken_returns401() {

        given()
            .header("Authorization", "Bearer invalidtoken")
        .when()
            .get(AUTH_ME_ENDPOINT)
        .then()
            .log().ifValidationFails()
            .statusCode(401);

    }

    // Helpers
    private String loginBody(String email, String password) {
        return """
            {
              "email": "%s",
              "password": "%s"
            }
            """.formatted(email, password);
    }

    private String loginAndGetToken(String email, String password) {

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
