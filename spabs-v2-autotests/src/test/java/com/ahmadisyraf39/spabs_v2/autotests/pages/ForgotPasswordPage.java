package com.ahmadisyraf39.spabs_v2.autotests.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

public class ForgotPasswordPage {

    private static final String PAGE_TITLE = "Forgot password";

    private final WebDriver driver;
    private final WebDriverWait wait;

    private final By title = By.cssSelector("[data-testid='auth-title']");

    public ForgotPasswordPage(WebDriver driver, WebDriverWait wait) {
        this.driver = driver;
        this.wait = wait;
    }

    //wait until it shows the page's title, not just until it's visible
    public void waitUntilLoaded() {
        wait.until(ExpectedConditions.textToBe(title, PAGE_TITLE));
    }

    public String getTitle() {
        return driver.findElement(title).getText();
    }
}
