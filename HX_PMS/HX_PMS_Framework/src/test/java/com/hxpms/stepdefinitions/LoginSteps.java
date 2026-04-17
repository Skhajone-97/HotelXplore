package com.hxpms.stepdefinitions;

import com.hxpms.utils.BaseTest;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.When;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Then;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.openqa.selenium.support.ui.ExpectedConditions;
import java.time.Duration;

public class LoginSteps {
    WebDriver driver = BaseTest.driver;

    @Given("I am on the login page")
    public void iAmOnTheLoginPage() {
        driver.get("https://demo.hotelxplore.com/frontdesk");
    }

    @When("I enter username {string} and password {string}")
    public void iEnterUsernameAndPassword(String username, String password) {
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(30));
        wait.until(ExpectedConditions.or(
            ExpectedConditions.titleContains("Admin Login"),
            ExpectedConditions.urlContains("frontdesk")
        ));
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("username")));
        driver.findElement(By.id("username")).sendKeys(username);
        driver.findElement(By.id("password")).sendKeys(password);
    }

    @And("I click the login button")
    public void iClickTheLoginButton() {
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(30));
        waitUntilPageReady(wait);
        submitLogin(wait);
        waitForDashboard(wait);
    }

    @Then("I should be logged in successfully")
    public void iShouldBeLoggedInSuccessfully() {
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(30));
        waitForDashboard(wait);
    }

    private void waitUntilPageReady(WebDriverWait wait) {
        wait.until(driver -> ((JavascriptExecutor) driver).executeScript("return document.readyState").equals("complete"));
    }

    private void submitLogin(WebDriverWait wait) {
        WebElement button = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("button[type='submit']")));
        ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", button);
        try {
            new Actions(driver).moveToElement(button).click().perform();
        } catch (Exception e) {
            try {
                ((JavascriptExecutor) driver).executeScript("arguments[0].click();", button);
            } catch (Exception inner) {
                driver.findElement(By.id("password")).sendKeys(Keys.ENTER);
            }
        }

        if (!driver.getCurrentUrl().contains("dashboard")) {
            try {
                Thread.sleep(1000);
                WebElement loginForm = driver.findElement(By.cssSelector("form.cmn-form.verify-gcaptcha.login-form"));
                loginForm.submit();
            } catch (Exception ignored) {
                // fallback silently
            }
        }
    }

    private void waitForDashboard(WebDriverWait wait) {
        System.out.println("[LoginSteps] waiting for dashboard, current URL=" + driver.getCurrentUrl() + ", title=" + driver.getTitle());
        wait.until(ExpectedConditions.or(
            ExpectedConditions.urlContains("dashboard"),
            ExpectedConditions.titleContains("Dashboard"),
            ExpectedConditions.visibilityOfElementLocated(By.cssSelector("a[href*='check-in']")),
            ExpectedConditions.visibilityOfElementLocated(By.cssSelector("a[href*='booking/todays/check-in']"))
        ));
        System.out.println("[LoginSteps] dashboard detected, URL=" + driver.getCurrentUrl() + ", title=" + driver.getTitle());
    }
}