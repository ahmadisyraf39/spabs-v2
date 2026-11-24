package com.ahmadisyraf39.spabs_v2.autotests.pages;

import com.ahmadisyraf39.spabs_v2.autotests.config.TestConfig;
import org.openqa.selenium.By;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

public class TeamRosterPage {

    private final WebDriver driver;
    private final WebDriverWait wait;

    private final By rows = By.cssSelector("[data-testid='roster-row']");
    private final By status = By.cssSelector("[data-testid='roster-status']");
    private final By leftAt = By.cssSelector("[data-testid='roster-left-at']");
    private final By endStintButton = By.cssSelector("[data-testid='roster-end-stint']");
    private final By confirmButton = By.cssSelector("[data-testid='confirm-modal-confirm']");

    public TeamRosterPage(WebDriver driver, WebDriverWait wait) {
        this.driver = driver;
        this.wait = wait;
    }

    public void open(int teamId) {
        driver.get(TestConfig.BASE_URL + "/teams/" + teamId + "/edit");
    }

    public void endStintFor(String playerName) {
        waitForRow(playerName).findElement(endStintButton).click();
        wait.until(ExpectedConditions.elementToBeClickable(confirmButton)).click();
    }

    public String getStatusOf(String playerName) {
        return waitForRow(playerName).findElement(status).getText();
    }

    public String getLeftAtOf(String playerName) {
        return waitForRow(playerName).findElement(leftAt).getText();
    }

    public void waitUntilStatusIs(String playerName, String expectedStatus) {
        wait.ignoring(StaleElementReferenceException.class)
                .until(d -> expectedStatus.equals(waitForRow(playerName).findElement(status).getText()));
    }

    private WebElement waitForRow(String playerName) {
        return wait.ignoring(StaleElementReferenceException.class)
                .until(d -> d.findElements(rows).stream()
                        .filter(row -> row.getText().contains(playerName))
                        .findFirst()
                        .orElse(null));
    }
}