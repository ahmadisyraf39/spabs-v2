package com.ahmadisyraf39.spabs_v2.autotests.support;

import com.ahmadisyraf39.spabs_v2.autotests.config.TestConfig;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.specification.RequestSpecification;

import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static io.restassured.RestAssured.given;

// Reads emails caught by Mailpit through its REST API
public final class MailpitApi {

    private static final RequestSpecification SPEC = new RequestSpecBuilder()
            .setBaseUri(TestConfig.MAILPIT_URL)
            .build();

    private static final long TIMEOUT_MS = 10_000;
    private static final long POLL_INTERVAL_MS = 500;
    private static final Pattern RESET_TOKEN = Pattern.compile("reset-password\\?token=([A-Za-z0-9_-]+)");
    private static final Pattern RESET_LINK = Pattern.compile("(https?://\\S+/reset-password\\?token=[A-Za-z0-9_-]+)");

    private MailpitApi() {}

    // Waits until at least `count` emails with this subject reached the address.
    // Returns their plain-text bodies, newest first.
    public static List<String> waitForEmails(String to, String subject, int count) {
        long deadline = System.currentTimeMillis() + TIMEOUT_MS;
        while (System.currentTimeMillis() < deadline) {
            List<String> ids = findMessageIds(to, subject);
            if (ids.size() >= count) {
                return ids.stream().map(MailpitApi::getText).toList();
            }
            pause();
        }
        throw new AssertionError("Expected " + count + " email(s) with subject '" + subject
                + "' to " + to + " within " + TIMEOUT_MS / 1000 + " seconds");
    }

    public static String waitForEmail(String to, String subject) {
        return waitForEmails(to, subject, 1).get(0);
    }

    public static int countEmailsTo(String to) {
        return given()
                    .spec(SPEC)
                    .queryParam("query", "to:" + to)
                .when()
                 .get("/api/v1/search")
                .then()
                    .statusCode(200)
                    .extract().jsonPath().getInt("messages_count");
    }

    public static String extractResetToken(String emailText) {
        Matcher matcher = RESET_TOKEN.matcher(emailText);
        if (!matcher.find()) {
            throw new AssertionError("No reset link found in email:\n" + emailText);
        }
        return matcher.group(1);
    }

    public static String extractResetLink(String emailText) {
        Matcher matcher = RESET_LINK.matcher(emailText);
        if (!matcher.find()) {
            throw new AssertionError("No reset link found in email:\n" + emailText);
        }
        return matcher.group(1);
    }

    // Mailpit's search lists messages newest first
    private static List<String> findMessageIds(String to, String subject) {
        List<Map<String, Object>> messages =
                given()
                    .spec(SPEC)
                    .queryParam("query", "to:" + to)
                .when()
                    .get("/api/v1/search")
                .then()
                    .statusCode(200)
                    .extract().jsonPath().getList("messages");

        return messages.stream()
                .filter(m -> subject.equals(m.get("Subject")))
                .map(m -> (String) m.get("ID"))
                .toList();
    }

    private static String getText(String messageId) {
        return given()
                    .spec(SPEC)
                .when()
                    .get("/api/v1/message/{id}", messageId)
                .then()
                    .statusCode(200)
                    .extract().path("Text");
    }

    private static void pause() {
        try {
            Thread.sleep(POLL_INTERVAL_MS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException(e);
        }
    }
}