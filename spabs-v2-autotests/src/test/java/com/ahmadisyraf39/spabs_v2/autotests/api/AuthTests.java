package com.ahmadisyraf39.spabs_v2.autotests.api;

import com.ahmadisyraf39.spabs_v2.autotests.support.TestDataApi;
import io.restassured.http.ContentType;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@Test
public class AuthTests extends BaseApiTest {

    private static final String AUTH_ME_ENDPOINT = "/auth/me";

    private static final String ADMIN_EMAIL = "admin.sample@spabs.example";
    private static final String ADMIN_PASSWORD = "Password123";

    private static final String TEST_USER_PASSWORD = "TestPass123!";
    private static final String TEST_USER_ROLE = "PARENT";

    private String testUserEmail;

    @BeforeClass
    public void createTestUser() {
        testUserEmail = "auth" + System.currentTimeMillis() + "@spabs.example";

        TestDataApi.loginAs(ADMIN_EMAIL, ADMIN_PASSWORD)
                .createUser(testUserEmail, TEST_USER_PASSWORD, TEST_USER_ROLE);
    }

    // POST /auth/login
    public void login_withValidCredentials_returnsToken() {
        given()
            .contentType(ContentType.JSON)
            .body(loginBody(testUserEmail, TEST_USER_PASSWORD))
        .when()
            .post(LOGIN_ENDPOINT)
        .then()
            .log().ifValidationFails()
            .statusCode(200)
            .body("token", notNullValue())
            .body("tokenType", equalTo("Bearer"));
    }

    public void login_withWrongPassword_returns401() {
        given()
            .contentType(ContentType.JSON)
            .body(loginBody(testUserEmail, "WrongPassword123!"))
        .when()
            .post(LOGIN_ENDPOINT)
        .then()
            .log().ifValidationFails()
            .statusCode(401)
            .body("message", equalTo("Invalid email or password"));
    }

    public void login_withNonExistentEmail_returns401() {
        given()
            .contentType(ContentType.JSON)
            .body(loginBody("nonexistent@spabs.example", TEST_USER_PASSWORD))
        .when()
            .post(LOGIN_ENDPOINT)
        .then()
            .log().ifValidationFails()
            .statusCode(401)
            .body("message", equalTo("Invalid email or password"));
    }

    public void login_withInvalidEmailFormat_returns400() {
        given()
            .contentType(ContentType.JSON)
            .body(loginBody("invalid-email-format", TEST_USER_PASSWORD))
        .when()
            .post(LOGIN_ENDPOINT)
        .then()
            .log().ifValidationFails()
            .statusCode(400)
            .body("message", equalTo("email: must be a well-formed email address"));
    }

    public void login_withBlankPassword_returns400() {
        given()
            .contentType(ContentType.JSON)
            .body(loginBody(testUserEmail, ""))
        .when()
            .post(LOGIN_ENDPOINT)
        .then()
            .log().ifValidationFails()
            .statusCode(400)
            .body("message", equalTo("password: must not be blank"));
    }

    // GET /auth/me
    public void me_withValidToken_returnsCurrentUser() {
        String token = loginAndGetToken(testUserEmail, TEST_USER_PASSWORD);

        given()
            .header("Authorization", "Bearer " + token)
        .when()
            .get(AUTH_ME_ENDPOINT)
        .then()
            .log().ifValidationFails()
            .statusCode(200)
            .body("email", equalTo(testUserEmail))
            .body("role", equalTo(TEST_USER_ROLE))
            .body("mustChangePassword", equalTo(true));
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
}