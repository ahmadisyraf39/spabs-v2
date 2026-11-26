package com.ahmadisyraf39.spabs_v2.autotests.ui;

import com.ahmadisyraf39.spabs_v2.autotests.config.TestConfig;
import com.ahmadisyraf39.spabs_v2.autotests.pages.Sidebar;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class LogoutUiTests extends BaseUiTest {

    private static final String ADMIN_EMAIL = "admin.sample@spabs.example";
    private static final String TOKEN_KEY = "spabs_token";

    private Sidebar sidebar;

    @BeforeMethod
    public void loginAsAdmin() {
        loginAs(ADMIN_EMAIL, SAMPLE_PASSWORD, "/dashboard/admin");
        sidebar = new Sidebar(driver, wait);
    }

    @Test
    public void logout_redirectsToLoginAndRemovesToken() {
        Assert.assertNotNull(getStoredToken(), "Precondition: a token is stored after login");

        sidebar.logout();

        wait.until(ExpectedConditions.urlContains("/login"));
        Assert.assertNull(getStoredToken(), "The token should be removed on logout");
    }

    @Test
    public void afterLogout_protectedPagesAndBackButtonLeadToLogin() {
        sidebar.logout();
        wait.until(ExpectedConditions.urlContains("/login"));

        driver.get(TestConfig.BASE_URL + "/players");
        wait.until(ExpectedConditions.urlContains("/login"));

        driver.navigate().back();
        wait.until(ExpectedConditions.urlContains("/login"));
        Assert.assertTrue(driver.findElements(By.cssSelector("[data-testid='sidebar-nav']")).isEmpty(),
                "No app pages (sidebar) should be shown after logout");
    }

    private String getStoredToken() {
        return (String) ((JavascriptExecutor) driver)
                .executeScript("return localStorage.getItem(arguments[0]);", TOKEN_KEY);
    }
}