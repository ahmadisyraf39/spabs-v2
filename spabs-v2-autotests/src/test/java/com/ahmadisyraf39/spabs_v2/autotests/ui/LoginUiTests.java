package com.ahmadisyraf39.spabs_v2.autotests.ui;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;
import org.testng.annotations.*;

import java.time.Duration;

public class LoginUiTests {

    private static final String BASE_URL = "http://localhost:5173";

    private WebDriver driver;
    private WebDriverWait wait;

    @BeforeMethod
    public void setup() {
        driver = new ChromeDriver();
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));
    }

    @Test
    public void loginPage_showsLoginForm() {
        driver.get(BASE_URL + "/login");

        WebElement email = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("[data-testid='login-email']")));

        Assert.assertTrue(email.isDisplayed(), "Email input should be displayed");
    }

    @Test
    public void login_withValidAdmin_redirectsToAdminDashboard() {

        String expectedPath = "/dashboard/admin";

        driver.get(BASE_URL + "/login");

        WebElement email = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("[data-testid='login-email']")));
        email.sendKeys("admin.sample@spabs.example");

        WebElement password = driver.findElement(By.cssSelector("[data-testid='login-password']"));
        password.sendKeys("Password123");

        WebElement loginButton =driver.findElement(By.cssSelector("[data-testid='login-submit']"));
        loginButton.click();

        wait.until(ExpectedConditions.urlContains(expectedPath));

        String currentUrl = driver.getCurrentUrl();
        Assert.assertTrue(currentUrl.contains(expectedPath),
                "Admin login should land on " + expectedPath + " but was: " + currentUrl);
    }

    @Test
    public void login_withWrongPassword_showsErrorAlert() {
        driver.get(BASE_URL + "/login");

        WebElement email = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("[data-testid='login-email']")));
        email.sendKeys("admin.sample@spabs.example");

        WebElement password = driver.findElement(By.cssSelector("[data-testid='login-password']"));
        password.sendKeys("WrongPassword123");

        WebElement loginButton = driver.findElement(By.cssSelector("[data-testid='login-submit']"));
        loginButton.click();

        WebElement errorAlert = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("[data-testid='login-error']")));
        String currentUrl = driver.getCurrentUrl();

        Assert.assertEquals(errorAlert.getText(), "Invalid email or password");
        Assert.assertTrue(currentUrl.contains("/login"),
                "Should stay on /login after wrong credentials, but was: " + currentUrl);
    }

    @AfterMethod
    public void tearDown(){
        if(driver!= null){
            driver.quit();
        }
    }

}
