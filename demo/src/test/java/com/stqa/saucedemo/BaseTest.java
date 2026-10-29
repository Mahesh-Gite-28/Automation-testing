package com.stqa.saucedemo;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

/**
 * Shared setup for every SauceDemo test.
 *
 * @BeforeEach starts a fresh Chrome browser so tests stay independent.
 * @AfterEach closes the browser even if a test fails.
 *
 * Selenium Manager (built into Selenium 4) downloads matching ChromeDriver.
 */
public class BaseTest {

    protected WebDriver driver;
    protected WebDriverWait wait;

    protected static final String BASE_URL = "https://www.saucedemo.com/";
    protected static final String VALID_USERNAME = "standard_user";
    protected static final String VALID_PASSWORD = "secret_sauce";

    @BeforeEach
    void setUp() {
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--start-maximized");
        options.addArguments("--incognito");
        options.addArguments("--disable-notifications");

        driver = new ChromeDriver(options);
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        driver.get(BASE_URL);
        waitFor(By.id("login-button"));
    }

    @AfterEach
    void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }

    protected WebElement waitFor(By locator) {
        return wait.until(ExpectedConditions.visibilityOfElementLocated(locator));
    }

    protected void login(String username, String password) {
        waitFor(By.id("user-name")).clear();
        driver.findElement(By.id("user-name")).sendKeys(username);
        driver.findElement(By.id("password")).clear();
        driver.findElement(By.id("password")).sendKeys(password);
        driver.findElement(By.id("login-button")).click();
    }

    protected void loginAsStandardUser() {
        login(VALID_USERNAME, VALID_PASSWORD);
        waitFor(By.className("inventory_list"));
    }

    protected String errorMessage() {
        return waitFor(By.cssSelector("[data-test='error']")).getText();
    }
}
