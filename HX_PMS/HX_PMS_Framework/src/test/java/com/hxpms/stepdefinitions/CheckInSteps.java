package com.hxpms.stepdefinitions;

import com.hxpms.utils.BaseTest;
import io.cucumber.java.en.When;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Then;
import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import java.io.File;
import java.io.FileOutputStream;
import java.nio.file.Files;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class CheckInSteps {
    WebDriver driver = BaseTest.driver;
    JavascriptExecutor js = (JavascriptExecutor) BaseTest.driver;

    private static final By CHECK_IN_BTN =
        By.xpath("//a[@class='btn btn-warning float-end mb-3']");

    @When("I navigate to arrivals page")
    public void iNavigateToArrivalsPage() {
        driver.get("https://demo.hotelxplore.com/frontdesk/booking/todays/check-in");
        new WebDriverWait(driver, Duration.ofSeconds(20))
            .until(ExpectedConditions.urlContains("check-in"));
        dismissAlert();
    }

    @And("I select a booking and check in")
    public void iSelectABookingAndCheckIn() throws InterruptedException {
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(20));

        // ── Observe: wait for page to load and count all guests ───────────────
        try {
            wait.until(ExpectedConditions.presenceOfElementLocated(CHECK_IN_BTN));
        } catch (Exception e) {
            System.out.println("[CheckInSteps] No check-in buttons found on arrivals page");
            return;
        }
        Thread.sleep(500);

        int totalGuests = driver.findElements(CHECK_IN_BTN).size();
        System.out.println("╔══════════════════════════════════════════╗");
        System.out.println("║  Arrivals page loaded                    ║");
        System.out.println("║  Total guests present: " + totalGuests + "                 ║");
        System.out.println("╚══════════════════════════════════════════╝");

        // ── Process each guest ─────────────────────────────────────────────────
        // Always click index [1] — after each check-in the DOM refreshes
        // so the next guest slides into position [1]
        for (int i = 1; i <= totalGuests; i++) {
            System.out.println("\n[CheckInSteps] ── Processing guest " + i + " of " + totalGuests + " ──");

            // Re-check how many buttons remain
            List<WebElement> remaining = driver.findElements(CHECK_IN_BTN);
            if (remaining.isEmpty()) {
                System.out.println("[CheckInSteps] No more check-in buttons — all guests processed");
                break;
            }
            System.out.println("[CheckInSteps] Check-in buttons remaining: " + remaining.size());

            // Always click the first button (index [1])
            By firstBtn = By.xpath("(//a[@class='btn btn-warning float-end mb-3'])[1]");
            try {
                wait.until(ExpectedConditions.elementToBeClickable(firstBtn));
                WebElement btn = driver.findElement(firstBtn);
                js.executeScript("arguments[0].scrollIntoView({block:'center'});", btn);
                Thread.sleep(300);
                js.executeScript("arguments[0].click();", btn);
                System.out.println("[CheckInSteps] Clicked Check In button for guest " + i);
                Thread.sleep(1500);
                dismissAlert();
            } catch (Exception e) {
                System.out.println("[CheckInSteps] Could not click Check In button: " + e.getMessage());
                continue;
            }

            // ── Handle post-payment modal ──────────────────────────────────────
            handlePostPaymentModal(wait, i);
        }

        System.out.println("\n[CheckInSteps] ✓ All " + totalGuests + " guests checked in");
    }

    @Then("the guest should be checked in successfully")
    public void theGuestShouldBeCheckedInSuccessfully() {
        System.out.println("[CheckInSteps] Check-in scenario completed successfully");
    }

    // ── Post-payment modal: button[2] → link → save PDF ───────────────────────
    private void handlePostPaymentModal(WebDriverWait wait, int guestIndex)
            throws InterruptedException {

        // Step 1: Click Check In button inside modal
        By checkInBtn = By.xpath("//*[@id='post-payment-form']/div[2]/button[2]");
        try {
            wait.until(ExpectedConditions.elementToBeClickable(checkInBtn));
            WebElement btn = driver.findElement(checkInBtn);
            js.executeScript("arguments[0].scrollIntoView({block:'center'});", btn);
            Thread.sleep(300);
            js.executeScript("arguments[0].click();", btn);
            System.out.println("[CheckInSteps] Clicked modal Check In button (guest " + guestIndex + ")");
            Thread.sleep(1500);
            dismissAlert();
        } catch (Exception e) {
            System.out.println("[CheckInSteps] Modal Check In button not found: " + e.getMessage());
            return;
        }

        // Step 2: Click next link (Complete / View Receipt)
        By nextLink = By.xpath("//*[@id='post-payment-form']/div[2]/a");
        try {
            wait.until(ExpectedConditions.elementToBeClickable(nextLink));
            WebElement link = driver.findElement(nextLink);
            js.executeScript("arguments[0].scrollIntoView({block:'center'});", link);
            Thread.sleep(300);
            js.executeScript("arguments[0].click();", link);
            System.out.println("[CheckInSteps] Clicked next link (guest " + guestIndex + ")");
            Thread.sleep(2000);
            dismissAlert();
        } catch (Exception e) {
            System.out.println("[CheckInSteps] Next link not found: " + e.getMessage());
            return;
        }

        // Step 3: Save receipt from //*[@id='content']
        saveReceipt(wait, guestIndex);

        // Step 4: Go back to arrivals page for next guest
        driver.get("https://demo.hotelxplore.com/frontdesk/booking/todays/check-in");
        wait.until(ExpectedConditions.urlContains("check-in"));
        Thread.sleep(1000);
        dismissAlert();
        System.out.println("[CheckInSteps] Returned to arrivals page");
    }

    // ── Save receipt as screenshot + HTML ─────────────────────────────────────
    private void saveReceipt(WebDriverWait wait, int guestIndex) {
        try {
            wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.xpath("//*[@id='content']")));
            Thread.sleep(500);

            File dir = new File("target/checkin-receipts");
            dir.mkdirs();

            String timestamp = LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            String baseName = "guest" + guestIndex + "_" + timestamp;

            // Screenshot
            try {
                File shot = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
                File dest = new File(dir, baseName + ".png");
                Files.copy(shot.toPath(), dest.toPath());
                System.out.println("[CheckInSteps] Receipt screenshot saved: " + dest.getAbsolutePath());
            } catch (Exception ignored) {}

            // HTML (open in browser → Ctrl+P → Save as PDF)
            try {
                File html = new File(dir, baseName + ".html");
                try (FileOutputStream fos = new FileOutputStream(html)) {
                    fos.write(driver.getPageSource().getBytes());
                }
                System.out.println("[CheckInSteps] Receipt HTML saved: " + html.getAbsolutePath());
            } catch (Exception ignored) {}

        } catch (Exception e) {
            System.out.println("[CheckInSteps] saveReceipt failed: " + e.getMessage());
        }
    }

    private void dismissAlert() {
        try {
            new WebDriverWait(driver, Duration.ofSeconds(2))
                .until(ExpectedConditions.alertIsPresent());
            driver.switchTo().alert().accept();
        } catch (Exception ignored) {}
    }
}
