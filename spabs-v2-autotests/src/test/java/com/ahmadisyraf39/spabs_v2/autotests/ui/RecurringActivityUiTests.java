package com.ahmadisyraf39.spabs_v2.autotests.ui;

import com.ahmadisyraf39.spabs_v2.autotests.pages.RecurringActivityFormPage;
import com.ahmadisyraf39.spabs_v2.autotests.support.TestDataApi;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class RecurringActivityUiTests extends BaseUiTest {

    private static final String ADMIN_EMAIL = "admin.sample@spabs.example";

    private static final String START_TIME = "17:00";
    private static final String START_DATE = "2027-01-04";    // Mon 4
    private static final String END_DATE = "2027-01-26";      // Tue

    private TestDataApi adminApi;
    private RecurringActivityFormPage formPage;

    @BeforeClass
    public void createApiClient() {
        adminApi = TestDataApi.loginAs(ADMIN_EMAIL, SAMPLE_PASSWORD);
    }

    @BeforeMethod
    public void createPageAndLoginAsAdmin() {
        formPage = new RecurringActivityFormPage(driver, wait);
        loginAs(ADMIN_EMAIL, SAMPLE_PASSWORD, "/dashboard/admin");
    }

    @Test
    public void recurringForm_withOneTeam_reportsCreatedSessionCount() {
        String teamName = "Recurring UI Team " + System.currentTimeMillis();
        adminApi.createTeam(teamName);

        formPage.open();
        formPage.selectTeam(teamName);
        formPage.selectDayOfWeek("TUESDAY");
        formPage.enterStartTime(START_TIME);
        formPage.enterDateRange(START_DATE, END_DATE);
        formPage.submit();

        // Tuesdays 5, 12, 19, 26 Jan 2027
        Assert.assertEquals(formPage.getSuccessMessage(), "Created 4 training sessions.");
    }

    @Test
    public void recurringForm_withoutTeam_showsSelectTeamError() {
        formPage.open();
        formPage.enterStartTime(START_TIME);
        formPage.enterDateRange(START_DATE, END_DATE);
        formPage.submit();

        Assert.assertEquals(formPage.getErrorMessage(),
                "Select at least one team, or check \"Apply to all teams\".");
    }

    @Test
    public void applyToAllTeams_disablesTeamSelect() {
        formPage.open();
        Assert.assertTrue(formPage.isTeamSelectEnabled(), "Precondition: team list starts enabled");

        formPage.checkApplyToAllTeams();

        Assert.assertFalse(formPage.isTeamSelectEnabled(),
                "Team list should be disabled when 'Apply to all teams' is checked");
    }

}