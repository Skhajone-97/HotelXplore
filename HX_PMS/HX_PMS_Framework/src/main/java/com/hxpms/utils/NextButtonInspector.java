package com.hxpms.utils;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;
import java.time.Duration;
import java.util.List;

public class NextButtonInspector {
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

            // Walk In
            By walkIn = By.xpath("//a[contains(@class,'btn-theam1') and normalize-space()='Walk In']");
            wait.until(ExpectedConditions.elementToBeClickable(walkIn));
            js.executeScript("arguments[0].click();", driver.findElement(walkIn));
            try { wait.until(ExpectedConditions.urlContains("book-room")); }
            catch (Exception e) { driver.get("https://demo.hotelxplore.com/frontdesk/book-room?book-type=Walk%20In"); }
            wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("checkin_date")));
            Thread.sleep(1000);

            // Search
            js.executeScript("arguments[0].click();", driver.findElement(By.cssSelector("button.btn.btn-warning.search")));
            Thread.sleep(2000);

            // Click first room_types
            new WebDriverWait(driver, Duration.ofSeconds(15)).until(
                ExpectedConditions.presenceOfElementLocated(By.xpath("//button[contains(@class,'room_types')]")));
            js.executeScript("arguments[0].click();",
                driver.findElements(By.xpath("//button[contains(@class,'room_types')]")).get(0));
            Thread.sleep(1500);

            // Click first available room-btn
            List<WebElement> roomBtns = driver.findElements(By.xpath("//button[contains(@class,'room-btn') and contains(@class,'available')]"));
            if (!roomBtns.isEmpty()) {
                js.executeScript("arguments[0].click();", roomBtns.get(0));
                Thread.sleep(1000);
            }

            // Close modal
            js.executeScript(
                "var m=document.getElementById('room-types-info-modal');" +
                "if(m){try{bootstrap.Modal.getInstance(m).hide();}catch(e){m.classList.remove('show');m.style.display='none';}}" +
                "document.querySelectorAll('.modal-backdrop').forEach(b=>b.remove());" +
                "document.body.classList.remove('modal-open');");
            Thread.sleep(800);

            // Fill guest form
            wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("guest_type")));
            new Select(driver.findElement(By.id("guest_type"))).selectByVisibleText("New Guest");
            Thread.sleep(300);
            wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("first_name")));
            driver.findElement(By.id("first_name")).sendKeys("Jully");
            driver.findElement(By.id("last_name")).sendKeys("Williams");
            driver.findElement(By.id("email")).sendKeys("jully.williams@example.com");
            driver.findElement(By.id("mobile")).sendKeys("5551234567");
            driver.findElement(By.id("address")).sendKeys("123 Main Street");
            driver.findElement(By.id("zip")).sendKeys("10001");
            System.out.println("Guest form filled");

            // Dump ALL buttons inside booking-form-div
            System.out.println("\n=== ALL BUTTONS IN booking-form-div ===");
            try {
                WebElement formDiv = driver.findElement(By.id("booking-form-div"));
                List<WebElement> btns = formDiv.findElements(By.tagName("button"));
                System.out.println("Total buttons: " + btns.size());
                for (int i = 0; i < btns.size(); i++) {
                    WebElement b = btns.get(i);
                    System.out.println("[" + (i+1) + "] text='" + b.getText().trim().replace("\n"," ") +
                        "' class='" + b.getAttribute("class") +
                        "' disabled='" + b.getAttribute("disabled") +
                        "' displayed=" + b.isDisplayed() +
                        "' enabled=" + b.isEnabled());
                }
            } catch (Exception e) { System.out.println("booking-form-div error: " + e.getMessage()); }

            // Dump ALL visible buttons on page
            System.out.println("\n=== ALL VISIBLE BUTTONS ON PAGE ===");
            for (WebElement b : driver.findElements(By.tagName("button"))) {
                try {
                    if (b.isDisplayed())
                        System.out.println("BTN text='" + b.getText().trim().replace("\n"," ") +
                            "' class='" + b.getAttribute("class") +
                            "' disabled='" + b.getAttribute("disabled") + "'");
                } catch (Exception ignored) {}
            }

            // Try user's xpath
            System.out.println("\n=== USER XPATH TEST ===");
            String[] xpaths = {
                "//*[@id='booking-form-div']/div/div/div/div[4]/div/button[1]",
                "//*[@id='booking-form-div']/div[1]/div[1]/div[1]/div[4]/div[1]/button[1]",
                "//*[@id='booking-form-div']//button[contains(@class,'payment-btn')]",
                "//*[@id='booking-form-div']//button[contains(normalize-space(),'Next')]",
                "//button[contains(@class,'payment-btn') and not(contains(@class,'d-none'))]",
                "//button[contains(@class,'btn-book') and contains(normalize-space(),'Next')]"
            };
            for (String xpath : xpaths) {
                List<WebElement> found = driver.findElements(By.xpath(xpath));
                System.out.println("xpath='" + xpath + "' found=" + found.size());
                for (WebElement b : found) {
                    System.out.println("  -> text='" + b.getText().trim() + "' class='" + b.getAttribute("class") + "' displayed=" + b.isDisplayed() + " enabled=" + b.isEnabled() + " disabled=" + b.getAttribute("disabled"));
                }
            }

            // Print booking-form-div HTML snippet
            System.out.println("\n=== booking-form-div HTML (last 1000 chars) ===");
            try {
                String html = driver.findElement(By.id("booking-form-div")).getAttribute("outerHTML");
                System.out.println(html.substring(Math.max(0, html.length()-1500)));
            } catch (Exception e) { System.out.println("Error: " + e.getMessage()); }

            Thread.sleep(3000);
        } finally {
            driver.quit();
        }
    }
}
