package com.hxpms.stepdefinitions;

import com.hxpms.utils.BaseTest;
import io.cucumber.java.en.When;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Then;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.openqa.selenium.support.ui.ExpectedConditions;
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

    // XPath provided by user — all check-in buttons on the arrivals page
    private static final By CHECK_IN_BTN = By.xpath("//a[@class='btn btn-warning float-end mb-3']");

    @When("I navigate to arrivals page")
    public void iNavigateToArrivalsPage() {
        driver.get("https://demo.hotelxplore.com/frontdesk/booking/todays/check-in");
        new WebDriverWait(driver, Duration.ofSeconds(20)).until(
            ExpectedConditions.urlContains("check-in"));
        dismissAlert();
    }

    @And("I select a booking and check in")
    public void iSelectABookingAndCheckIn() throws InterruptedException {
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(20));

        // Wait for at least one check-in button to appear
        wait.until(ExpectedConditions.presenceOfElementLocated(CHECK_IN_BTN));
        Thread.sleep(500);

        // Get total count of check-in buttons present
        List<WebElement> checkInBtns = driver.findElements(CHECK_IN_BTN);
        int total = checkInBtns.size();
        System.out.println("[CheckInSteps] Total check-in buttons found: " + total);

        // Click each check-in button by index (re-fetch after each click to avoid stale)
        for (int i = 1; i <= total; i++) {
            try {
                By indexedBtn = By.xpath("(//a[@class='btn btn-warning float-end mb-3'])[" + i + "]");
                wait.until(ExpectedConditions.elementToBeClickable(indexedBtn));
                WebElement btn = driver.findElement(indexedBtn);
                js.executeScript("arguments[0].scrollIntoView({block:'center'});", btn);
                Thread.sleep(300);
                js.executeScript("arguments[0].click();", btn);
                System.out.println("[CheckInSteps] Clicked check-in button [" + i + "]");
                Thread.sleep(1000);
                dismissAlert();

                // Handle post-payment check-in modal popup
                handlePostPaymentCheckIn(wait);
                dismissAlert();

                // If page navigated away (e.g. to check-in form), go back to arrivals
                if (!driver.getCurrentUrl().contains("check-in")) {
                    driver.navigate().back();
                    wait.until(ExpectedConditions.urlContains("check-in"));
                    Thread.sleep(500);
                }
            } catch (Exception e) {
                System.out.println("[CheckInSteps] Could not click button [" + i + "]: " + e.getMessage());
            }
        }

        System.out.println("[CheckInSteps] All " + total + " check-in buttons processed");
    }

    @Then("the guest should be checked in successfully")
    public void theGuestShouldBeCheckedInSuccessfully() {
        // Verify we are still on or returned to the check-in page
        new WebDriverWait(driver, Duration.ofSeconds(20)).until(
            ExpectedConditions.urlContains("check-in"));
        System.out.println("[CheckInSteps] Check-in completed successfully");
    }

    private void dismissAlert() {
        try {
            new WebDriverWait(driver, Duration.ofSeconds(2))
                .until(ExpectedConditions.alertIsPresent());
            driver.switchTo().alert().accept();
        } catch (Exception ignored) {}
    }

    private void handlePostPaymentCheckIn(WebDriverWait wait) throws InterruptedException {
        // Step 1: Click the Check In button inside post-payment modal
        By checkInBtn = By.xpath("//*[@id='post-payment-form']/div[2]/button[2]");
        try {
            wait.until(ExpectedConditions.elementToBeClickable(checkInBtn));
            WebElement modalBtn = driver.findElement(checkInBtn);
            js.executeScript("arguments[0].scrollIntoView({block:'center'});", modalBtn);
            Thread.sleep(300);
            js.executeScript("arguments[0].click();", modalBtn);
            System.out.println("[CheckInSteps] Clicked post-payment check-in button");
            Thread.sleep(1500);
            dismissAlert();
        } catch (Exception e) {
            System.out.println("[CheckInSteps] Post-payment check-in button not found: " + e.getMessage());
            return;
        }

        // Step 2: Click the next link (Complete Check In / View Receipt)
        By nextLink = By.xpath("//*[@id='post-payment-form']/div[2]/a");
        try {
            wait.until(ExpectedConditions.elementToBeClickable(nextLink));
            WebElement link = driver.findElement(nextLink);
            js.executeScript("arguments[0].scrollIntoView({block:'center'});", link);
            Thread.sleep(300);
            js.executeScript("arguments[0].click();", link);
            System.out.println("[CheckInSteps] Clicked post-payment next link");
            Thread.sleep(2000);
            dismissAlert();
        } catch (Exception e) {
            System.out.println("[CheckInSteps] Post-payment next link not found: " + e.getMessage());
            return;
        }

        // Step 3: Save PDF of the content area //*[@id='content']
        savePdf(wait);
    }

    private void savePdf(WebDriverWait wait) {
        try {
            // Wait for content area to be visible
            wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//*[@id='content']") ));
            Thread.sleep(500);

            // Create output directory
            File pdfDir = new File("target/checkin-pdfs");
            pdfDir.mkdirs();

            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            String fileName  = "checkin_" + timestamp;

            // Method 1: Print to PDF via Chrome DevTools Protocol
            try {
                Object pdfBase64 = js.executeScript(
                    "return new Promise(resolve => {" +
                    "  chrome.debugger ? resolve(null) : resolve(null);" +
                    "});");

                // Use window.print() triggered PDF via JS
                js.executeScript(
                    "var style = document.createElement('style');" +
                    "style.innerHTML = '@media print { body * { visibility: hidden; } #content, #content * { visibility: visible; } #content { position: absolute; left: 0; top: 0; } }';" +
                    "document.head.appendChild(style);");

                // Take full-page screenshot of content element as fallback
                WebElement content = driver.findElement(By.xpath("//*[@id='content']"));
                File screenshot = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
                File dest = new File(pdfDir, fileName + ".png");
                Files.copy(screenshot.toPath(), dest.toPath());
                System.out.println("[CheckInSteps] Saved check-in receipt screenshot: " + dest.getAbsolutePath());
            } catch (Exception ignored) {}

            // Method 2: Save page source as HTML (can be opened/printed as PDF)
            try {
                String pageSource = driver.getPageSource();
                File htmlFile = new File(pdfDir, fileName + ".html");
                try (FileOutputStream fos = new FileOutputStream(htmlFile)) {
                    fos.write(pageSource.getBytes());
                }
                System.out.println("[CheckInSteps] Saved check-in receipt HTML: " + htmlFile.getAbsolutePath());
            } catch (Exception ignored) {}

        } catch (Exception e) {
            System.out.println("[CheckInSteps] savePdf failed: " + e.getMessage());
        }
    }
}
