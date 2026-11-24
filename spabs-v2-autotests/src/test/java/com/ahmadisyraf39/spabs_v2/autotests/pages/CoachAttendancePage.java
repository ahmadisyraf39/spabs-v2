package com.ahmadisyraf39.spabs_v2.autotests.pages;

import com.ahmadisyraf39.spabs_v2.autotests.config.TestConfig;
import org.openqa.selenium.By;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

public class CoachAttendancePage {

    private final WebDriver driver;
    private final WebDriverWait wait;

    private final By rows = By.cssSelector("[data-testid='attendance-row']");
    private final By statusSelect = By.cssSelector("[data-testid='attendance-status']");
    private final By saveButton = By.cssSelector("[data-testid='attendance-save']");
    private final By successAlert = By.cssSelector("[data-testid='attendance-success']");

    public CoachAttendancePage(WebDriver driver, WebDriverWait wait) {
        this.driver = driver;
        this.wait = wait;
    }

    public void open(int activityId) {
        driver.get(TestConfig.BASE_URL + "/dashboard/coach/activities/" + activityId);
    }

    public void setStatus(String playerName, String status) {
        WebElement row = waitForRow(playerName);
        new Select(row.findElement(statusSelect)).selectByVisibleText(status);
    }

    public void save() {
        wait.until(ExpectedConditions.elementToBeClickable(saveButton)).click();
    }

    public String getSuccessMessage() {
        return wait.until(ExpectedConditions.visibilityOfElementLocated(successAlert)).getText();
    }

    private WebElement waitForRow(String playerName) {
        return wait.ignoring(StaleElementReferenceException.class)
                .until(d -> d.findElements(rows).stream()
                        .filter(row -> row.getText().contains(playerName))
                        .findFirst()
                        .orElse(null));
    }
}