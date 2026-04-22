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

public class SearchTriggerInspector {
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

            // Print current field values
            System.out.println("=== INITIAL FIELD VALUES ===");
            System.out.println("checkin_date  = " + driver.findElement(By.id("checkin_date")).getAttribute("value"));
            System.out.println("checkout_date = " + driver.findElement(By.id("checkout_date")).getAttribute("value"));
            System.out.println("stay-nights   = " + driver.findElement(By.id("stay-nights")).getAttribute("value"));
            System.out.println("booking_type  = " + new Select(driver.findElement(By.id("booking_type"))).getFirstSelectedOption().getText());
            System.out.println("rooms         = " + new Select(driver.findElement(By.id("rooms"))).getFirstSelectedOption().getText());

            // Print search button attributes
            WebElement searchBtn = driver.findElement(By.cssSelector("button.btn.btn-warning.search"));
            System.out.println("\n=== SEARCH BUTTON ===");
            System.out.println("class=" + searchBtn.getAttribute("class"));
            System.out.println("type=" + searchBtn.getAttribute("type"));
            System.out.println("onclick=" + searchBtn.getAttribute("onclick"));
            System.out.println("data-action=" + searchBtn.getAttribute("data-action"));

            // Print form attributes
            System.out.println("\n=== FORM ELEMENT ===");
            List<WebElement> forms = driver.findElements(By.tagName("form"));
            for (WebElement form : forms) {
                System.out.println("form id='" + form.getAttribute("id") + "' action='" + form.getAttribute("action") + "' method='" + form.getAttribute("method") + "'");
            }

            // Try clicking search and monitor network/DOM for 15s
            System.out.println("\n=== CLICKING SEARCH ===");
            searchBtn.click();

            for (int i = 0; i < 30; i++) {
                Thread.sleep(500);
                List<WebElement> rt = driver.findElements(By.xpath("//button[contains(@class,'room_types')]"));
                List<WebElement> rb = driver.findElements(By.xpath("//button[contains(@class,'room-btn')]"));
                List<WebElement> gt = driver.findElements(By.id("guest_type"));
                boolean gtVis = !gt.isEmpty() && gt.get(0).isDisplayed();
                // Check for any error/alert messages
                List<WebElement> alerts = driver.findElements(By.cssSelector(".alert-danger, .alert-warning, .toast-error, [class*='error']"));
                String alertText = "";
                for (WebElement a : alerts) {
                    try { if (a.isDisplayed()) alertText += a.getText().trim() + " | "; } catch (Exception ignored) {}
                }
                System.out.println("[" + (i*500) + "ms] room_types=" + rt.size() + " room-btn=" + rb.size() + " guest_type=" + gtVis + (alertText.isEmpty() ? "" : " ALERT: " + alertText));
                if (!rt.isEmpty() || !rb.isEmpty() || gtVis) {
                    System.out.println(">>> RESULTS FOUND");
                    break;
                }
            }

            // Print page source snippet around search results area
            System.out.println("\n=== PAGE SOURCE SNIPPET (room results area) ===");
            String src = driver.getPageSource();
            // Look for accordion or room type content
            for (String keyword : new String[]{"accordion", "room_types", "room-btn", "room-type", "heading-type"}) {
                int idx = src.indexOf(keyword);
                if (idx >= 0) {
                    System.out.println("Found '" + keyword + "' at idx=" + idx);
                    System.out.println(src.substring(Math.max(0, idx-50), Math.min(src.length(), idx+200)));
                    System.out.println("---");
                    break;
                }
            }

            Thread.sleep(3000);
        } finally {
            driver.quit();
        }
    }
}
