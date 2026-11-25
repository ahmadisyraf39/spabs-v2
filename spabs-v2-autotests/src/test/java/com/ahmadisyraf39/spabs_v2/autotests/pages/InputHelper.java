package com.ahmadisyraf39.spabs_v2.autotests.pages;

import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

public final class InputHelper {

    private InputHelper() {}

    /**
     * Sets a date/time input to an ISO value ("2027-01-04", "17:00") in a way that works in any
     * browser locale. Uses the browser's own value setter, then fires an "input" event so
     * React's onChange runs and the form state updates.
     */
    public static void setValue(WebDriver driver, WebElement input, String isoValue) {
        ((JavascriptExecutor) driver).executeScript(
                "const setter = Object.getOwnPropertyDescriptor(HTMLInputElement.prototype, 'value').set;"
                        + "setter.call(arguments[0], arguments[1]);"
                        + "arguments[0].dispatchEvent(new Event('input', { bubbles: true }));",
                input, isoValue);
    }
}