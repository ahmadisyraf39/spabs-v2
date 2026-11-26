package com.ahmadisyraf39.spabs_v2.autotests.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

public class ResetPasswordPage {

    private final WebDriver driver;
    private final WebDriverWait wait;

    private final By title = By.cssSelector("[data-testid='auth-title']");
    private final By newPasswordInput = By.cssSelector("[data-testid='reset-new-password']");
    private final By submitButton = By.cssSelector("[data-testid='reset-submit']");

    public ResetPasswordPage(WebDriver driver, WebDriverWait wait) {
        this.driver = driver;
        this.wait = wait;
    }

    public void resetTo(String newPassword) {
        wait.until(ExpectedConditions.visibilityOfElementLocated(newPasswordInput)).sendKeys(newPassword);
        driver.findElement(submitButton).click();
    }

    public void waitForSuccess() {
        wait.until(ExpectedConditions.textToBe(title, "Password reset"));
    }
}