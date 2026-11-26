package com.ahmadisyraf39.spabs_v2.autotests.ui;

import com.ahmadisyraf39.spabs_v2.autotests.config.TestConfig;
import com.ahmadisyraf39.spabs_v2.autotests.pages.ChangePasswordPage;
import com.ahmadisyraf39.spabs_v2.autotests.support.TestDataApi;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class PasswordUiTests extends BaseUiTest {

    private static final String ADMIN_EMAIL = "admin.sample@spabs.example";
    private static final String TEMP_PASSWORD = "TempPassword1";
    private static final String NEW_PASSWORD = "NewPassword1";

    private TestDataApi adminApi;

    @BeforeClass
    public void createApiClient() {
        adminApi = TestDataApi.loginAs(ADMIN_EMAIL, SAMPLE_PASSWORD);
    }

    // A new user must change their password before using the app
    @Test
    public void firstLogin_forcesPasswordChange_thenOpensDashboard() {
        String email = newUser();

        loginAs(email, TEMP_PASSWORD, "/change-password");

        // Other pages are blocked until the password is changed
        driver.get(TestConfig.BASE_URL + "/dashboard/parent");
        wait.until(ExpectedConditions.urlContains("/change-password"));

        new ChangePasswordPage(driver, wait).changePassword(TEMP_PASSWORD, NEW_PASSWORD);
        wait.until(ExpectedConditions.urlContains("/dashboard/parent"));

        // The new password works for a fresh login (loginAs fails the test if it doesn't)
        TestDataApi.loginAs(email, NEW_PASSWORD);
    }

    private String newUser() {
        String email = "uipw" + System.nanoTime() + "@spabs.example";
        adminApi.createUser(email, TEMP_PASSWORD, "PARENT");
        return email;
    }
}