package com.hxpms.utils;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import java.time.Duration;
import java.util.List;

public class ArrivalInspector {
    public static void main(String[] args) throws Exception {
        WebDriverManager.chromedriver().setup();
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--no-sandbox", "--disable-dev-shm-usage");
        WebDriver driver = new ChromeDriver(options);
        JavascriptExecutor js = (JavascriptExecutor) driver;
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(20));

        try {
            // Login
            driver.get("https://demo.hotelxplore.com/frontdesk");
            wait.until(ExpectedConditions.visibilityOfElementLocated(By.name("username")));
            driver.findElement(By.name("username")).sendKeys("webdev");
            driver.findElement(By.name("password")).sendKeys("1234");
            driver.findElement(By.cssSelector("button[type='submit']")).click();
            wait.until(ExpectedConditions.urlContains("dashboard"));

            // Go to arrivals
            driver.get("https://demo.hotelxplore.com/frontdesk/booking/todays/check-in");
            wait.until(ExpectedConditions.urlContains("check-in"));
            Thread.sleep(2000);

            System.out.println("=== PAGE TITLE: " + driver.getTitle() + " ===");
            System.out.println("=== URL: " + driver.getCurrentUrl() + " ===");

            // Count all check-in buttons with user's xpath
            List<WebElement> checkInBtns = driver.findElements(
                By.xpath("//a[@class='btn btn-warning float-end mb-3']"));
            System.out.println("\n=== CHECK-IN BUTTONS (user xpath): " + checkInBtns.size() + " ===");
            for (int i = 0; i < checkInBtns.size(); i++) {
                System.out.println("  [" + (i+1) + "] text='" + checkInBtns.get(i).getText().trim() +
                    "' displayed=" + checkInBtns.get(i).isDisplayed());
            }

            // Count table rows
            List<WebElement> rows = driver.findElements(By.cssSelector("table tbody tr"));
            System.out.println("\n=== TABLE ROWS: " + rows.size() + " ===");
            for (int i = 0; i < Math.min(rows.size(), 10); i++) {
                System.out.println("  row[" + (i+1) + "] text='" + rows.get(i).getText().trim().replace("\n", " | ") + "'");
            }

            // Count guest cards/divs
            List<WebElement> cards = driver.findElements(By.cssSelector(".card, .booking-card, .guest-card, [class*='booking'], [class*='guest']"));
            System.out.println("\n=== GUEST CARDS: " + cards.size() + " ===");

            // Dump all visible buttons and links
            System.out.println("\n=== ALL VISIBLE BUTTONS/LINKS ===");
            for (WebElement el : driver.findElements(By.xpath("//a[contains(@class,'btn')] | //button[contains(@class,'btn')]"))) {
                try {
                    if (el.isDisplayed())
                        System.out.println("  " + el.getTagName() + " class='" + el.getAttribute("class") +
                            "' text='" + el.getText().trim() + "'");
                } catch (Exception ignored) {}
            }

            // Print page source snippet around 'check-in' or 'checkin'
            System.out.println("\n=== PAGE SOURCE SNIPPET ===");
            String src = driver.getPageSource();
            int idx = src.toLowerCase().indexOf("float-end mb-3");
            if (idx >= 0) System.out.println(src.substring(Math.max(0, idx-200), Math.min(src.length(), idx+400)));
            else System.out.println("'float-end mb-3' not found in source");

            Thread.sleep(3000);
        } finally {
            driver.quit();
        }
    }
}
