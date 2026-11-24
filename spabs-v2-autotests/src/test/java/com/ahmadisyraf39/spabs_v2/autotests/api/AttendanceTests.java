package com.ahmadisyraf39.spabs_v2.autotests.api;

import com.ahmadisyraf39.spabs_v2.autotests.support.TestDataApi;
import io.restassured.specification.RequestSpecification;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static io.restassured.RestAssured.given;
import static io.restassured.RestAssured.withArgs;
import static org.hamcrest.Matchers.*;

public class AttendanceTests extends BaseApiTest {

    private static final String ATTENDANCES_ENDPOINT = "/attendances";
    private static final String BULK_ENDPOINT = "/attendances/bulk";
    private static final String ATTENDANCE_BY_ID_ENDPOINT = "/attendances/{id}";
    private static final String SUMMARY_ENDPOINT = "/attendances/summary";

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

    @Test
    public void bulkAttendance_createsEveryRecord() {
        int teamId = newTeam();
        int activityId = adminApi.createActivity(teamId, hoursAgo(1));
        int player1 = adminApi.createPlayer("Bulk Player A " + System.currentTimeMillis());
        int player2 = adminApi.createPlayer("Bulk Player B " + System.currentTimeMillis());

        given()
            .spec(adminSpec)
            .body(bulkBody(activityId, player1, "PRESENT", player2, "ABSENT"))
        .when()
            .post(BULK_ENDPOINT)
        .then()
            .log().ifValidationFails()
            .statusCode(201)
            .body("size()", equalTo(2))
            .body("find { it.playerId == %d }.status", withArgs(player1), equalTo("PRESENT"))
            .body("find { it.playerId == %d }.status", withArgs(player2), equalTo("ABSENT"));
    }

    // bulk updates existing records instead of duplicating them
    @Test
    public void bulkAttendance_sentTwice_updatesInsteadOfDuplicating() {
        int teamId = newTeam();
        int activityId = adminApi.createActivity(teamId, hoursAgo(1));
        int player1 = adminApi.createPlayer("Bulk Player A " + System.currentTimeMillis());
        int player2 = adminApi.createPlayer("Bulk Player B " + System.currentTimeMillis());

        given().spec(adminSpec).body(bulkBody(activityId, player1, "PRESENT", player2, "ABSENT"))
                .when().post(BULK_ENDPOINT)
                .then().statusCode(201);

        // Same players again, with changed statuses
        given()
            .spec(adminSpec)
            .body(bulkBody(activityId, player1, "LATE", player2, "EXCUSED"))
        .when().
            post(BULK_ENDPOINT)
        .then()
            .statusCode(201);

        given()
            .spec(adminSpec)
            .queryParam("activityId", activityId)
        .when()
            .get(ATTENDANCES_ENDPOINT)
        .then()
            .log().ifValidationFails()
            .statusCode(200)
            .body("size()", equalTo(2))
            .body("find { it.playerId == %d }.status", withArgs(player1), equalTo("LATE"))
            .body("find { it.playerId == %d }.status", withArgs(player2), equalTo("EXCUSED"));
    }

    @Test
    public void coachEditingAttendanceAfterThreeDayWindow_returns403() {
        int teamId = newTeam();
        adminApi.assignCoachToTeam(coachApi.getMyCoachId(), teamId);

        // An activity that started 4 days ago: outside the coach's 3-day window
        int activityId = adminApi.createActivity(teamId, hoursAgo(4 * 24));
        int playerId = adminApi.createPlayer("Window Player " + System.currentTimeMillis());

        // Admins have no window, so the admin can create the record
        int attendanceId = adminApi.recordAttendance(activityId, playerId, "PRESENT");

        given()
            .spec(coachSpec)
            .body(attendanceBody(activityId, playerId, "ABSENT"))
        .when()
            .put(ATTENDANCE_BY_ID_ENDPOINT, attendanceId)
        .then()
            .log().ifValidationFails()
            .statusCode(403)
            .body("message", equalTo("Attendance can only be recorded between the activity start time and 3 days after"));
    }

    @Test
    public void attendanceSummary_countsStatusesAndCalculatesPercentage() {
        int teamId = newTeam();
        int playerId = adminApi.createPlayer("Summary Player " + System.currentTimeMillis());

        // Four sessions, one of each status
        adminApi.recordAttendance(adminApi.createActivity(teamId, hoursAgo(4)), playerId, "PRESENT");
        adminApi.recordAttendance(adminApi.createActivity(teamId, hoursAgo(3)), playerId, "LATE");
        adminApi.recordAttendance(adminApi.createActivity(teamId, hoursAgo(2)), playerId, "ABSENT");
        adminApi.recordAttendance(adminApi.createActivity(teamId, hoursAgo(1)), playerId, "EXCUSED");

        // (present + late) / (total - excused) = 2 / 3 = 66.7%
        given()
            .spec(adminSpec)
            .queryParam("playerId", playerId)
            .queryParam("teamId", teamId)
        .when()
            .get(SUMMARY_ENDPOINT)
        .then()
            .log().ifValidationFails()
            .statusCode(200)
            .body("totalRecords", equalTo(4))
            .body("presentCount", equalTo(1))
            .body("lateCount", equalTo(1))
            .body("absentCount", equalTo(1))
            .body("excusedCount", equalTo(1))
            .body("attendancePercentage", comparesEqualTo(new BigDecimal("66.7")));
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

    private String bulkBody(int activityId, int player1, String status1, int player2, String status2) {
        return """
            {
              "activityId": %d,
              "entries": [
                {"playerId": %d, "status": "%s"},
                {"playerId": %d, "status": "%s"}
              ]
            }
            """.formatted(activityId, player1, status1, player2, status2);
    }

}