package com.hxpms.utils;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import java.time.Duration;
import java.util.List;

public class BookRoomInspector {
    public static void main(String[] args) throws Exception {
        WebDriverManager.chromedriver().setup();
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--no-sandbox", "--disable-dev-shm-usage");
        WebDriver driver = new ChromeDriver(options);
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(20));

        try {
            // Login
            driver.get("https://demo.hotelxplore.com/frontdesk");
            wait.until(ExpectedConditions.visibilityOfElementLocated(By.name("username")));
            driver.findElement(By.name("username")).sendKeys("webdev");
            driver.findElement(By.name("password")).sendKeys("1234");
            driver.findElement(By.cssSelector("button[type='submit']")).click();
            wait.until(ExpectedConditions.urlContains("dashboard"));
            System.out.println("=== Logged in ===");

            // Click Walk In
            By walkIn = By.xpath("//a[contains(@class,'btn-theam1') and normalize-space()='Walk In']");
            wait.until(ExpectedConditions.elementToBeClickable(walkIn));
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", driver.findElement(walkIn));
            try { wait.until(ExpectedConditions.urlContains("book-room")); }
            catch (Exception e) { driver.get("https://demo.hotelxplore.com/frontdesk/book-room?book-type=Walk%20In"); }
            System.out.println("=== On book-room page: " + driver.getCurrentUrl() + " ===");

            // Dump all inputs and selects on the search form
            System.out.println("\n--- ALL INPUTS ---");
            for (WebElement el : driver.findElements(By.tagName("input"))) {
                System.out.println("INPUT id='" + el.getAttribute("id") + "' name='" + el.getAttribute("name") + "' type='" + el.getAttribute("type") + "' placeholder='" + el.getAttribute("placeholder") + "'");
            }
            System.out.println("\n--- ALL SELECTS ---");
            for (WebElement el : driver.findElements(By.tagName("select"))) {
                System.out.println("SELECT id='" + el.getAttribute("id") + "' name='" + el.getAttribute("name") + "'");
                for (WebElement opt : el.findElements(By.tagName("option"))) {
                    System.out.println("  OPTION value='" + opt.getAttribute("value") + "' text='" + opt.getText() + "'");
                }
            }
            System.out.println("\n--- ALL BUTTONS ---");
            for (WebElement el : driver.findElements(By.tagName("button"))) {
                System.out.println("BUTTON id='" + el.getAttribute("id") + "' class='" + el.getAttribute("class") + "' text='" + el.getText().trim().replace("\n"," ") + "'");
            }

            // Now fill dates and search
            Thread.sleep(1000);
            WebElement checkIn = driver.findElement(By.id("checkin_date"));
            WebElement checkOut = driver.findElement(By.id("checkout_date"));
            ((JavascriptExecutor) driver).executeScript("arguments[0].value=arguments[1]; arguments[0].dispatchEvent(new Event('change'));", checkIn, java.time.LocalDate.now().format(java.time.format.DateTimeFormatter.ofPattern("MM-dd-yyyy")));
            ((JavascriptExecutor) driver).executeScript("arguments[0].value=arguments[1]; arguments[0].dispatchEvent(new Event('change'));", checkOut, java.time.LocalDate.now().plusDays(1).format(java.time.format.DateTimeFormatter.ofPattern("MM-dd-yyyy")));

            // Click search
            try {
                By searchBtn = By.xpath("//button[@class='btn btn-warning w-100 h-45 search']");
                wait.until(ExpectedConditions.elementToBeClickable(searchBtn));
                driver.findElement(searchBtn).click();
            } catch (Exception e) {
                driver.findElement(By.xpath("//button[contains(normalize-space(),'Search')]")).click();
            }
            System.out.println("\n=== After search click ===");
            Thread.sleep(3000);

            // Dump room results
            System.out.println("\n--- ROOM RESULT BUTTONS ---");
            for (WebElement el : driver.findElements(By.tagName("button"))) {
                try {
                    if (el.isDisplayed()) System.out.println("BUTTON id='" + el.getAttribute("id") + "' class='" + el.getAttribute("class") + "' text='" + el.getText().trim().replace("\n"," ") + "'");
                } catch (Exception ignored) {}
            }

            // Click first room button
            List<WebElement> roomBtns = driver.findElements(By.xpath("//button[contains(@class,'room_types')]"));
            if (roomBtns.isEmpty()) roomBtns = driver.findElements(By.xpath("//button[contains(@class,'btn-outline-dark')]"));
            if (!roomBtns.isEmpty()) {
                ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true); arguments[0].click();", roomBtns.get(0));
                System.out.println("\n=== Clicked room button: " + roomBtns.get(0).getText() + " ===");
                Thread.sleep(2000);
            }

            // Dump guest form
            System.out.println("\n--- GUEST FORM INPUTS ---");
            for (WebElement el : driver.findElements(By.tagName("input"))) {
                try { if (el.isDisplayed()) System.out.println("INPUT id='" + el.getAttribute("id") + "' name='" + el.getAttribute("name") + "'"); } catch (Exception ignored) {}
            }
            for (WebElement el : driver.findElements(By.tagName("select"))) {
                try { if (el.isDisplayed()) System.out.println("SELECT id='" + el.getAttribute("id") + "' name='" + el.getAttribute("name") + "'"); } catch (Exception ignored) {}
            }

            // Try Next/Proceed to Payment
            System.out.println("\n--- BUTTONS AFTER ROOM SELECT ---");
            for (WebElement el : driver.findElements(By.tagName("button"))) {
                try { if (el.isDisplayed()) System.out.println("BUTTON id='" + el.getAttribute("id") + "' class='" + el.getAttribute("class") + "' text='" + el.getText().trim().replace("\n"," ") + "'"); } catch (Exception ignored) {}
            }

        } finally {
            Thread.sleep(3000);
            driver.quit();
        }
    }
}
