package com.ahmadisyraf39.spabs_v2.autotests.ui;

import com.ahmadisyraf39.spabs_v2.autotests.api.BaseApiTest;
import com.ahmadisyraf39.spabs_v2.autotests.pages.ForgotPasswordPage;
import com.ahmadisyraf39.spabs_v2.autotests.pages.LoginPage;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;
import org.testng.annotations.*;

import java.time.Duration;

public class LoginUiTests extends BaseUiTest {

    private static final String ADMIN_EMAIL = "admin.sample@spabs.example";
    private static final String CORRECT_PASSWORD = "Password123";
    private static final String WRONG_PASSWORD = "WrongPassword123";

    private LoginPage loginPage;

    @BeforeMethod
    public void createPages() {
        loginPage = new LoginPage(driver, wait);
    }

    @Test
    public void loginPage_showsLoginForm() {
        loginPage.open();
        Assert.assertTrue(loginPage.isLoginFormDisplayed(), "Login form (email, password, submit) should be displayed");
    }

    @Test
    public void login_withValidAdmin_redirectsToAdminDashboard() {

        String expectedPath = "/dashboard/admin";

        loginPage.open();
        loginPage.loginAs(ADMIN_EMAIL, CORRECT_PASSWORD);

        wait.until(ExpectedConditions.urlContains(expectedPath));

        String currentUrl = driver.getCurrentUrl();
        Assert.assertTrue(currentUrl.contains(expectedPath),
                "Admin login should land on " + expectedPath + " but was: " + currentUrl);
    }

    @Test
    public void login_withWrongPassword_showsErrorAlert() {

        loginPage.open();
        loginPage.loginAs(ADMIN_EMAIL, WRONG_PASSWORD);

        Assert.assertEquals(loginPage.getErrorMessage(), "Invalid email or password");

        String currentUrl = driver.getCurrentUrl();
        Assert.assertTrue(currentUrl.contains("/login"),
                "Should stay on /login after wrong credentials, but was: " + currentUrl);
    }

    @Test
    public void forgotPasswordLink_opensForgotPasswordPage() {

        loginPage.open();
        loginPage.clickForgotPassword();

        String expectedPath = "/forgot-password";
        wait.until(ExpectedConditions.urlContains(expectedPath));

        String currentUrl = driver.getCurrentUrl();
        Assert.assertTrue(currentUrl.contains(expectedPath),
                "Should navigate to " + expectedPath + " when clicking the link, but was: " + currentUrl);

        ForgotPasswordPage forgotPasswordPage = new ForgotPasswordPage(driver, wait);
        Assert.assertEquals(forgotPasswordPage.getTitle(), "Forgot password");
    }


}
