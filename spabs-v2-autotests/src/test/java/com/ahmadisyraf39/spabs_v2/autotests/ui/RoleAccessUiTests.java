package com.ahmadisyraf39.spabs_v2.autotests.ui;

import com.ahmadisyraf39.spabs_v2.autotests.config.TestConfig;
import com.ahmadisyraf39.spabs_v2.autotests.pages.Sidebar;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.List;

public class RoleAccessUiTests extends BaseUiTest {

    private static final String ADMIN_EMAIL = "admin.sample@spabs.example";
    private static final String COACH_EMAIL = "coach1@spabs.example";
    private static final String PARENT_EMAIL = "parent1@spabs.example";

    // each role lands on its own dashboard
    @DataProvider(name = "roleDashboards")
    public Object[][] roleDashboards() {
        return new Object[][] {
                { ADMIN_EMAIL,  "/dashboard/admin" },
                { COACH_EMAIL,  "/dashboard/coach" },
                { PARENT_EMAIL, "/dashboard/parent" },
        };
    }

    @Test(dataProvider = "roleDashboards")
    public void login_eachRole_landsOnOwnDashboard(String email, String expectedPath) {
        loginAs(email, SAMPLE_PASSWORD, expectedPath);

        String currentUrl = driver.getCurrentUrl();
        Assert.assertTrue(currentUrl.endsWith(expectedPath),
                email + " should land on " + expectedPath + " but was: " + currentUrl);
    }

    @Test
    public void notLoggedIn_openingPlayers_redirectsToLogin() {
        driver.get(TestConfig.BASE_URL + "/players");

        wait.until(ExpectedConditions.urlContains("/login"));

        String currentUrl = driver.getCurrentUrl();
        Assert.assertTrue(currentUrl.endsWith("/login"),
                "Anonymous user opening /players should be sent to /login, but was: " + currentUrl);
    }

    @Test
    public void parent_openingUsersPage_isRedirectedToOwnDashboard() {
        loginAs(PARENT_EMAIL, SAMPLE_PASSWORD, "/dashboard/parent");

        driver.get(TestConfig.BASE_URL + "/users");

        // RequireRole sends unauthorised users back to their own dashboard
        wait.until(ExpectedConditions.urlContains("/dashboard/parent"));

        String currentUrl = driver.getCurrentUrl();
        Assert.assertFalse(currentUrl.contains("/users"),
                "Parent should not stay on /users, but was: " + currentUrl);
    }
    
    @Test
    public void coach_sidebar_showsOnlyCoachPages() {
        loginAs(COACH_EMAIL, SAMPLE_PASSWORD, "/dashboard/coach");

        List<String> paths = new Sidebar(driver, wait).getLinkPaths();

        Assert.assertFalse(paths.isEmpty(), "Coach sidebar should have links");
        for (String path : paths) {
            Assert.assertTrue(path.startsWith("/dashboard/coach"),
                    "Coach sidebar should only link to coach pages, but found: " + path + " in " + paths);
        }
    }


}