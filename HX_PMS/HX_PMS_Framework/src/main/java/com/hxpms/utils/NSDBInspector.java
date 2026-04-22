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

public class NSDBInspector {
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

            // Fill dates + search
            wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("checkin_date")));
            Thread.sleep(500);
            new Select(driver.findElement(By.id("booking_type"))).selectByVisibleText("Walk In");
            new Select(driver.findElement(By.id("rooms"))).selectByValue("1");
            driver.findElement(By.cssSelector("button.btn.btn-warning.search")).click();

            // Wait for room_types
            new WebDriverWait(driver, Duration.ofSeconds(30)).until(
                ExpectedConditions.presenceOfElementLocated(By.xpath("//button[contains(@class,'room_types')]")));
            Thread.sleep(500);

            // Dump all room_types buttons
            System.out.println("=== ROOM TYPE BUTTONS ===");
            List<WebElement> rtBtns = driver.findElements(By.xpath("//button[contains(@class,'room_types')]"));
            for (int i = 0; i < rtBtns.size(); i++) {
                System.out.println("[" + i + "] text='" + rtBtns.get(i).getText().trim().replace("\n"," ") +
                    "' class='" + rtBtns.get(i).getAttribute("class") + "'");
            }

            // Click NSDB heading
            By nsdbHeading = By.xpath("//*[@id='heading-type-01955b6e-aaaa-0ca1-ebd7-d9b141d0ceb4']/button");
            try {
                wait.until(ExpectedConditions.elementToBeClickable(nsdbHeading));
                js.executeScript("arguments[0].scrollIntoView(true);arguments[0].click();", driver.findElement(nsdbHeading));
                System.out.println("\n=== Clicked NSDB heading ===");
                Thread.sleep(1500);
            } catch (Exception e) {
                System.out.println("NSDB heading not found: " + e.getMessage());
            }

            // Dump collapse panel HTML
            System.out.println("\n=== NSDB COLLAPSE PANEL HTML ===");
            try {
                WebElement panel = driver.findElement(By.id("collapse-type-01955b6e-aaaa-0ca1-ebd7-d9b141d0ceb4"));
                String html = panel.getAttribute("outerHTML");
                System.out.println(html.substring(0, Math.min(3000, html.length())));
            } catch (Exception e) { System.out.println("Panel not found: " + e.getMessage()); }

            // Dump all buttons inside collapse panel
            System.out.println("\n=== BUTTONS INSIDE NSDB COLLAPSE PANEL ===");
            try {
                List<WebElement> panelBtns = driver.findElements(
                    By.xpath("//*[@id='collapse-type-01955b6e-aaaa-0ca1-ebd7-d9b141d0ceb4']//button"));
                System.out.println("Total buttons: " + panelBtns.size());
                for (int i = 0; i < panelBtns.size(); i++) {
                    System.out.println("[" + (i+1) + "] class='" + panelBtns.get(i).getAttribute("class") +
                        "' text='" + panelBtns.get(i).getText().trim().replace("\n"," ") +
                        "' displayed=" + panelBtns.get(i).isDisplayed());
                }
            } catch (Exception e) { System.out.println("Error: " + e.getMessage()); }

            // Dump all divs structure inside collapse
            System.out.println("\n=== DIV STRUCTURE INSIDE NSDB COLLAPSE ===");
            try {
                List<WebElement> divs = driver.findElements(
                    By.xpath("//*[@id='collapse-type-01955b6e-aaaa-0ca1-ebd7-d9b141d0ceb4']/div/div/div"));
                System.out.println("div count: " + divs.size());
                for (int i = 0; i < divs.size(); i++) {
                    System.out.println("div[" + (i+1) + "] class='" + divs.get(i).getAttribute("class") +
                        "' text='" + divs.get(i).getText().trim().substring(0, Math.min(80, divs.get(i).getText().trim().length())) + "'");
                }
            } catch (Exception e) { System.out.println("Error: " + e.getMessage()); }

            Thread.sleep(3000);
        } finally {
            driver.quit();
        }
    }
}
