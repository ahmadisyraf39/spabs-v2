package com.ahmadisyraf39.spabs_v2.autotests.pages;

import com.ahmadisyraf39.spabs_v2.autotests.config.TestConfig;
import org.openqa.selenium.By;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public class FeeRecordsPage {

    private final WebDriver driver;
    private final WebDriverWait wait;

    private final By recordRows = By.cssSelector("[data-testid='fee-record-row']");
    private final By status = By.cssSelector("[data-testid='fee-record-status']");
    private final By payButton = By.cssSelector("[data-testid='fee-record-pay']");
    private final By confirmButton = By.cssSelector("[data-testid='confirm-modal-confirm']");

    public FeeRecordsPage(WebDriver driver, WebDriverWait wait) {
        this.driver = driver;
        this.wait = wait;
    }

    public void open(int teamId, String playerName) {
        String search = URLEncoder.encode(playerName, StandardCharsets.UTF_8);
        driver.get(TestConfig.BASE_URL + "/fee-records?teamId=" + teamId + "&search=" + search);
        waitForRow(playerName);
    }

    public void markPaidFor(String playerName) {
        waitForRow(playerName).findElement(payButton).click();
        wait.until(ExpectedConditions.elementToBeClickable(confirmButton)).click();
    }

    public String getStatusOf(String playerName) {
        return waitForRow(playerName).findElement(status).getText();
    }

    public void waitUntilStatusIs(String playerName, String expectedStatus) {
        wait.ignoring(StaleElementReferenceException.class)
                .until(d -> expectedStatus.equals(waitForRow(playerName).findElement(status).getText()));
    }

    private WebElement waitForRow(String playerName) {
        return wait.ignoring(StaleElementReferenceException.class)
                .until(d -> d.findElements(recordRows).stream()
                        .filter(row -> row.getText().contains(playerName))
                        .findFirst()
                        .orElse(null));
    }
}