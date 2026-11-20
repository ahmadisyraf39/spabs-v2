package com.ahmadisyraf39.spabs_v2.autotests.ui;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;

import java.time.Duration;

public abstract class BaseUiTest {

    protected WebDriver driver;
    protected WebDriverWait wait;

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

}
