package com.ahmadisyraf39.spabs_v2.autotests.api;

import com.ahmadisyraf39.spabs_v2.autotests.support.TestDataApi;
import io.restassured.specification.RequestSpecification;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

public class AttendanceTests extends BaseApiTest {

    private static final String ATTENDANCES_ENDPOINT = "/attendances";

    private static final String ADMIN_EMAIL = "admin.sample@spabs.example";
    private static final String COACH_EMAIL = "coach1@spabs.example";
    private static final String PASSWORD = "Password123";

    private static final DateTimeFormatter DATE_TIME = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");

    private RequestSpecification adminSpec;
    private RequestSpecification coachSpec;
    private TestDataApi adminApi;
    private TestDataApi coachApi;

    @BeforeClass
    public void loginUsers() {
        adminSpec = authSpec(loginAndGetToken(ADMIN_EMAIL, PASSWORD));
        coachSpec = authSpec(loginAndGetToken(COACH_EMAIL, PASSWORD));
        adminApi = TestDataApi.loginAs(ADMIN_EMAIL, PASSWORD);
        coachApi = TestDataApi.loginAs(COACH_EMAIL, PASSWORD);
    }

    @Test
    public void recordAttendance_returns201() {
        int teamId = newTeam();
        int activityId = adminApi.createActivity(teamId, hoursAgo(1));
        int playerId = adminApi.createPlayer("Attendance Player " + System.currentTimeMillis());

        given()
            .spec(adminSpec)
            .body(attendanceBody(activityId, playerId, "PRESENT"))
        .when()
            .post(ATTENDANCES_ENDPOINT)
        .then()
            .log().ifValidationFails()
            .statusCode(201)
            .body("id", notNullValue())
            .body("activityId", equalTo(activityId))
            .body("playerId", equalTo(playerId))
            .body("status", equalTo("PRESENT"));
    }

    @Test
    public void recordAttendance_samePlayerAndActivityTwice_returns409() {
        int teamId = newTeam();
        int activityId = adminApi.createActivity(teamId, hoursAgo(1));
        int playerId = adminApi.createPlayer("Attendance Player " + System.currentTimeMillis());
        adminApi.recordAttendance(activityId, playerId, "PRESENT");

        given()
            .spec(adminSpec)
            .body(attendanceBody(activityId, playerId, "LATE"))
        .when()
            .post(ATTENDANCES_ENDPOINT)
        .then()
            .log().ifValidationFails()
            .statusCode(409)
            .body("message", equalTo("Player " + playerId + " already has an attendance record for activity " + activityId));
    }


    //Helpers
    private int newTeam() {
        return adminApi.createTeam("Attendance Team " + System.currentTimeMillis());
    }

    private String hoursAgo(int hours) {
        return LocalDateTime.now().minusHours(hours).format(DATE_TIME);
    }

    private String attendanceBody(int activityId, int playerId, String status) {
        return """
            {"activityId": %d, "playerId": %d, "status": "%s"}
            """.formatted(activityId, playerId, status);
    }

}