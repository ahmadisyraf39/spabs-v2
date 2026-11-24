package com.ahmadisyraf39.spabs_v2.autotests.support;

import com.ahmadisyraf39.spabs_v2.autotests.config.TestConfig;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;

import static io.restassured.RestAssured.given;

// Creates test data through the API, so UI tests only test the UI part
public class TestDataApi {

    private final RequestSpecification spec;

    private TestDataApi(String token) {
        this.spec = new RequestSpecBuilder()
                            .setBaseUri(TestConfig.API_BASE_URL)
                            .addHeader("Authorization", "Bearer " + token)
                            .setContentType(ContentType.JSON)
                            .build();
    }

    // Log in and get a client that acts as that user
    public static TestDataApi loginAs(String email, String password) {
        String body = """
            {
              "email": "%s",
              "password": "%s"
            }
            """.formatted(email, password);

        String token = given()
                            .baseUri(TestConfig.API_BASE_URL)
                            .contentType(ContentType.JSON)
                            .body(body)
                        .when()
                            .post("/auth/login")
                        .then()
                            .statusCode(200)
                            .extract().path("token");

        return new TestDataApi(token);
    }

    // New users always start with mustChangePassword = true
    public void createUser(String email, String password, String role) {
        post("/users", """
        {"email": "%s", "password": "%s", "fullName": "Test User", "role": "%s", "active": true}
        """.formatted(email, password, role));
    }

    public int createTeam(String name) {
        return post("/teams", """
            {"name": "%s", "category": "BOYS", "ageGroup": "U12"}
            """.formatted(name));
    }

    public int createPlayer(String fullName) {
        return post("/players", """
            {"fullName": "%s", "dateOfBirth": "2015-05-10", "gender": "MALE"}
            """.formatted(fullName));
    }

    public int createFeeItem(String name, String amount) {
        return post("/fee-items", """
            {"name": "%s", "feeType": "MONTHLY", "amount": %s}
            """.formatted(name, amount));
    }

    public int assignFee(int playerId, int feeItemId, int teamId, String dueDate) {
        return post("/fee-records", """
            {"playerId": %d, "feeItemId": %d, "teamId": %d, "dueDate": "%s"}
            """.formatted(playerId, feeItemId, teamId, dueDate));
    }

    public void payFee(int feeRecordId) {
        given()
            .spec(spec)
        .when()
            .post("/fee-records/{id}/pay", feeRecordId)
        .then()
            .statusCode(200);
    }

    // Only works when this client is logged in as a parent
    public int getMyParentId() {
        return given()
                    .spec(spec)
                .when()
                    .get("/parents/me")
                .then()
                    .statusCode(200)
                    .extract().path("id");
    }

    public void linkPlayerToParent(int playerId, int parentId) {
        post("/player-parents", """
            {"playerId": %d, "parentId": %d, "relationship": "GUARDIAN"}
            """.formatted(playerId, parentId));
    }

    // POST a body, check 201, return the new id
    private int post(String endpoint, String body) {
        return given()
                    .spec(spec)
                    .body(body)
                .when()
                    .post(endpoint)
                .then()
                    .log().ifValidationFails()
                    .statusCode(201)
                    .extract().path("id");
    }

    public int createActivity(int teamId, String startAt) {
        return post("/activities", """
        {"teamId": %d, "type": "TRAINING", "title": "Role Test Session", "startAt": "%s"}
        """.formatted(teamId, startAt));
    }

    // Only works when this client is logged in as a coach
    public int getMyCoachId() {
        return given()
                .spec(spec)
                .when()
                .get("/coaches/me")
                .then()
                .statusCode(200)
                .extract().path("id");
    }

    public void assignCoachToTeam(int coachId, int teamId) {
        post("/coach-teams", """
        {"coachId": %d, "teamId": %d, "role": "HEAD_COACH", "status": "ACTIVE", "joinedAt": "%s"}
        """.formatted(coachId, teamId, java.time.LocalDate.now()));
    }

    public void addPlayerToTeam(int playerId, int teamId, String joinedAt) {
        post("/player-teams", """
        {"playerId": %d, "teamId": %d, "status": "ACTIVE", "joinedAt": "%s"}
        """.formatted(playerId, teamId, joinedAt));
    }

    public int recordAttendance(int activityId, int playerId, String status) {
        return post("/attendances", """
        {"activityId": %d, "playerId": %d, "status": "%s"}
        """.formatted(activityId, playerId, status));
    }

}