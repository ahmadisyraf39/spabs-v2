package com.ahmadisyraf39.spabs_v2.autotests.pages;

import com.ahmadisyraf39.spabs_v2.autotests.config.TestConfig;
import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

public class PlayersListPage {

    private final WebDriver driver;
    private final WebDriverWait wait;

    private final By newPlayerButton = By.cssSelector("[data-testid='players-new']");
    private final By searchInput = By.cssSelector("[data-testid='players-search']");
    private final By playerRows = By.cssSelector("[data-testid='player-row']");
    private final By detailsLink = By.cssSelector("[data-testid='player-details-link']");

    public PlayersListPage(WebDriver driver, WebDriverWait wait) {
        this.driver = driver;
        this.wait = wait;
    }

    public void open() {
        driver.get(TestConfig.BASE_URL + "/players");
        wait.until(ExpectedConditions.visibilityOfElementLocated(searchInput));
    }

    public void clickNewPlayer() {
        driver.findElement(newPlayerButton).click();
    }

    public void search(String name) {
        WebElement search = driver.findElement(searchInput);
        search.clear();
        search.sendKeys(name);
    }

    public boolean isPlayerListed(String name) {
        try {
            wait.ignoring(StaleElementReferenceException.class)
                    .until(d -> findRowContaining(name));
            return true;
        } catch (TimeoutException e) {
            return false;
        }
    }

    public void openDetailsOf(String name) {
        WebElement row = wait.ignoring(StaleElementReferenceException.class)
                .until(d -> findRowContaining(name));
        row.findElement(detailsLink).click();
    }

    private WebElement findRowContaining(String name) {
        return driver.findElements(playerRows).stream()
                .filter(row -> row.getText().contains(name))
                .findFirst()
                .orElse(null);
    }



}
