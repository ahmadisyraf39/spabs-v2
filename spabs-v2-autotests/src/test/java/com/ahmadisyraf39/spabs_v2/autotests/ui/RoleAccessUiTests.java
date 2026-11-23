package com.ahmadisyraf39.spabs_v2.autotests.ui;

import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

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

}