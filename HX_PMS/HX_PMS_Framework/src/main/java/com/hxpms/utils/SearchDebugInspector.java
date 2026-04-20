package com.hxpms.utils;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;
import java.time.Duration;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class SearchDebugInspector {
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

            String today    = LocalDate.now().format(DateTimeFormatter.ofPattern("MM/dd/yyyy"));
            String tomorrow = LocalDate.now().plusDays(1).format(DateTimeFormatter.ofPattern("MM/dd/yyyy"));

            // Print initial field values
            System.out.println("=== BEFORE FILL ===");
            System.out.println("checkin_date value  = " + driver.findElement(By.id("checkin_date")).getAttribute("value"));
            System.out.println("checkout_date value = " + driver.findElement(By.id("checkout_date")).getAttribute("value"));
            System.out.println("stay-nights value   = " + driver.findElement(By.id("stay-nights")).getAttribute("value"));

            // Method 1: JS set value + events
            js.executeScript("var e=document.getElementById('checkin_date');e.value=arguments[0];e.dispatchEvent(new Event('input',{bubbles:true}));e.dispatchEvent(new Event('change',{bubbles:true}));", today);
            Thread.sleep(500);
            js.executeScript("var e=document.getElementById('checkout_date');e.value=arguments[0];e.dispatchEvent(new Event('input',{bubbles:true}));e.dispatchEvent(new Event('change',{bubbles:true}));", tomorrow);
            Thread.sleep(500);

            System.out.println("\n=== AFTER JS SET ===");
            System.out.println("checkin_date value  = " + driver.findElement(By.id("checkin_date")).getAttribute("value"));
            System.out.println("checkout_date value = " + driver.findElement(By.id("checkout_date")).getAttribute("value"));
            System.out.println("stay-nights value   = " + driver.findElement(By.id("stay-nights")).getAttribute("value"));

            // Try sendKeys approach as well
            WebElement ciEl = driver.findElement(By.id("checkin_date"));
            ciEl.click(); ciEl.clear(); ciEl.sendKeys(today);
            Thread.sleep(300);
            WebElement coEl = driver.findElement(By.id("checkout_date"));
            coEl.click(); coEl.clear(); coEl.sendKeys(tomorrow);
            Thread.sleep(500);

            System.out.println("\n=== AFTER SENDKEYS ===");
            System.out.println("checkin_date value  = " + driver.findElement(By.id("checkin_date")).getAttribute("value"));
            System.out.println("checkout_date value = " + driver.findElement(By.id("checkout_date")).getAttribute("value"));
            System.out.println("stay-nights value   = " + driver.findElement(By.id("stay-nights")).getAttribute("value"));

            // Check booking_type and rooms
            System.out.println("\n=== DROPDOWNS ===");
            Select btSel = new Select(driver.findElement(By.id("booking_type")));
            System.out.println("booking_type options: ");
            for (WebElement o : btSel.getOptions()) System.out.println("  '" + o.getText() + "' value='" + o.getAttribute("value") + "'");
            Select roomsSel = new Select(driver.findElement(By.id("rooms")));
            System.out.println("rooms options: ");
            for (WebElement o : roomsSel.getOptions()) System.out.println("  '" + o.getText() + "' value='" + o.getAttribute("value") + "'");

            // Set dropdowns
            btSel.selectByVisibleText("Walk In");
            roomsSel.selectByValue("1");
            Thread.sleep(300);

            // Click search
            driver.findElement(By.cssSelector("button.btn.btn-warning.search")).click();
            System.out.println("\n=== SEARCH CLICKED ===");

            // Poll for 30s
            for (int i = 0; i < 60; i++) {
                Thread.sleep(500);
                List<WebElement> rt = driver.findElements(By.xpath("//button[contains(@class,'room_types')]"));
                List<WebElement> rb = driver.findElements(By.xpath("//button[contains(@class,'room-btn')]"));
                List<WebElement> gt = driver.findElements(By.id("guest_type"));
                boolean gtVisible = !gt.isEmpty() && gt.get(0).isDisplayed();
                System.out.println("[" + (i*500) + "ms] room_types=" + rt.size() + " room-btn=" + rb.size() + " guest_type_visible=" + gtVisible);
                if (!rt.isEmpty() || !rb.isEmpty() || gtVisible) {
                    System.out.println(">>> RESULTS FOUND at " + (i*500) + "ms");
                    break;
                }
                // Check for any error messages
                List<WebElement> alerts = driver.findElements(By.cssSelector(".alert, .error, .toast, [class*='error'], [class*='alert']"));
                for (WebElement a : alerts) {
                    try { if (a.isDisplayed()) System.out.println("  ALERT: " + a.getText().trim()); } catch (Exception ignored) {}
                }
            }

            // Final dump
            System.out.println("\n=== FINAL PAGE BUTTONS ===");
            for (WebElement b : driver.findElements(By.tagName("button"))) {
                try { if (b.isDisplayed()) System.out.println("BTN class='" + b.getAttribute("class") + "' text='" + b.getText().trim().replace("\n"," ") + "'"); }
                catch (Exception ignored) {}
            }
            System.out.println("\n=== PAGE SOURCE SNIPPET (search area) ===");
            String src = driver.getPageSource();
            int idx = src.toLowerCase().indexOf("room_types");
            if (idx >= 0) System.out.println(src.substring(Math.max(0, idx-100), Math.min(src.length(), idx+300)));
            else System.out.println("room_types not found in page source");

            Thread.sleep(3000);
        } finally {
            driver.quit();
        }
    }
}
