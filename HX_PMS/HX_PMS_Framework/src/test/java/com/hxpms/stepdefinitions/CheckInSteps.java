package com.hxpms.stepdefinitions;

import com.hxpms.utils.BaseTest;
import io.cucumber.java.en.When;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Then;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.openqa.selenium.support.ui.ExpectedConditions;
import java.time.Duration;

public class CheckInSteps {
    WebDriver driver = BaseTest.driver;

    @When("I navigate to arrivals page")
    public void iNavigateToArrivalsPage() {
        driver.get("https://demo.hotelxplore.com/frontdesk/booking/todays/check-in");
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(20));
        wait.until(ExpectedConditions.urlContains("check-in"));
        handlePopups();
    }

    @And("I select a booking and check in")
    public void iSelectABookingAndCheckIn() {
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        wait.until(ExpectedConditions.elementToBeClickable(By.name("selectBooking")));
        driver.findElement(By.name("selectBooking")).click();
        driver.findElement(By.name("checkInButton")).click();
        handlePopups();
    }

    @Then("the guest should be checked in successfully")
    public void theGuestShouldBeCheckedInSuccessfully() {
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        wait.until(ExpectedConditions.urlContains("checkin"));
    }

    private void handlePopups() {
        try {
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(2));
            wait.until(ExpectedConditions.alertIsPresent());
            driver.switchTo().alert().accept();
        } catch (Exception e) {
            // No alert
        }
    }
}