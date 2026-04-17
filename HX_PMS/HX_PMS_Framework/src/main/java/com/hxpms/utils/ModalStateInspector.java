package com.hxpms.utils;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import java.time.Duration;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class ModalStateInspector {
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

            // Fill + search
            wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("checkin_date")));
            String today    = LocalDate.now().format(DateTimeFormatter.ofPattern("MM/dd/yyyy"));
            String tomorrow = LocalDate.now().plusDays(1).format(DateTimeFormatter.ofPattern("MM/dd/yyyy"));
            js.executeScript("var e=document.getElementById('checkin_date');e.value=arguments[0];e.dispatchEvent(new Event('input',{bubbles:true}));e.dispatchEvent(new Event('change',{bubbles:true}));", today);
            Thread.sleep(400);
            js.executeScript("var e=document.getElementById('checkout_date');e.value=arguments[0];e.dispatchEvent(new Event('input',{bubbles:true}));e.dispatchEvent(new Event('change',{bubbles:true}));", tomorrow);
            Thread.sleep(600);
            js.executeScript("var n=document.getElementById('stay-nights');n.value='1';n.dispatchEvent(new Event('input',{bubbles:true}));n.dispatchEvent(new Event('change',{bubbles:true}));");
            driver.findElement(By.cssSelector("button.btn.btn-warning.search")).click();

            // Wait for room_types buttons
            new WebDriverWait(driver, Duration.ofSeconds(30))
                .until(ExpectedConditions.presenceOfElementLocated(By.xpath("//button[contains(@class,'room_types')]")));
            Thread.sleep(500);

            List<WebElement> rtBtns = driver.findElements(By.xpath("//button[contains(@class,'room_types')]"));
            System.out.println("room_types count=" + rtBtns.size());

            // Print all room_types button attributes
            for (int i = 0; i < Math.min(3, rtBtns.size()); i++) {
                WebElement b = rtBtns.get(i);
                System.out.println("room_types[" + i + "] class='" + b.getAttribute("class") +
                    "' data-bs-target='" + b.getAttribute("data-bs-target") +
                    "' data-bs-toggle='" + b.getAttribute("data-bs-toggle") +
                    "' data-target='" + b.getAttribute("data-target") +
                    "' data-toggle='" + b.getAttribute("data-toggle") +
                    "' onclick='" + b.getAttribute("onclick") + "'");
            }

            // Click first room_types button
            js.executeScript("arguments[0].scrollIntoView(true);arguments[0].click();", rtBtns.get(0));
            System.out.println("Clicked room_types[0]");

            // Poll modal state every 500ms for 10s
            for (int i = 0; i < 20; i++) {
                Thread.sleep(500);
                WebElement modal = driver.findElement(By.id("room-types-info-modal"));
                String cls = modal.getAttribute("class");
                String display = (String) js.executeScript("return window.getComputedStyle(arguments[0]).display;", modal);
                String visibility = (String) js.executeScript("return window.getComputedStyle(arguments[0]).visibility;", modal);
                String bodyHtml = (String) js.executeScript("return document.querySelector('#room-types-info-modal .modal-body').innerHTML.trim().substring(0,200);");
                System.out.println("[" + (i*500) + "ms] class='" + cls + "' display=" + display + " visibility=" + visibility + " bodyLen=" + bodyHtml.length() + " body='" + bodyHtml.replace("\n","") + "'");
                if (cls.contains("show") || display.equals("block")) {
                    System.out.println(">>> MODAL IS OPEN at " + (i*500) + "ms");
                    break;
                }
            }

            // Dump all buttons now
            System.out.println("\n=== ALL VISIBLE BUTTONS ===");
            for (WebElement b : driver.findElements(By.tagName("button"))) {
                try { if (b.isDisplayed()) System.out.println("BTN class='" + b.getAttribute("class") + "' text='" + b.getText().trim().replace("\n"," ") + "'"); }
                catch (Exception ignored) {}
            }

            // Dump all visible inputs
            System.out.println("\n=== ALL VISIBLE INPUTS ===");
            for (WebElement el : driver.findElements(By.tagName("input"))) {
                try { if (el.isDisplayed()) System.out.println("INPUT id='" + el.getAttribute("id") + "' name='" + el.getAttribute("name") + "'"); }
                catch (Exception ignored) {}
            }
            for (WebElement el : driver.findElements(By.tagName("select"))) {
                try { if (el.isDisplayed()) System.out.println("SELECT id='" + el.getAttribute("id") + "' name='" + el.getAttribute("name") + "'"); }
                catch (Exception ignored) {}
            }

            Thread.sleep(3000);
        } finally {
            driver.quit();
        }
    }
}
