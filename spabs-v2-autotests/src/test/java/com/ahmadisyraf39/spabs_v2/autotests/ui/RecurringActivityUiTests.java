package com.ahmadisyraf39.spabs_v2.autotests.ui;

import com.ahmadisyraf39.spabs_v2.autotests.pages.RecurringActivityFormPage;
import com.ahmadisyraf39.spabs_v2.autotests.support.TestDataApi;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class RecurringActivityUiTests extends BaseUiTest {

    private static final String ADMIN_EMAIL = "admin.sample@spabs.example";

    private static final String TYPED_START_TIME = "0500PM";      // 5:00 PM, 12-hour clock
    private static final String TYPED_START_DATE = "04012027";    // Mon 4 Jan 2027 (day-month-year)
    private static final String TYPED_END_DATE = "26012027";      // Tue 26 Jan 2027

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
        formPage.enterStartTime(TYPED_START_TIME);
        formPage.enterDateRange(TYPED_START_DATE, TYPED_END_DATE);
        formPage.submit();

        // Tuesdays 5, 12, 19, 26 Jan 2027
        Assert.assertEquals(formPage.getSuccessMessage(), "Created 4 training sessions.");
    }

    @Test
    public void recurringForm_withoutTeam_showsSelectTeamError() {
        formPage.open();
        formPage.enterStartTime(TYPED_START_TIME);
        formPage.enterDateRange(TYPED_START_DATE, TYPED_END_DATE);
        formPage.submit();

        Assert.assertEquals(formPage.getErrorMessage(),
                "Select at least one team, or check \"Apply to all teams\".");
    }

}