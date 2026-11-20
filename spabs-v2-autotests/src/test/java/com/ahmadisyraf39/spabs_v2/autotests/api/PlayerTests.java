package com.ahmadisyraf39.spabs_v2.autotests.api;

import io.restassured.http.ContentType;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@Test
public class PlayerTests extends BaseApiTest {

    private static final String PLAYERS_ENDPOINT = "/players";
    private static final String PLAYER_BY_ID_ENDPOINT = "/players/{id}";

    private static final String ADMIN_EMAIL = "admin.sample@spabs.example";
    private static final String ADMIN_PASSWORD = "Password123";

    private String token;

    @BeforeClass
    public void loginAsAdmin() {
        token = loginAndGetToken(ADMIN_EMAIL, ADMIN_PASSWORD);
    }

    public void createPlayer_withValidData_returns201() {
        given()
            .header("Authorization", "Bearer " + token)
            .contentType(ContentType.JSON)
            .body(playerBody("Test Player", "2015-05-10", "MALE"))
        .when()
            .post(PLAYERS_ENDPOINT)
        .then()
            .log().ifValidationFails()
            .statusCode(201)
            .body("id", notNullValue())
            .body("fullName", equalTo("Test Player"))
            .body("dateOfBirth", equalTo("2015-05-10"))
            .body("gender", equalTo("MALE"));
    }

    public void getPlayer_byId_returns200() {
        int id = createPlayer("Get Player", "2014-03-20", "FEMALE");

        given()
            .header("Authorization", "Bearer " + token)
        .when()
            .get(PLAYER_BY_ID_ENDPOINT, id)
        .then()
            .log().ifValidationFails()
            .statusCode(200)
            .body("id", equalTo(id))
            .body("fullName", equalTo("Get Player"))
            .body("gender", equalTo("FEMALE"));
    }

    public void updatePlayer_withNewValues_returns200() {
        int id = createPlayer("Old Name", "2015-05-10", "MALE");

        given()
            .header("Authorization", "Bearer " + token)
            .contentType(ContentType.JSON)
            .body(playerBody("New Name", "2016-01-15", "FEMALE"))
        .when()
            .put(PLAYER_BY_ID_ENDPOINT, id)
        .then()
            .log().ifValidationFails()
            .statusCode(200)
            .body("id", equalTo(id))
            .body("fullName", equalTo("New Name"))
            .body("dateOfBirth", equalTo("2016-01-15"))
            .body("gender", equalTo("FEMALE"));
    }

    public void deletePlayer_thenGet_returns404() {
        int id = createPlayer("To Delete", "2015-05-10", "MALE");

        given()
            .header("Authorization", "Bearer " + token)
        .when()
            .delete(PLAYER_BY_ID_ENDPOINT, id)
        .then()
            .log().ifValidationFails()
            .statusCode(204);

        given()
            .header("Authorization", "Bearer " + token)
        .when()
            .get(PLAYER_BY_ID_ENDPOINT, id)
        .then()
            .log().ifValidationFails()
            .statusCode(404)
            .body("message", equalTo("Player not found: " + id));
    }

    public void createPlayer_withoutFullName_returns400() {
        String body = """
            {
              "dateOfBirth": "2015-05-10",
              "gender": "MALE"
            }
            """;

        given()
            .header("Authorization", "Bearer " + token)
            .contentType(ContentType.JSON)
            .body(body)
        .when()
            .post(PLAYERS_ENDPOINT)
        .then()
            .log().ifValidationFails()
            .statusCode(400)
            .body("message", equalTo("fullName: must not be blank"));
    }

    // Row 29
    public void createPlayer_withoutDateOfBirth_returns400() {
        String body = """
            {
              "fullName": "No Birthday",
              "gender": "MALE"
            }
            """;

        given()
            .header("Authorization", "Bearer " + token)
            .contentType(ContentType.JSON)
            .body(body)
        .when()
            .post(PLAYERS_ENDPOINT)
        .then()
            .log().ifValidationFails()
            .statusCode(400)
            .body("message", equalTo("dateOfBirth: must not be null"));
    }

    // Row 30
    public void createPlayer_withoutToken_returns401() {
        given()
            .contentType(ContentType.JSON)
            .body(playerBody("No Token", "2015-05-10", "MALE"))
        .when()
            .post(PLAYERS_ENDPOINT)
        .then()
            .log().ifValidationFails()
            .statusCode(401);
    }

    // Helpers
    private String playerBody(String fullName, String dateOfBirth, String gender) {
        return """
            {
              "fullName": "%s",
              "dateOfBirth": "%s",
              "gender": "%s"
            }
            """.formatted(fullName, dateOfBirth, gender);
    }

    // Creates a player and returns its id
    private int createPlayer(String fullName, String dateOfBirth, String gender) {
        return given()
                    .header("Authorization", "Bearer " + token)
                    .contentType(ContentType.JSON)
                    .body(playerBody(fullName, dateOfBirth, gender))
                .when()
                    .post(PLAYERS_ENDPOINT)
                .then()
                    .log().ifValidationFails()
                    .statusCode(201)
                    .extract().path("id");
    }

}
