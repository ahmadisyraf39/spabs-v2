package com.ahmadisyraf39.spabs_v2.autotests.pages;

import com.ahmadisyraf39.spabs_v2.autotests.config.TestConfig;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

public class RecurringActivityFormPage {

    private final WebDriver driver;
    private final WebDriverWait wait;

    private final By teamSelect = By.cssSelector("[data-testid='recurring-team-select']");
    private final By applyToAllCheckbox = By.cssSelector("[data-testid='recurring-apply-all']");
    private final By daySelect = By.cssSelector("[data-testid='recurring-day']");
    private final By startTimeInput = By.cssSelector("[data-testid='recurring-start-time']");
    private final By startDateInput = By.cssSelector("[data-testid='recurring-start-date']");
    private final By endDateInput = By.cssSelector("[data-testid='recurring-end-date']");
    private final By submitButton = By.cssSelector("[data-testid='recurring-submit']");
    private final By successAlert = By.cssSelector("[data-testid='recurring-success']");
    private final By errorAlert = By.cssSelector("[data-testid='recurring-error']");

    public RecurringActivityFormPage(WebDriver driver, WebDriverWait wait) {
        this.driver = driver;
        this.wait = wait;
    }

    public void open() {
        driver.get(TestConfig.BASE_URL + "/activities/recurring");
        wait.until(ExpectedConditions.visibilityOfElementLocated(teamSelect));
    }

    public void selectTeam(String teamName) {
        WebElement option = wait.until(d -> new Select(d.findElement(teamSelect)).getOptions().stream()
                .filter(o -> o.getText().equals(teamName))
                .findFirst()
                .orElse(null));
        new Actions(driver).scrollToElement(option).click(option).perform();
    }

    public void checkApplyToAllTeams() {
        driver.findElement(applyToAllCheckbox).click();
    }

    public boolean isTeamSelectEnabled() {
        return driver.findElement(teamSelect).isEnabled();
    }

    public void selectDayOfWeek(String day) {
        new Select(driver.findElement(daySelect)).selectByVisibleText(day);
    }

    // Values in ISO format: time "17:00", dates "2027-01-04". Works in any browser locale
    public void enterStartTime(String isoTime) {
        InputHelper.setValue(driver, driver.findElement(startTimeInput), isoTime);
    }

    public void enterDateRange(String isoStartDate, String isoEndDate) {
        InputHelper.setValue(driver, driver.findElement(startDateInput), isoStartDate);
        InputHelper.setValue(driver, driver.findElement(endDateInput), isoEndDate);
    }

    public void submit() {
        driver.findElement(submitButton).click();
    }

    public String getSuccessMessage() {
        return wait.until(ExpectedConditions.visibilityOfElementLocated(successAlert)).getText();
    }

    public String getErrorMessage() {
        return wait.until(ExpectedConditions.visibilityOfElementLocated(errorAlert)).getText();
    }
}