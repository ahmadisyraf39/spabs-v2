package com.ahmadisyraf39.spabs_v2.autotests.ui;

import com.ahmadisyraf39.spabs_v2.autotests.pages.FeeRecordsPage;
import com.ahmadisyraf39.spabs_v2.autotests.pages.LoginPage;
import com.ahmadisyraf39.spabs_v2.autotests.pages.ParentFeesPage;
import com.ahmadisyraf39.spabs_v2.autotests.support.TestDataApi;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.time.LocalDate;

public class FeesUiTests extends BaseUiTest {

    private static final String ADMIN_EMAIL = "admin.sample@spabs.example";
    private static final String PARENT_EMAIL = "parent1@spabs.example";

    private TestDataApi adminApi;

    private FeeRecordsPage feeRecordsPage;
    private ParentFeesPage parentFeesPage;

    @BeforeClass
    public void createApiClient() {
        adminApi = TestDataApi.loginAs(ADMIN_EMAIL, SAMPLE_PASSWORD);
    }

    @BeforeMethod
    public void createPages() {
        feeRecordsPage = new FeeRecordsPage(driver, wait);
        parentFeesPage = new ParentFeesPage(driver, wait);
    }

    @Test
    public void admin_marksFeeAsPaid_statusBecomesPaid() {
        long ts = System.currentTimeMillis();
        String playerName = "Fee UI " + ts;

        // API setup: a team, a player and an unpaid fee record
        int teamId = adminApi.createTeam("Fee UI Team " + ts);
        int playerId = adminApi.createPlayer(playerName);
        int feeItemId = adminApi.createFeeItem("Fee UI Item " + ts, "100.00");
        adminApi.assignFee(playerId, feeItemId, teamId, futureDueDate());

        // UI: the part this test is about
        loginAs(ADMIN_EMAIL, SAMPLE_PASSWORD,"/dashboard/admin");
        feeRecordsPage.open(teamId, playerName);

        Assert.assertEquals(feeRecordsPage.getStatusOf(playerName), "UNPAID",
                "Precondition: the new fee record should start unpaid");

        feeRecordsPage.markPaidFor(playerName);
        feeRecordsPage.waitUntilStatusIs(playerName, "PAID");

        Assert.assertEquals(feeRecordsPage.getStatusOf(playerName), "PAID");
    }

    @Test
    public void parent_seesPaidFee_asPaid() {
        long ts = System.currentTimeMillis();
        String playerName = "Fee UI Child " + ts;

        // API setup: a player linked to parent1, with a fee that's already paid
        int teamId = adminApi.createTeam("Fee UI Team " + ts);
        int playerId = adminApi.createPlayer(playerName);

        int parentId = TestDataApi.loginAs(PARENT_EMAIL, SAMPLE_PASSWORD).getMyParentId();
        adminApi.linkPlayerToParent(playerId, parentId);

        int feeItemId = adminApi.createFeeItem("Fee UI Item " + ts, "100.00");
        int feeRecordId = adminApi.assignFee(playerId, feeItemId, teamId, futureDueDate());
        adminApi.payFee(feeRecordId);

        // UI: what the parent sees
        loginAs(PARENT_EMAIL, SAMPLE_PASSWORD, "/dashboard/parent");
        parentFeesPage.open();
        parentFeesPage.filterByChild(playerName);

        Assert.assertEquals(parentFeesPage.getStatusOf(playerName), "PAID");
    }

    //Helper
    private String futureDueDate() {
        return LocalDate.now().plusDays(30).toString();
    }
}