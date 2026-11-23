package com.ahmadisyraf39.spabs_v2.autotests.api;

import com.ahmadisyraf39.spabs_v2.autotests.support.TestDataApi;
import io.restassured.specification.RequestSpecification;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.util.List;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

public class RecurringActivityTests extends BaseApiTest {

    private static final String RECURRING_ENDPOINT = "/activities/recurring";
    private static final String TEAMS_ENDPOINT = "/teams";

    private static final String ADMIN_EMAIL = "admin.sample@spabs.example";
    private static final String ADMIN_PASSWORD = "Password123";

    private RequestSpecification adminSpec;
    private TestDataApi adminApi;

    @BeforeClass
    public void loginAsAdmin() {
        adminSpec = authSpec(loginAndGetToken(ADMIN_EMAIL, ADMIN_PASSWORD));
        adminApi = TestDataApi.loginAs(ADMIN_EMAIL, ADMIN_PASSWORD);
    }

    @Test
    public void weeklyRule_createsOneSessionPerMatchingDate() {
        int teamId = adminApi.createTeam("Recurring Team " + System.currentTimeMillis());

        // Mon 4 Jan to Tue 26 Jan 2027: Tuesdays are 5, 12, 19, 26 → 4 sessions
        String body = recurringBody("[" + teamId + "]", false, "TUESDAY", "2027-01-04", "2027-01-26");

        given()
            .spec(adminSpec)
            .body(body)
        .when()
            .post(RECURRING_ENDPOINT)
        .then()
            .log().ifValidationFails()
            .statusCode(201)
            .body("size()", equalTo(4))
            .body("teamId", everyItem(equalTo(teamId)));
    }

    @Test
    public void weeklyRule_everySessionFallsOnChosenDayAndTime() {
        int teamId = adminApi.createTeam("Recurring Team " + System.currentTimeMillis());
        String body = recurringBody("[" + teamId + "]", false, "TUESDAY", "2027-01-04", "2027-01-26");

        List<String> startTimes =
                given()
                    .spec(adminSpec)
                    .body(body)
                .when()
                    .post(RECURRING_ENDPOINT)
                .then()
                    .log().ifValidationFails()
                    .statusCode(201)
                    .extract().jsonPath().getList("startAt", String.class);

        Assert.assertEquals(startTimes.size(), 4);
        for (String startAt : startTimes) {
            LocalDateTime dateTime = LocalDateTime.parse(startAt);
            Assert.assertEquals(dateTime.getDayOfWeek(), DayOfWeek.TUESDAY, "Wrong day for " + startAt);
            Assert.assertEquals(dateTime.toLocalTime().toString(), "17:00", "Wrong time for " + startAt);
        }

        // Inclusive range: the first and last Tuesdays are both included
        Assert.assertTrue(startTimes.contains("2027-01-05T17:00:00"), "First Tuesday missing: " + startTimes);
        Assert.assertTrue(startTimes.contains("2027-01-26T17:00:00"), "End date (a Tuesday) missing: " + startTimes);
    }

    // Helpers
    private String recurringBody(String teamIdsJson, boolean applyToAllTeams, String dayOfWeek,
                                 String startDate, String endDate) {
        return """
            {
              "teamIds": %s,
              "applyToAllTeams": %b,
              "type": "TRAINING",
              "title": "Recurring Test Session",
              "dayOfWeek": "%s",
              "startTime": "17:00",
              "startDate": "%s",
              "endDate": "%s"
            }
            """.formatted(teamIdsJson, applyToAllTeams, dayOfWeek, startDate, endDate);
    }
}