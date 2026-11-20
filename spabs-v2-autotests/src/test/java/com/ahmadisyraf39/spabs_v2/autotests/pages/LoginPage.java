package com.ahmadisyraf39.spabs_v2.autotests.pages;

import com.ahmadisyraf39.spabs_v2.autotests.config.TestConfig;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

public class LoginPage {



    private final WebDriver driver;
    private final WebDriverWait wait;

    private final By emailInput = By.cssSelector("[data-testid='login-email']");
    private final By passwordInput = By.cssSelector("[data-testid='login-password']");
    private final By loginButton = By.cssSelector("[data-testid='login-submit']");
    private final By errorAlert = By.cssSelector("[data-testid='login-error']");
    private final By forgotPasswordLink = By.cssSelector("[data-testid='login-forgot-password']");

    public LoginPage(WebDriver driver, WebDriverWait wait) {
        this.driver = driver;
        this.wait = wait;
    }

    public void open(){
        driver.get(TestConfig.BASE_URL + "/login");
        wait.until(ExpectedConditions.visibilityOfElementLocated(emailInput));
    }

    public void enterEmail(String email){
        WebElement emailElement = driver.findElement(emailInput);
        emailElement.sendKeys(email);
    }

    public void enterPassword(String password){
        WebElement passwordElement = driver.findElement(passwordInput);
        passwordElement.sendKeys(password);
    }

    public void clickLogin() {
        WebElement loginElement = driver.findElement(loginButton);
        loginElement.click();
    }

    public void loginAs(String email, String password){
        enterEmail(email);
        enterPassword(password);
        clickLogin();
    }

    public String getErrorMessage(){
        return wait.until(ExpectedConditions.visibilityOfElementLocated(errorAlert)).getText();
    }

    public void clickForgotPassword(){
        WebElement forgotPasswordLinkElement = driver.findElement(forgotPasswordLink);
        forgotPasswordLinkElement.click();
    }


    public boolean isLoginFormDisplayed(){
        return driver.findElement(emailInput).isDisplayed()
                && driver.findElement(passwordInput).isDisplayed()
                && driver.findElement(loginButton).isDisplayed();
    }
}
