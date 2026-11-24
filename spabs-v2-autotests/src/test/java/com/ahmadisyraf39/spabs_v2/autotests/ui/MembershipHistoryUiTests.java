package com.ahmadisyraf39.spabs_v2.autotests.ui;

import com.ahmadisyraf39.spabs_v2.autotests.pages.TeamRosterPage;
import com.ahmadisyraf39.spabs_v2.autotests.support.TestDataApi;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.time.LocalDate;

public class MembershipHistoryUiTests extends BaseUiTest {

    private static final String ADMIN_EMAIL = "admin.sample@spabs.example";

    private TestDataApi adminApi;

    @BeforeClass
    public void createApiClient() {
        adminApi = TestDataApi.loginAs(ADMIN_EMAIL, SAMPLE_PASSWORD);
    }

    @Test
    public void endStint_rowStaysOnRosterAsInactive() {
        long ts = System.currentTimeMillis();
        String playerName = "Roster UI " + ts;

        int teamId = adminApi.createTeam("Roster UI Team " + ts);
        int playerId = adminApi.createPlayer(playerName);
        int playerTeamId = adminApi.addPlayerToTeam(playerId, teamId, LocalDate.now().minusDays(30).toString());

        loginAs(ADMIN_EMAIL, SAMPLE_PASSWORD, "/dashboard/admin");
        TeamRosterPage rosterPage = new TeamRosterPage(driver, wait);
        rosterPage.open(teamId);

        Assert.assertEquals(rosterPage.getStatusOf(playerName), "ACTIVE", "Precondition: the player starts active");

        rosterPage.endStintFor(playerName);
        rosterPage.waitUntilStatusIs(playerName, "INACTIVE");

        Assert.assertEquals(rosterPage.getStatusOf(playerName), "INACTIVE");
        Assert.assertEquals(rosterPage.getLeftAtOf(playerName), LocalDate.now().toString());

        Assert.assertEquals(adminApi.getPlayerTeamStatus(playerTeamId), "INACTIVE");
    }
}