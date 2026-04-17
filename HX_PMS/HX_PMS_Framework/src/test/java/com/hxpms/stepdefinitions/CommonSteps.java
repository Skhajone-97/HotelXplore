package com.hxpms.stepdefinitions;

import com.hxpms.utils.BaseTest;
import io.cucumber.java.en.Given;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.openqa.selenium.support.ui.ExpectedConditions;
import java.time.Duration;

public class CommonSteps {
    WebDriver driver = BaseTest.driver;

    @Given("I am logged in")
    public void iAmLoggedIn() {
        driver.get("https://demo.hotelxplore.com/frontdesk");
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(30));
        waitUntilPageReady(wait);
        wait.until(ExpectedConditions.or(
            ExpectedConditions.titleContains("Admin Login"),
            ExpectedConditions.urlContains("frontdesk")
        ));
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.name("username")));
        driver.findElement(By.name("username")).sendKeys("webdev");
        driver.findElement(By.name("password")).sendKeys("1234");
        submitLogin(wait);
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
                driver.findElement(By.name("password")).sendKeys(Keys.ENTER);
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
        System.out.println("[CommonSteps] waiting for dashboard, current URL=" + driver.getCurrentUrl() + ", title=" + driver.getTitle());
        wait.until(ExpectedConditions.or(
            ExpectedConditions.urlContains("dashboard"),
            ExpectedConditions.titleContains("Dashboard"),
            ExpectedConditions.visibilityOfElementLocated(By.cssSelector("a[href*='check-in']")),
            ExpectedConditions.visibilityOfElementLocated(By.cssSelector("a[href*='booking/todays/check-in']"))
        ));
        System.out.println("[CommonSteps] dashboard detected, URL=" + driver.getCurrentUrl() + ", title=" + driver.getTitle());
    }
}
