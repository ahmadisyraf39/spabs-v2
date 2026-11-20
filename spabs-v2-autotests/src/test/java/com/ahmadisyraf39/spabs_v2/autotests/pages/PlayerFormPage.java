package com.ahmadisyraf39.spabs_v2.autotests.pages;

import com.ahmadisyraf39.spabs_v2.autotests.config.TestConfig;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

public class PlayerFormPage {

    private final WebDriver driver;
    private final WebDriverWait wait;

    private final By fullNameInput = By.cssSelector("[data-testid='player-fullname']");
    private final By dateOfBirthInput = By.cssSelector("[data-testid='player-dob']");
    private final By genderSelect = By.cssSelector("[data-testid='player-gender']");
    private final By saveButton = By.cssSelector("[data-testid='player-save']");

    public PlayerFormPage(WebDriver driver, WebDriverWait wait) {
        this.driver = driver;
        this.wait = wait;
    }

    public void open() {
        driver.get(TestConfig.BASE_URL + "/players/new");
        waitUntilLoaded();
    }

    public void waitUntilLoaded() {
        wait.until(ExpectedConditions.visibilityOfElementLocated(fullNameInput));
    }

    public void enterFullName(String fullName) {
        driver.findElement(fullNameInput).sendKeys(fullName);
    }

    // Type the date in the browser's locale order, without separators,
    // e.g. "10052015" for 10 May 2015 in day-month-year locales.
    public void enterDateOfBirth(String typedDate) {
        driver.findElement(dateOfBirthInput).sendKeys(typedDate);
    }

    public void selectGender(String gender) {
        new Select(driver.findElement(genderSelect)).selectByVisibleText(gender);
    }

    public void clickSave() {
        driver.findElement(saveButton).click();
    }

    public void createPlayer(String fullName, String typedDateOfBirth, String gender) {
        enterFullName(fullName);
        enterDateOfBirth(typedDateOfBirth);
        selectGender(gender);
        clickSave();
    }

    // The browser's built-in "required" message, e.g. "Please fill out this field."
    public String getFullNameValidationMessage() {
        WebElement input = driver.findElement(fullNameInput);
        return input.getDomProperty("validationMessage");
    }
}