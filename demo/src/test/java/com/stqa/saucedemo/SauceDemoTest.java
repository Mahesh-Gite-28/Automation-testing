package com.stqa.saucedemo;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Automated test cases for https://www.saucedemo.com/
 * Each test starts from a new browser session (see BaseTest).
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class SauceDemoTest extends BaseTest {

    @Test
    @Order(1)
    @DisplayName("1. Open website and verify page title")
    void openWebsiteAndVerifyTitle() {
        String actualTitle = driver.getTitle();
        System.out.println("Website title: " + actualTitle);
        assertEquals("Swag Labs", actualTitle);
    }

    @Test
    @Order(2)
    @DisplayName("2. Login successfully with valid credentials")
    void loginWithValidCredentials() {
        loginAsStandardUser();

        assertTrue(driver.getCurrentUrl().contains("inventory.html"),
                "After a successful login the URL should contain inventory.html");
        assertEquals("Products", waitFor(By.className("title")).getText());
    }

    @Test
    @Order(3)
    @DisplayName("3. Login with invalid password and verify error")
    void loginWithInvalidPassword() {
        login(VALID_USERNAME, "wrong_password");

        String message = errorMessage();
        assertTrue(message.contains("Username and password do not match any user in this service"),
                "Unexpected error message: " + message);
        assertTrue(driver.getCurrentUrl().contains("saucedemo.com"),
                "User should remain on the login page");
    }

    @Test
    @Order(4)
    @DisplayName("4. Submit login with empty fields and verify validation")
    void loginWithEmptyFields() {
        waitFor(By.id("login-button")).click();

        String message = errorMessage();
        assertTrue(message.contains("Username is required"),
                "Empty login should ask for a username. Actual: " + message);
    }

    @Test
    @Order(5)
    @DisplayName("5. Verify products on the inventory page")
    void verifyInventoryProducts() {
        loginAsStandardUser();

        List<WebElement> products = driver.findElements(By.className("inventory_item"));
        assertEquals(6, products.size(), "SauceDemo inventory should list 6 products");

        List<String> names = productNames();
        assertTrue(names.contains("Sauce Labs Backpack"));
        assertTrue(names.contains("Sauce Labs Bike Light"));
        assertTrue(names.contains("Sauce Labs Bolt T-Shirt"));
        assertTrue(names.contains("Sauce Labs Fleece Jacket"));
        assertTrue(names.contains("Sauce Labs Onesie"));
        assertTrue(names.contains("Test.allTheThings() T-Shirt (Red)"));
    }

    @Test
    @Order(6)
    @DisplayName("6. Sort products by name and price")
    void sortProductsByNameAndPrice() {
        loginAsStandardUser();

        sortBy("az");
        List<String> namesAz = productNames();
        List<String> expectedAz = new ArrayList<>(namesAz);
        Collections.sort(expectedAz);
        assertEquals(expectedAz, namesAz, "Name (A to Z) sort is incorrect");

        sortBy("za");
        List<String> namesZa = productNames();
        List<String> expectedZa = new ArrayList<>(namesZa);
        expectedZa.sort(Collections.reverseOrder());
        assertEquals(expectedZa, namesZa, "Name (Z to A) sort is incorrect");

        sortBy("lohi");
        List<Double> pricesLowHigh = productPrices();
        List<Double> expectedLowHigh = new ArrayList<>(pricesLowHigh);
        Collections.sort(expectedLowHigh);
        assertEquals(expectedLowHigh, pricesLowHigh, "Price (low to high) sort is incorrect");

        sortBy("hilo");
        List<Double> pricesHighLow = productPrices();
        List<Double> expectedHighLow = new ArrayList<>(pricesHighLow);
        expectedHighLow.sort(Collections.reverseOrder());
        assertEquals(expectedHighLow, pricesHighLow, "Price (high to low) sort is incorrect");
    }

    @Test
    @Order(7)
    @DisplayName("7. Add a product to the cart and verify cart count")
    void addProductAndVerifyCartCount() {
        loginAsStandardUser();

        waitFor(By.id("add-to-cart-sauce-labs-backpack")).click();

        String badge = waitFor(By.className("shopping_cart_badge")).getText();
        assertEquals("1", badge, "Cart badge should show 1 item after adding Backpack");
    }

    @Test
    @Order(8)
    @DisplayName("8. Open the cart and verify the selected product")
    void openCartAndVerifySelectedProduct() {
        loginAsStandardUser();
        waitFor(By.id("add-to-cart-sauce-labs-backpack")).click();
        waitFor(By.className("shopping_cart_link")).click();

        waitFor(By.className("cart_list"));
        assertEquals("Your Cart", waitFor(By.className("title")).getText());
        assertEquals("Sauce Labs Backpack", waitFor(By.className("inventory_item_name")).getText());
        assertEquals("$29.99", waitFor(By.className("inventory_item_price")).getText());
    }

    @Test
    @Order(9)
    @DisplayName("9. Remove a product and verify it is removed")
    void removeProductFromCart() {
        loginAsStandardUser();
        waitFor(By.id("add-to-cart-sauce-labs-backpack")).click();
        waitFor(By.className("shopping_cart_link")).click();

        waitFor(By.id("remove-sauce-labs-backpack")).click();

        List<WebElement> items = driver.findElements(By.className("cart_item"));
        assertTrue(items.isEmpty(), "Cart should be empty after removing the product");
        assertTrue(driver.findElements(By.className("shopping_cart_badge")).isEmpty(),
                "Cart badge should disappear when the cart is empty");
    }

    @Test
    @Order(10)
    @DisplayName("10. Complete checkout and verify confirmation")
    void completeCheckoutProcess() {
        loginAsStandardUser();
        waitFor(By.id("add-to-cart-sauce-labs-backpack")).click();
        waitFor(By.className("shopping_cart_link")).click();
        waitFor(By.id("checkout")).click();

        waitFor(By.id("first-name")).sendKeys("John");
        driver.findElement(By.id("last-name")).sendKeys("Doe");
        driver.findElement(By.id("postal-code")).sendKeys("12345");
        driver.findElement(By.id("continue")).click();

        waitFor(By.id("finish")).click();

        String confirmation = waitFor(By.className("complete-header")).getText();
        assertEquals("Thank you for your order!", confirmation);
        assertTrue(driver.getCurrentUrl().contains("checkout-complete.html"));
    }

    @Test
    @Order(11)
    @DisplayName("11. Logout and verify the login page")
    void logoutAndVerifyLoginPage() {
        loginAsStandardUser();

        waitFor(By.id("react-burger-menu-btn")).click();
        wait.until(ExpectedConditions.elementToBeClickable(By.id("logout_sidebar_link"))).click();

        waitFor(By.id("login-button"));
        assertTrue(driver.getCurrentUrl().startsWith(BASE_URL));
        assertTrue(driver.findElement(By.id("user-name")).isDisplayed());
        assertTrue(driver.findElement(By.id("password")).isDisplayed());
    }

    @Test
    @Order(12)
    @DisplayName("12. Locked-out user cannot log in")
    void lockedOutUserCannotLogin() {
        login("locked_out_user", VALID_PASSWORD);

        String message = errorMessage();
        assertTrue(message.contains("Sorry, this user has been locked out"),
                "Unexpected locked-out message: " + message);
        assertFalse(driver.getCurrentUrl().contains("inventory.html"),
                "Locked-out user must not reach the inventory page");
    }

    private void sortBy(String value) {
        new Select(waitFor(By.className("product_sort_container"))).selectByValue(value);
        waitFor(By.className("inventory_item_name"));
    }

    private List<String> productNames() {
        List<WebElement> nameElements = driver.findElements(By.className("inventory_item_name"));
        List<String> names = new ArrayList<>();
        for (WebElement element : nameElements) {
            names.add(element.getText());
        }
        return names;
    }

    private List<Double> productPrices() {
        List<WebElement> priceElements = driver.findElements(By.className("inventory_item_price"));
        List<Double> prices = new ArrayList<>();
        for (WebElement element : priceElements) {
            prices.add(Double.parseDouble(element.getText().replace("$", "")));
        }
        return prices;
    }
}
