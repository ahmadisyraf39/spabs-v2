package com.ahmadisyraf39.spabs_v2.autotests.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.util.List;

public class Sidebar {

    private final WebDriver driver;
    private final WebDriverWait wait;

    private final By navLinks = By.cssSelector("[data-testid='sidebar-nav'] a");

    public Sidebar(WebDriver driver, WebDriverWait wait) {
        this.driver = driver;
        this.wait = wait;
    }

    public List<String> getLinkPaths() {
        wait.until(ExpectedConditions.visibilityOfElementLocated(navLinks));
        return driver.findElements(navLinks).stream()
                .map(link -> link.getDomAttribute("href"))
                .toList();
    }
}