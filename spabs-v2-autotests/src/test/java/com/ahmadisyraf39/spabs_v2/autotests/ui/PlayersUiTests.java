package com.ahmadisyraf39.spabs_v2.autotests.ui;

import com.ahmadisyraf39.spabs_v2.autotests.pages.LoginPage;
import com.ahmadisyraf39.spabs_v2.autotests.pages.PlayerDetailsPage;
import com.ahmadisyraf39.spabs_v2.autotests.pages.PlayerFormPage;
import com.ahmadisyraf39.spabs_v2.autotests.pages.PlayersListPage;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class PlayersUiTests extends BaseUiTest {

    private static final String ADMIN_EMAIL = "admin.sample@spabs.example";
    private static final String ADMIN_PASSWORD = "Password123";

    private static final String TYPED_DATE_OF_BIRTH = "10052015";

    private PlayersListPage playersListPage;
    private PlayerFormPage playerFormPage;
    private PlayerDetailsPage playerDetailsPage;

    @BeforeMethod
    public void loginAsAdmin() {
        playersListPage = new PlayersListPage(driver, wait);
        playerFormPage = new PlayerFormPage(driver, wait);
        playerDetailsPage = new PlayerDetailsPage(driver, wait);

        LoginPage loginPage = new LoginPage(driver, wait);
        loginPage.open();
        loginPage.loginAs(ADMIN_EMAIL, ADMIN_PASSWORD);
        wait.until(ExpectedConditions.urlContains("/dashboard/admin"));
    }

    @Test
    public void createPlayer_viaForm_showsInPlayersList() {
        String name = uniqueName();

        createPlayerViaForm(name);

        playersListPage.open();
        playersListPage.search(name);

        Assert.assertTrue(playersListPage.isPlayerListed(name),
                "Player '" + name + "' should be listed after creating it");
    }

    @Test
    public void submitEmptyPlayerForm_isBlockedByBrowserValidation() {
        playerFormPage.open();
        playerFormPage.clickSave();

        String currentUrl = driver.getCurrentUrl();
        Assert.assertTrue(currentUrl.contains("/players/new"),
                "Empty form should not submit, but URL was: " + currentUrl);

        String message = playerFormPage.getFullNameValidationMessage();
        Assert.assertTrue(message != null && !message.isBlank(),
                "Full name should show the browser's required-field message, but was: '" + message + "'");
    }

    @Test
    public void openPlayerDetails_fromList_showsPlayerName() {
        String name = uniqueName();
        createPlayerViaForm(name);

        playersListPage.open();
        playersListPage.search(name);
        playersListPage.openDetailsOf(name);

        wait.until(ExpectedConditions.urlMatches(".*/players/\\d+$"));

        Assert.assertEquals(playerDetailsPage.getTitle(), name);
    }

    //Helpers
    private void createPlayerViaForm(String name) {
        playersListPage.open();
        playersListPage.clickNewPlayer();
        playerFormPage.waitUntilLoaded();

        playerFormPage.createPlayer(name, TYPED_DATE_OF_BIRTH, "MALE");

        wait.until(ExpectedConditions.urlMatches(".*/players/\\d+/edit"));
    }

    private String uniqueName() {
        return "UI Test " + System.currentTimeMillis();
    }
}