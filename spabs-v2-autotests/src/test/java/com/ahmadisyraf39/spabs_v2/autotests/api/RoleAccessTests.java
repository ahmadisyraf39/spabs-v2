package com.ahmadisyraf39.spabs_v2.autotests.api;

import com.ahmadisyraf39.spabs_v2.autotests.support.TestDataApi;
import io.restassured.specification.RequestSpecification;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

public class RoleAccessTests extends BaseApiTest {

    private static final String PASSWORD = "Password123";

    private static final String VALID_PLAYER_BODY = """
        {"fullName": "Role Test Player", "dateOfBirth": "2015-05-10", "gender": "MALE"}
        """;

    private static final String DUMMY_ATTENDANCE_BODY = """
        {"activityId": 1, "playerId": 1, "status": "PRESENT"}
        """;

    private Map<String, RequestSpecification> specByRole;

    @BeforeClass
    public void loginAllRoles() {
        specByRole = Map.of(
                "ADMIN",  authSpec(loginAndGetToken("admin.sample@spabs.example", PASSWORD)),
                "COACH",  authSpec(loginAndGetToken("coach1@spabs.example", PASSWORD)),
                "PARENT", authSpec(loginAndGetToken("parent1@spabs.example", PASSWORD))
        );
    }

    // one test, many cases
    @DataProvider(name = "roleAccessCases")
    public Object[][] roleAccessCases() {
        return new Object[][] {
                // role,    method, endpoint,        body,                   expected status
                { "COACH",  "POST", "/players",      VALID_PLAYER_BODY,      403 },
                { "PARENT", "POST", "/players",      VALID_PLAYER_BODY,      403 },
                { "PARENT", "POST", "/attendances",  DUMMY_ATTENDANCE_BODY,  403 },
                { "COACH",  "GET",  "/coaches/me",   null,                   200 },
                { "ADMIN",  "GET",  "/coaches/me",   null,                   403 },
                { "PARENT", "GET",  "/admins",       null,                   403 },
        };
    }

    @Test(dataProvider = "roleAccessCases")
    public void role_accessingEndpoint_returnsExpectedStatus(
            String role, String method, String endpoint, String body, int expectedStatus) {

        RequestSpecification request = given().spec(specByRole.get(role));
        if (body != null) {
            request.body(body);
        }

        request
            .when()
                .request(method, endpoint)
            .then()
                .log().ifValidationFails()
                .statusCode(expectedStatus);
    }

    // coach records attendance for their own team
    @Test
    public void coach_recordingAttendanceForOwnTeam_returns201() {
        TestDataApi adminApi = TestDataApi.loginAs("admin.sample@spabs.example", PASSWORD);
        TestDataApi coachApi = TestDataApi.loginAs("coach1@spabs.example", PASSWORD);
        long ts = System.currentTimeMillis();

        int teamId = adminApi.createTeam("Role Team " + ts);
        adminApi.assignCoachToTeam(coachApi.getMyCoachId(), teamId);
        int activityId = adminApi.createActivity(teamId, oneHourAgo());
        int playerId = adminApi.createPlayer("Role Player " + ts);

        String body = """
            {"activityId": %d, "playerId": %d, "status": "PRESENT"}
            """.formatted(activityId, playerId);

        given()
            .spec(specByRole.get("COACH"))
            .body(body)
        .when()
            .post("/attendances")
        .then()
            .log().ifValidationFails()
            .statusCode(201)
            .body("activityId", equalTo(activityId))
            .body("playerId", equalTo(playerId))
            .body("status", equalTo("PRESENT"));
    }

    // Helpers
    private String oneHourAgo() {
        return LocalDateTime.now().minusHours(1)
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss"));
    }
}