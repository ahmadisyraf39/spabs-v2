package com.ahmadisyraf39.spabs_v2.autotests.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

public class ChangePasswordPage {

    private final WebDriver driver;
    private final WebDriverWait wait;

    private final By currentPasswordInput = By.cssSelector("[data-testid='change-current-password']");
    private final By newPasswordInput = By.cssSelector("[data-testid='change-new-password']");
    private final By submitButton = By.cssSelector("[data-testid='change-submit']");

    public ChangePasswordPage(WebDriver driver, WebDriverWait wait) {
        this.driver = driver;
        this.wait = wait;
    }

    public void changePassword(String currentPassword, String newPassword) {
        wait.until(ExpectedConditions.visibilityOfElementLocated(currentPasswordInput)).sendKeys(currentPassword);
        driver.findElement(newPasswordInput).sendKeys(newPassword);
        driver.findElement(submitButton).click();
    }
}