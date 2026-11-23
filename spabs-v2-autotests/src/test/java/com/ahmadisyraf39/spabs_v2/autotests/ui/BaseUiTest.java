package com.ahmadisyraf39.spabs_v2.autotests.ui;

import com.ahmadisyraf39.spabs_v2.autotests.pages.LoginPage;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;

import java.time.Duration;

public abstract class BaseUiTest {

    protected WebDriver driver;
    protected WebDriverWait wait;

    protected static final String SAMPLE_PASSWORD = "Password123";

    @BeforeMethod
    public void startBrowser(){
        driver = new ChromeDriver();
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));
    }

    @AfterMethod
    public void quitBrowser(){
        if(driver!= null){
            driver.quit();
        }
    }

    protected void loginAs(String email, String password, String expectedPath) {
        LoginPage loginPage = new LoginPage(driver, wait);
        loginPage.open();
        loginPage.loginAs(email, password);
        wait.until(ExpectedConditions.urlContains(expectedPath));
    }

}
