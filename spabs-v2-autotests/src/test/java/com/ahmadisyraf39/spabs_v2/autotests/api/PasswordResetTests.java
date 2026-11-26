package com.ahmadisyraf39.spabs_v2.autotests.api;

import com.ahmadisyraf39.spabs_v2.autotests.support.MailpitApi;
import com.ahmadisyraf39.spabs_v2.autotests.support.TestDataApi;
import io.restassured.http.ContentType;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;


import static io.restassured.RestAssured.given;

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

    private String resetBody(String token, String newPassword) {
        return """
            {"token": "%s", "newPassword": "%s"}
            """.formatted(token, newPassword);
    }

}