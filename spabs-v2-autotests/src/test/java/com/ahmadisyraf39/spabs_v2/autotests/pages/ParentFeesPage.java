package com.ahmadisyraf39.spabs_v2.autotests.pages;

import com.ahmadisyraf39.spabs_v2.autotests.config.TestConfig;
import org.openqa.selenium.By;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

public class ParentFeesPage {

    private final WebDriver driver;
    private final WebDriverWait wait;

    private final By childFilter = By.cssSelector("[data-testid='parent-fees-child-filter']");
    private final By feeRows = By.cssSelector("[data-testid='parent-fee-row']");
    private final By status = By.cssSelector("[data-testid='parent-fee-status']");

    public ParentFeesPage(WebDriver driver, WebDriverWait wait) {
        this.driver = driver;
        this.wait = wait;
    }

    public void open() {
        driver.get(TestConfig.BASE_URL + "/dashboard/parent/fees");
        wait.until(ExpectedConditions.visibilityOfElementLocated(childFilter));
    }

    public void filterByChild(String childName) {
        new Select(driver.findElement(childFilter)).selectByVisibleText(childName);
    }

    public String getStatusOf(String childName) {
        return waitForRow(childName).findElement(status).getText();
    }

    private WebElement waitForRow(String childName) {
        return wait.ignoring(StaleElementReferenceException.class)
                .until(d -> d.findElements(feeRows).stream()
                        .filter(row -> row.getText().contains(childName))
                        .findFirst()
                        .orElse(null));
    }
}