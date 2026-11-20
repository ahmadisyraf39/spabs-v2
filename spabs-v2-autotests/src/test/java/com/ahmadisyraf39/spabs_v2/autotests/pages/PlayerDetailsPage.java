package com.ahmadisyraf39.spabs_v2.autotests.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

public class PlayerDetailsPage {

    private final WebDriver driver;
    private final WebDriverWait wait;

    private final By title = By.cssSelector("[data-testid='player-details-title']");

    public PlayerDetailsPage(WebDriver driver, WebDriverWait wait) {
        this.driver = driver;
        this.wait = wait;
    }

    public String getTitle() {
        return wait.until(ExpectedConditions.visibilityOfElementLocated(title)).getText();
    }
}