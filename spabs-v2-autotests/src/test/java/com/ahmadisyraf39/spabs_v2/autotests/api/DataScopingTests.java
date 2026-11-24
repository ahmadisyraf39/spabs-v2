package com.ahmadisyraf39.spabs_v2.autotests.api;

import com.ahmadisyraf39.spabs_v2.autotests.support.TestDataApi;
import io.restassured.specification.RequestSpecification;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

public class DataScopingTests extends BaseApiTest {

    private static final String ADMIN_EMAIL = "admin.sample@spabs.example";
    private static final String PARENT_EMAIL = "parent1@spabs.example";
    private static final String COACH_EMAIL = "coach1@spabs.example";
    private static final String PASSWORD = "Password123";

    private RequestSpecification parentSpec;
    private RequestSpecification coachSpec;
    private TestDataApi adminApi;

    // IDs prepared once in @BeforeClass
    private int ownChildId;
    private int otherChildId;
    private int otherParentId;
    private int ownTeamId;
    private int otherTeamId;
    private int otherCoachId;
    private int unrelatedActivityId;
    private int unrelatedAnnouncementId;

    @BeforeClass
    public void prepareUsersAndData() {
        parentSpec = authSpec(loginAndGetToken(PARENT_EMAIL, PASSWORD));
        coachSpec = authSpec(loginAndGetToken(COACH_EMAIL, PASSWORD));
        adminApi = TestDataApi.loginAs(ADMIN_EMAIL, PASSWORD);
        long ts = System.currentTimeMillis();

        // Parent side: one child linked to parent1, one child that isn't
        int myParentId = TestDataApi.loginAs(PARENT_EMAIL, PASSWORD).getMyParentId();
        ownChildId = adminApi.createPlayer("Scoping Own Child " + ts);
        adminApi.linkPlayerToParent(ownChildId, myParentId);
        otherChildId = adminApi.createPlayer("Scoping Other Child " + ts);
        otherParentId = adminApi.getIds("/parents").stream()
                .filter(id -> id != myParentId).findFirst().orElseThrow();

        // Coach side: a team coach1 coaches, a team they don't, another coach
        int myCoachId = TestDataApi.loginAs(COACH_EMAIL, PASSWORD).getMyCoachId();
        ownTeamId = adminApi.createTeam("Scoping Own Team " + ts);
        adminApi.assignCoachToTeam(myCoachId, ownTeamId);
        otherTeamId = adminApi.createTeam("Scoping Other Team " + ts);
        otherCoachId = adminApi.getIds("/coaches").stream()
                .filter(id -> id != myCoachId).findFirst().orElseThrow();

        // Data for a team neither parent1's children nor coach1 belong to
        String tomorrow = LocalDateTime.now().plusDays(1)
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss"));
        unrelatedActivityId = adminApi.createActivity(otherTeamId, tomorrow);
        unrelatedAnnouncementId = adminApi.createAnnouncement(otherTeamId, "Scoping Unrelated " + ts);
    }

    //Parent
    @Test
    public void parent_viewingOwnChild_returns200() {
        given()
            .spec(parentSpec)
        .when()
            .get("/players/{id}", ownChildId)
        .then()
            .log().ifValidationFails()
            .statusCode(200)
            .body("id", equalTo(ownChildId));
    }

    // Correctly protected endpoints
    @DataProvider(name = "parentForbiddenRequests")
    public Object[][] parentForbiddenRequests() {
        return new Object[][] {
                { "another child's fee records",  "/fee-records?playerId=" + otherChildId },
                { "another parent's activities",  "/activities?parentId=" + otherParentId },
        };
    }

    @Test(dataProvider = "parentForbiddenRequests")
    public void parent_accessingOtherFamiliesData_returns403(String description, String path) {
        assertForbidden(parentSpec, path);
    }

    // Known data exposure bugs: see issues #8 and #9. Remove the group once fixed.
    @DataProvider(name = "parentKnownBugRequests")
    public Object[][] parentKnownBugRequests() {
        return new Object[][] {
                { "another child's profile (#9)",         "/players/" + otherChildId },
                { "another child's attendance (#9)",      "/attendances?playerId=" + otherChildId },
                { "every user's account details (#8)",    "/users" },
                { "every parent's address/contact (#8)",  "/parents" },
        };
    }

    @Test(dataProvider = "parentKnownBugRequests", groups = "known-bugs")
    public void parent_accessingOtherFamiliesData_knownBug_returns403(String description, String path) {
        assertForbidden(parentSpec, path);
    }

    //Coach
    @Test
    public void coach_viewingOwnTeamRoster_returns200() {
        given()
            .spec(coachSpec)
            .queryParam("teamId", ownTeamId)
        .when()
            .get("/player-teams")
        .then()
            .log().ifValidationFails()
            .statusCode(200);
    }

    // Correctly protected: must keep passing
    @DataProvider(name = "coachForbiddenRequests")
    public Object[][] coachForbiddenRequests() {
        return new Object[][] {
                { "another coach's payments", "/coach-payments?coachId=" + otherCoachId },
        };
    }

    @Test(dataProvider = "coachForbiddenRequests")
    public void coach_accessingOtherCoachesData_returns403(String description, String path) {
        assertForbidden(coachSpec, path);
    }

    // Needs a decision: see issue #10
    @DataProvider(name = "coachKnownBugRequests")
    public Object[][] coachKnownBugRequests() {
        return new Object[][] {
                { "roster of a team they don't coach (#10)", "/player-teams?teamId=" + otherTeamId },
        };
    }

    @Test(dataProvider = "coachKnownBugRequests", groups = "known-bugs")
    public void coach_accessingOtherCoachesData_knownBug_returns403(String description, String path) {
        assertForbidden(coachSpec, path);
    }

    //"My" endpoints
    @DataProvider(name = "rolesForMyUpcoming")
    public Object[][] rolesForMyUpcoming() {
        return new Object[][] { { "parent" }, { "coach" } };
    }

    @Test(dataProvider = "rolesForMyUpcoming")
    public void myUpcoming_doesNotIncludeUnrelatedTeamsActivities(String role) {
        RequestSpecification spec = role.equals("parent") ? parentSpec : coachSpec;

        given()
            .spec(spec)
        .when()
            .get("/activities/my-upcoming")
        .then()
            .log().ifValidationFails()
            .statusCode(200)
            .body("id", not(hasItem(unrelatedActivityId)));
    }

    @Test
    public void myAnnouncements_doesNotIncludeUnrelatedTeamsAnnouncements() {
        given()
            .spec(parentSpec)
        .when()
            .get("/announcements/mine")
        .then()
            .log().ifValidationFails()
            .statusCode(200)
            .body("id", not(hasItem(unrelatedAnnouncementId)));
    }

    // Helpers
    private void assertForbidden(RequestSpecification spec, String path) {
        given()
            .spec(spec)
        .when()
            .get(path)
        .then()
            .log().ifValidationFails()
            .statusCode(403);
    }

}