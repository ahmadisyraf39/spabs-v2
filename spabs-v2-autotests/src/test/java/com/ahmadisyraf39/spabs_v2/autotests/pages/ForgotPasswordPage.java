package com.ahmadisyraf39.spabs_v2.autotests.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

public class ForgotPasswordPage {

    private static final String PAGE_TITLE = "Forgot password";
    private static final String CONFIRMATION_TITLE = "Check your email";

    private final WebDriver driver;
    private final WebDriverWait wait;

    private final By title = By.cssSelector("[data-testid='auth-title']");
    private final By emailInput = By.cssSelector("[data-testid='forgot-email']");
    private final By submitButton = By.cssSelector("[data-testid='forgot-submit']");

    public ForgotPasswordPage(WebDriver driver, WebDriverWait wait) {
        this.driver = driver;
        this.wait = wait;
    }

    // Wait until it shows the page's title, not just until it's visible
    public void waitUntilLoaded() {
        wait.until(ExpectedConditions.textToBe(title, PAGE_TITLE));
    }

    public String getTitle() {
        return driver.findElement(title).getText();
    }

    public void requestResetLink(String email) {
        driver.findElement(emailInput).sendKeys(email);
        driver.findElement(submitButton).click();
    }

    public void waitForConfirmation() {
        wait.until(ExpectedConditions.textToBe(title, CONFIRMATION_TITLE));
    }
}