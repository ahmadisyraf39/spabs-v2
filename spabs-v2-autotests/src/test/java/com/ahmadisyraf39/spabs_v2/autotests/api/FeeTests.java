package com.ahmadisyraf39.spabs_v2.autotests.api;

import io.restassured.specification.RequestSpecification;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@Test
public class FeeTests extends BaseApiTest {

    private static final String FEE_ITEMS_ENDPOINT = "/fee-items";
    private static final String FEE_RECORDS_ENDPOINT = "/fee-records";
    private static final String PAY_FEE_ENDPOINT = "/fee-records/{id}/pay";
    private static final String PLAYERS_ENDPOINT = "/players";
    private static final String TEAMS_ENDPOINT = "/teams";
    private static final String TEAM_FEE_SUMMARY_ENDPOINT = "/fee-records/team/{teamId}/summary";

    private static final String ADMIN_EMAIL = "admin.sample@spabs.example";
    private static final String ADMIN_PASSWORD = "Password123";

    private RequestSpecification adminSpec;

    @BeforeClass
    public void loginAsAdmin() {
        adminSpec = authSpec(loginAndGetToken(ADMIN_EMAIL, ADMIN_PASSWORD));
    }

    public void createFeeItem_withValidData_returns201() {
        String body = feeItemBody("Monthly Fee Test", "MONTHLY", "150.00");

        given()
            .spec(adminSpec)
            .body(body)
        .when()
            .post(FEE_ITEMS_ENDPOINT)
        .then()
            .log().ifValidationFails()
            .statusCode(201)
            .body("id", notNullValue())
            .body("name", equalTo("Monthly Fee Test"))
            .body("feeType", equalTo("MONTHLY"))
            .body("amount", comparesEqualTo(new BigDecimal("150")));
    }

    public void assignFee_toPlayer_returns201WithUnpaidStatus() {
        int playerId = createPlayer();
        int teamId = createTeam();
        int feeItemId = createFeeItem("80.00");
        String dueDate = futureDueDate();

        String body = feeRecordBody(playerId, feeItemId, teamId, dueDate);

        given()
            .spec(adminSpec)
            .body(body)
        .when()
            .post(FEE_RECORDS_ENDPOINT)
        .then()
            .log().ifValidationFails()
            .statusCode(201)
            .body("id", notNullValue())
            .body("playerId", equalTo(playerId))
            .body("feeItemId", equalTo(feeItemId))
            .body("teamId", equalTo(teamId))
            .body("dueDate", equalTo(dueDate))
            .body("status", equalTo("UNPAID"))
            .body("amount", comparesEqualTo(new BigDecimal("80")));
    }

    public void payFee_unpaidRecord_marksAsPaid() {
        int feeRecordId = createUnpaidFeeRecord();

        given()
            .spec(adminSpec)
        .when()
            .post(PAY_FEE_ENDPOINT, feeRecordId)
        .then()
            .log().ifValidationFails()
            .statusCode(200)
            .body("id", equalTo(feeRecordId))
            .body("status", equalTo("PAID"))
            .body("paidAt", notNullValue());
    }

    public void payFee_alreadyPaidRecord_returns400() {
        int feeRecordId = createUnpaidFeeRecord();

        // First payment succeeds
        given()
            .spec(adminSpec)
        .when()
            .post(PAY_FEE_ENDPOINT, feeRecordId)
        .then()
            .log().ifValidationFails()
            .statusCode(200);

        // Second payment is rejected
        given()
            .spec(adminSpec)
        .when()
            .post(PAY_FEE_ENDPOINT, feeRecordId)
        .then()
            .log().ifValidationFails()
            .statusCode(400)
            .body("message", equalTo("FeeRecord " + feeRecordId + " is already paid"));
    }

    public void teamFeeSummary_withPaidAndUnpaidRecords_returnsCorrectTotals() {
        int teamId = createTeam();

        int paidRecordId = assignFee(createPlayer(), createFeeItem("100.00"), teamId);
        assignFee(createPlayer(), createFeeItem("50.00"), teamId);   // stays unpaid

        payFee(paidRecordId);

        given()
            .spec(adminSpec)
        .when()
            .get(TEAM_FEE_SUMMARY_ENDPOINT, teamId)
        .then()
            .log().ifValidationFails()
            .statusCode(200)
            .body("teamId", equalTo(teamId))
            .body("recordCount", equalTo(2))
            .body("totalAmount", comparesEqualTo(new BigDecimal("150")))
            .body("totalPaid", comparesEqualTo(new BigDecimal("100")))
            .body("totalOutstanding", comparesEqualTo(new BigDecimal("50")));
    }

    // Helpers
    private int assignFee(int playerId, int feeItemId, int teamId) {
        return createAndGetId(FEE_RECORDS_ENDPOINT, feeRecordBody(playerId, feeItemId, teamId, futureDueDate()));
    }

    private void payFee(int feeRecordId) {
        given()
                .spec(adminSpec)
                .when()
                .post(PAY_FEE_ENDPOINT, feeRecordId)
                .then()
                .log().ifValidationFails()
                .statusCode(200);
    }

    private int createUnpaidFeeRecord() {
        return assignFee(createPlayer(), createFeeItem("100.00"), createTeam());
    }

    private int createPlayer() {
        String body = """
            {
              "fullName": "Fee Player %d",
              "dateOfBirth": "2015-05-10",
              "gender": "MALE"
            }
            """.formatted(System.currentTimeMillis());

        return createAndGetId(PLAYERS_ENDPOINT, body);
    }

    private int createTeam() {
        String body = """
            {
              "name": "Fee Team %d",
              "category": "BOYS",
              "ageGroup": "U12"
            }
            """.formatted(System.currentTimeMillis());

        return createAndGetId(TEAMS_ENDPOINT, body);
    }

    private int createFeeItem(String amount) {
        return createAndGetId(FEE_ITEMS_ENDPOINT, feeItemBody("Fee Item " + System.currentTimeMillis(), "MONTHLY", amount));
    }

    private int createAndGetId(String endpoint, String body) {
        return given()
                    .spec(adminSpec)
                    .body(body)
                .when()
                    .post(endpoint)
                .then()
                    .log().ifValidationFails()
                    .statusCode(201)
                    .extract().path("id");
    }

    private String feeItemBody(String name, String feeType, String amount) {
        return """
            {
              "name": "%s",
              "feeType": "%s",
              "amount": %s
            }
            """.formatted(name, feeType, amount);
    }

    private String feeRecordBody(int playerId, int feeItemId, int teamId, String dueDate) {
        return """
            {
              "playerId": %d,
              "feeItemId": %d,
              "teamId": %d,
              "dueDate": "%s"
            }
            """.formatted(playerId, feeItemId, teamId, dueDate);
    }

    private String futureDueDate() {
        return LocalDate.now().plusDays(30).toString();
    }
}