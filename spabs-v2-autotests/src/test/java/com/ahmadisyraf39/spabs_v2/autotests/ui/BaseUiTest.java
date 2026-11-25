package com.ahmadisyraf39.spabs_v2.autotests.ui;

import com.ahmadisyraf39.spabs_v2.autotests.config.TestConfig;
import com.ahmadisyraf39.spabs_v2.autotests.pages.LoginPage;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.ITestResult;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.Duration;

public abstract class BaseUiTest {

    protected WebDriver driver;
    protected WebDriverWait wait;

    protected static final String SAMPLE_PASSWORD = "Password123";

    @BeforeMethod
    public void startBrowser() {
        ChromeOptions options = new ChromeOptions();
        if (TestConfig.HEADLESS) {
            options.addArguments("--headless=new");
        }
        driver = new ChromeDriver(options);
        driver.manage().window().setSize(new Dimension(1366, 768));
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));
    }

    @AfterMethod(alwaysRun = true)
    public void quitBrowser(ITestResult result) {
        if (driver == null) {
            return;
        }
        try {
            if (result.getStatus() == ITestResult.FAILURE) {
                saveScreenshot(result);
            }
        } finally {
            driver.quit();
        }
    }

    protected void loginAs(String email, String password, String expectedPath) {
        LoginPage loginPage = new LoginPage(driver, wait);
        loginPage.open();
        loginPage.loginAs(email, password);
        wait.until(ExpectedConditions.urlContains(expectedPath));
    }

    private void saveScreenshot(ITestResult result) {
        try {
            File screenshot = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
            String fileName = result.getTestClass().getRealClass().getSimpleName()
                    + "_" + result.getMethod().getMethodName()
                    + "_" + System.currentTimeMillis() + ".png";
            Path target = Path.of("target", "screenshots", fileName);
            Files.createDirectories(target.getParent());
            Files.copy(screenshot.toPath(), target, StandardCopyOption.REPLACE_EXISTING);
            System.out.println("Screenshot saved: " + target.toAbsolutePath());
        } catch (Exception e) {
            // A failed screenshot must never hide the real test failure
            System.out.println("Could not save screenshot: " + e.getMessage());
        }
    }
}