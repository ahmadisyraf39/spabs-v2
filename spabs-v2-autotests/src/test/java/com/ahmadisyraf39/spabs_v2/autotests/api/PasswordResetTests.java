package com.ahmadisyraf39.spabs_v2.autotests.api;

import com.ahmadisyraf39.spabs_v2.autotests.support.MailpitApi;
import com.ahmadisyraf39.spabs_v2.autotests.support.TestDataApi;
import io.restassured.http.ContentType;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;


import java.util.List;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

public class PasswordResetTests extends BaseApiTest {

    private static final String FORGOT_PASSWORD_ENDPOINT = "/auth/forgot-password";
    private static final String RESET_PASSWORD_ENDPOINT = "/auth/reset-password";
    private static final String CHANGE_PASSWORD_ENDPOINT = "/auth/change-password";
    private static final String ME_ENDPOINT = "/auth/me";

    private static final String RESET_EMAIL_SUBJECT = "Password reset request";
    private static final String INVALID_TOKEN_MESSAGE = "Invalid or expired reset token";

    private static final String ADMIN_EMAIL = "admin.sample@spabs.example";
    private static final String ADMIN_PASSWORD = "Password123";
    private static final String OLD_PASSWORD = "OldPassword1";
    private static final String NEW_PASSWORD = "NewPassword1";

    private TestDataApi adminApi;

    @BeforeClass
    public void createAdminClient() {
        adminApi = TestDataApi.loginAs(ADMIN_EMAIL, ADMIN_PASSWORD);
    }

    // Forgot password

    // Known email returns 204 and a reset link is emailed
    @Test
    public void forgotPassword_knownEmail_returns204AndEmailsResetLink() {
        String email = newUser();

        requestReset(email);

        String emailText = MailpitApi.waitForEmail(email, RESET_EMAIL_SUBJECT);
        Assert.assertTrue(emailText.contains("http://localhost:5173/reset-password?token="),
                "Email should contain the reset link. Email was:\n" + emailText);
    }

    // Unknown email gets the same 204 and no email, so it doesn't reveal which emails exist
    @Test
    public void forgotPassword_unknownEmail_returns204AndSendsNothing() {
        String unknownEmail = "nobody" + System.nanoTime() + "@spabs.example";
        String knownEmail = newUser();

        requestReset(unknownEmail);
        requestReset(knownEmail);

        // Control: once the known user's email has arrived, the unknown one would have arrived too
        MailpitApi.waitForEmail(knownEmail, RESET_EMAIL_SUBJECT);
        Assert.assertEquals(MailpitApi.countEmailsTo(unknownEmail), 0,
                "No email should be sent to an address that has no account");
    }

    // Reset password

    // Invalid token returns 400
    @Test
    public void resetPassword_invalidToken_returns400() {
        given()
            .contentType(ContentType.JSON)
            .body(resetBody("not-a-real-token", NEW_PASSWORD))
        .when()
            .post(RESET_PASSWORD_ENDPOINT)
        .then()
            .log().ifValidationFails()
            .statusCode(400)
            .body("message", equalTo(INVALID_TOKEN_MESSAGE));
    }

    // Full flow: forgot → token from the email → reset → only the new password works
    @Test
    public void resetPassword_withEmailedToken_replacesThePassword() {
        String email = newUser();
        String token = requestResetAndGetToken(email);

        given()
            .contentType(ContentType.JSON)
            .body(resetBody(token, NEW_PASSWORD))
        .when()
            .post(RESET_PASSWORD_ENDPOINT)
        .then()
            .log().ifValidationFails()
            .statusCode(204);

        String newToken = loginAndGetToken(email, NEW_PASSWORD);
        assertLoginFails(email, OLD_PASSWORD);

        // A reset also clears the "must change password" flag
        given()
            .spec(authSpec(newToken))
        .when()
            .get(ME_ENDPOINT)
        .then()
            .statusCode(200)
            .body("mustChangePassword", equalTo(false));
    }

    @Test
    public void resetToken_canOnlyBeUsedOnce() {
        String email = newUser();
        String token = requestResetAndGetToken(email);

        resetPassword(token, NEW_PASSWORD, 204);
        resetPassword(token, "AnotherPassword1", 400);
    }

    // Known bug: a second reset request fails with 401 (see issue #18). Remove the group once fixed.
    @Test(groups = "known-bugs")
    public void newResetRequest_invalidatesTheEarlierToken() {
        String email = newUser();
        requestReset(email);
        requestReset(email);

        List<String> emails = MailpitApi.waitForEmails(email, RESET_EMAIL_SUBJECT, 2);
        String newestToken = MailpitApi.extractResetToken(emails.get(0));
        String earlierToken = MailpitApi.extractResetToken(emails.get(1));

        resetPassword(earlierToken, NEW_PASSWORD, 400);
        resetPassword(newestToken, NEW_PASSWORD, 204);
    }

    // Change password

    // Wrong current password returns 400
    @Test
    public void changePassword_wrongCurrentPassword_returns400() {
        String email = newUser();

        given()
            .spec(authSpec(loginAndGetToken(email, OLD_PASSWORD)))
            .body(changeBody("WrongPassword1", NEW_PASSWORD))
        .when()
            .put(CHANGE_PASSWORD_ENDPOINT)
        .then()
            .log().ifValidationFails()
            .statusCode(400)
            .body("message", equalTo("Current password is incorrect"));
    }

    // Change password, then the new password works and the old one doesn't
    @Test
    public void changePassword_thenLoginWithNewPassword_works() {
        String email = newUser();

        given()
            .spec(authSpec(loginAndGetToken(email, OLD_PASSWORD)))
            .body(changeBody(OLD_PASSWORD, NEW_PASSWORD))
        .when()
            .put(CHANGE_PASSWORD_ENDPOINT)
        .then()
            .log().ifValidationFails()
            .statusCode(204);

        loginAndGetToken(email, NEW_PASSWORD);
        assertLoginFails(email, OLD_PASSWORD);
    }

    // Known bug: change-password accepts short passwords (see issue #19). Remove the group once fixed.
    @Test(groups = "known-bugs")
    public void changePassword_tooShortNewPassword_returns400() {
        String email = newUser();

        given()
            .spec(authSpec(loginAndGetToken(email, OLD_PASSWORD)))
            .body(changeBody(OLD_PASSWORD, "abc"))
        .when()
            .put(CHANGE_PASSWORD_ENDPOINT)
        .then()
            .log().ifValidationFails()
            .statusCode(400);
    }

    // Helpers
    private String newUser() {
        String email = "reset" + System.nanoTime() + "@spabs.example";
        adminApi.createUser(email, OLD_PASSWORD, "PARENT");
        return email;
    }

    private void requestReset(String email) {
        given()
                .contentType(ContentType.JSON)
                .body("""
                {"email": "%s"}
                """.formatted(email))
                .when()
                .post(FORGOT_PASSWORD_ENDPOINT)
                .then()
                .log().ifValidationFails()
                .statusCode(204);
    }

    private String requestResetAndGetToken(String email) {
        requestReset(email);
        return MailpitApi.extractResetToken(MailpitApi.waitForEmail(email, RESET_EMAIL_SUBJECT));
    }

    private void resetPassword(String token, String newPassword, int expectedStatus) {
        given()
                .contentType(ContentType.JSON)
                .body(resetBody(token, newPassword))
                .when()
                .post(RESET_PASSWORD_ENDPOINT)
                .then()
                .log().ifValidationFails()
                .statusCode(expectedStatus);
    }

    private void assertLoginFails(String email, String password) {
        given()
                .contentType(ContentType.JSON)
                .body(loginBody(email, password))
                .when()
                .post(LOGIN_ENDPOINT)
                .then()
                .log().ifValidationFails()
                .statusCode(401);
    }

    private String resetBody(String token, String newPassword) {
        return """
            {"token": "%s", "newPassword": "%s"}
            """.formatted(token, newPassword);
    }

    private String changeBody(String currentPassword, String newPassword) {
        return """
            {"currentPassword": "%s", "newPassword": "%s"}
            """.formatted(currentPassword, newPassword);
    }

}