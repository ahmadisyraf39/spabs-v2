package com.ahmadisyraf39.spabs_v2.autotests.ui;

import com.ahmadisyraf39.spabs_v2.autotests.pages.CoachAttendancePage;
import com.ahmadisyraf39.spabs_v2.autotests.support.TestDataApi;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class AttendanceUiTests extends BaseUiTest {

    private static final String ADMIN_EMAIL = "admin.sample@spabs.example";
    private static final String COACH_EMAIL = "coach1@spabs.example";

    private TestDataApi adminApi;
    private TestDataApi coachApi;

    @BeforeClass
    public void createApiClients() {
        adminApi = TestDataApi.loginAs(ADMIN_EMAIL, SAMPLE_PASSWORD);
        coachApi = TestDataApi.loginAs(COACH_EMAIL, SAMPLE_PASSWORD);
    }

    @Test
    public void coach_marksAttendance_isSaved() {
        long ts = System.currentTimeMillis();
        String playerName = "Attendance UI " + ts;

        // API setup: a team coach1 coaches, a player on its roster, an activity from yesterday
        int teamId = adminApi.createTeam("Attendance UI Team " + ts);
        adminApi.assignCoachToTeam(coachApi.getMyCoachId(), teamId);

        int playerId = adminApi.createPlayer(playerName);
        adminApi.addPlayerToTeam(playerId, teamId, LocalDate.now().minusDays(7).toString());

        String yesterday = LocalDateTime.now().minusDays(1)
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss"));
        int activityId = adminApi.createActivity(teamId, yesterday);

        // UI: the coach marks the player present and saves
        loginAs(COACH_EMAIL, SAMPLE_PASSWORD, "/dashboard/coach");

        CoachAttendancePage attendancePage = new CoachAttendancePage(driver, wait);
        attendancePage.open(activityId);
        attendancePage.setStatus(playerName, "PRESENT");
        attendancePage.save();

        Assert.assertEquals(attendancePage.getSuccessMessage(), "Attendance saved.");
        Assert.assertEquals(adminApi.getAttendanceStatus(activityId, playerId), "PRESENT");
    }
}