package com.ahmadisyraf39.spabs_v2.autotests.api;

import com.ahmadisyraf39.spabs_v2.autotests.support.TestDataApi;
import io.restassured.specification.RequestSpecification;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.time.LocalDate;

import static io.restassured.RestAssured.given;
import static io.restassured.RestAssured.withArgs;
import static org.hamcrest.Matchers.*;

public class MembershipHistoryTests extends BaseApiTest {

    private static final String PLAYER_TEAMS_ENDPOINT = "/player-teams";
    private static final String PLAYER_TEAM_BY_ID_ENDPOINT = "/player-teams/{id}";

    private static final String ADMIN_EMAIL = "admin.sample@spabs.example";
    private static final String PASSWORD = "Password123";

    private RequestSpecification adminSpec;
    private TestDataApi adminApi;

    @BeforeClass
    public void loginAsAdmin() {
        adminSpec = authSpec(loginAndGetToken(ADMIN_EMAIL, PASSWORD));
        adminApi = TestDataApi.loginAs(ADMIN_EMAIL, PASSWORD);
    }

    //Player history

    // setting status to INACTIVE keeps the record
    @Test
    public void endStint_keepsRecordWithInactiveStatusAndLeaveDate() {
        int teamId = newTeam();
        int playerId = newPlayer();
        String joinedAt = daysAgo(30);
        int playerTeamId = adminApi.addPlayerToTeam(playerId, teamId, 10, joinedAt);

        // "End stint": PUT the whole record with status INACTIVE and a leave date
        given()
            .spec(adminSpec)
            .body(playerTeamBody(playerId, teamId, 10, "INACTIVE", joinedAt, today()))
        .when()
            .put(PLAYER_TEAM_BY_ID_ENDPOINT, playerTeamId)
        .then()
            .log().ifValidationFails()
            .statusCode(200);

        // The record still exists, now inactive, with its history intact
        given()
            .spec(adminSpec)
        .when()
            .get(PLAYER_TEAM_BY_ID_ENDPOINT, playerTeamId)
        .then()
            .log().ifValidationFails()
            .statusCode(200)
            .body("status", equalTo("INACTIVE"))
            .body("leftAt", equalTo(today()))
            .body("joinedAt", equalTo(joinedAt))
            .body("jerseyNumber", equalTo(10));
    }

    // after moving teams, history lists old and new team
    @Test
    public void movingTeams_playerHistoryListsOldAndNewTeam() {
        int oldTeamId = newTeam();
        int newTeamId = newTeam();
        int playerId = newPlayer();

        String joinedOld = daysAgo(60);
        int oldStintId = adminApi.addPlayerToTeam(playerId, oldTeamId, joinedOld);

        // Leave the old team, then join the new one
        given()
            .spec(adminSpec)
            .body(playerTeamBody(playerId, oldTeamId, null, "INACTIVE", joinedOld, today()))
        .when()
            .put(PLAYER_TEAM_BY_ID_ENDPOINT, oldStintId)
        .then()
            .statusCode(200);

        adminApi.addPlayerToTeam(playerId, newTeamId, today());

        given()
            .spec(adminSpec)
            .queryParam("playerId", playerId)
        .when()
            .get(PLAYER_TEAMS_ENDPOINT)
        .then()
            .log().ifValidationFails()
            .statusCode(200)
            .body("size()", equalTo(2))
            .body("find { it.teamId == %d }.status", withArgs(oldTeamId), equalTo("INACTIVE"))
            .body("find { it.teamId == %d }.leftAt", withArgs(oldTeamId), equalTo(today()))
            .body("find { it.teamId == %d }.status", withArgs(newTeamId), equalTo("ACTIVE"));
    }


    // Helpers
    private int newTeam() {
        return adminApi.createTeam("History Team " + System.nanoTime());
    }

    private int newPlayer() {
        return adminApi.createPlayer("History Player " + System.nanoTime());
    }

    private String today() {
        return LocalDate.now().toString();
    }

    private String daysAgo(int days) {
        return LocalDate.now().minusDays(days).toString();
    }

    // Full PlayerTeam body for PUT/POST. leftAt and jerseyNumber may be null
    private String playerTeamBody(int playerId, int teamId, Integer jerseyNumber, String status,
                                  String joinedAt, String leftAt) {
        return """
            {"playerId": %d, "teamId": %d, "jerseyNumber": %s, "status": "%s",
             "joinedAt": "%s", "leftAt": %s}
            """.formatted(playerId, teamId, jerseyNumber, status, joinedAt,
                leftAt == null ? "null" : "\"" + leftAt + "\"");
    }
}