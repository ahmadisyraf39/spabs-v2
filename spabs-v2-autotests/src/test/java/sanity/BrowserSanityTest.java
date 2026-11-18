package sanity;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.testng.Assert;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

public class BrowserSanityTest {

    private WebDriver driver;

    @BeforeTest
    public void setUp(){
        driver = new ChromeDriver();
    }

    @Test
    public void seleniumSite_hasSeleniumTitle(){
        driver.get("https://www.selenium.dev/");
        String title = driver.getTitle();
        Assert.assertTrue(title.contains("Selenium"), "Title was: " + title);
    }

    @AfterTest
    public void tearDown(){
        if (driver!=null) {
            driver.quit();
        }
    }

}
