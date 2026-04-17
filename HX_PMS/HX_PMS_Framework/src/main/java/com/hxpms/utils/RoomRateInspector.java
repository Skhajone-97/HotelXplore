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

public class RoomRateInspector {
    public static void main(String[] args) throws Exception {
        WebDriverManager.chromedriver().setup();
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--no-sandbox", "--disable-dev-shm-usage");
        WebDriver driver = new ChromeDriver(options);
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(20));
        JavascriptExecutor js = (JavascriptExecutor) driver;

        try {
            // Login
            driver.get("https://demo.hotelxplore.com/frontdesk");
            wait.until(ExpectedConditions.visibilityOfElementLocated(By.name("username")));
            driver.findElement(By.name("username")).sendKeys("webdev");
            driver.findElement(By.name("password")).sendKeys("1234");
            driver.findElement(By.cssSelector("button[type='submit']")).click();
            wait.until(ExpectedConditions.urlContains("dashboard"));

            // Go to Walk In
            By walkIn = By.xpath("//a[contains(@class,'btn-theam1') and normalize-space()='Walk In']");
            wait.until(ExpectedConditions.elementToBeClickable(walkIn));
            js.executeScript("arguments[0].click();", driver.findElement(walkIn));
            try { wait.until(ExpectedConditions.urlContains("book-room")); }
            catch (Exception e) { driver.get("https://demo.hotelxplore.com/frontdesk/book-room?book-type=Walk%20In"); }

            // Fill dates
            wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("checkin_date")));
            String today    = LocalDate.now().format(DateTimeFormatter.ofPattern("MM/dd/yyyy"));
            String tomorrow = LocalDate.now().plusDays(1).format(DateTimeFormatter.ofPattern("MM/dd/yyyy"));
            js.executeScript("arguments[0].value=arguments[1]; arguments[0].dispatchEvent(new Event('change',{bubbles:true}));", driver.findElement(By.id("checkin_date")), today);
            js.executeScript("arguments[0].value=arguments[1]; arguments[0].dispatchEvent(new Event('change',{bubbles:true}));", driver.findElement(By.id("checkout_date")), tomorrow);
            Thread.sleep(500);
            js.executeScript("var n=document.getElementById('stay-nights'); n.value='1'; n.dispatchEvent(new Event('change',{bubbles:true}));");

            // Click Search
            driver.findElement(By.cssSelector("button.btn.btn-warning.search")).click();
            Thread.sleep(3000);

            // Click first room_types button by index via JS to avoid stale
            List<WebElement> roomBtns = driver.findElements(By.xpath("//button[contains(@class,'room_types')]"));
            if (roomBtns.isEmpty()) roomBtns = driver.findElements(By.xpath("//button[contains(@class,'btn-outline-dark')]"));
            System.out.println("=== Room buttons found: " + roomBtns.size() + " ===");
            if (!roomBtns.isEmpty()) {
                js.executeScript("arguments[0].scrollIntoView(true); arguments[0].click();", roomBtns.get(0));
                System.out.println("Clicked first room button");
                Thread.sleep(2000);
            }

            // Dump all visible modals
            System.out.println("\n=== MODALS AFTER ROOM CLICK ===");
            for (WebElement modal : driver.findElements(By.cssSelector(".modal.show, .modal-dialog"))) {
                System.out.println("MODAL class='" + modal.getAttribute("class") + "' id='" + modal.getAttribute("id") + "'");
            }

            // Dump all visible buttons
            System.out.println("\n=== VISIBLE BUTTONS AFTER ROOM CLICK ===");
            for (WebElement btn : driver.findElements(By.tagName("button"))) {
                try {
                    if (btn.isDisplayed())
                        System.out.println("BUTTON class='" + btn.getAttribute("class") + "' id='" + btn.getAttribute("id") + "' text='" + btn.getText().trim().replace("\n"," ") + "'");
                } catch (Exception ignored) {}
            }

            // Dump all visible inputs/selects
            System.out.println("\n=== VISIBLE INPUTS AFTER ROOM CLICK ===");
            for (WebElement el : driver.findElements(By.tagName("input"))) {
                try {
                    if (el.isDisplayed())
                        System.out.println("INPUT id='" + el.getAttribute("id") + "' name='" + el.getAttribute("name") + "' type='" + el.getAttribute("type") + "'");
                } catch (Exception ignored) {}
            }
            for (WebElement el : driver.findElements(By.tagName("select"))) {
                try {
                    if (el.isDisplayed())
                        System.out.println("SELECT id='" + el.getAttribute("id") + "' name='" + el.getAttribute("name") + "'");
                } catch (Exception ignored) {}
            }

            // Now look for rate plan buttons inside the modal - re-fetch fresh
            System.out.println("\n=== RATE PLAN BUTTONS ===");
            List<WebElement> rateBtns = driver.findElements(By.xpath("//button[contains(@class,'rate') or contains(@class,'plan') or contains(@class,'room_types')]"));
            for (WebElement btn : rateBtns) {
                try { System.out.println("RATE BTN class='" + btn.getAttribute("class") + "' text='" + btn.getText().trim().replace("\n"," ") + "'"); } catch (Exception ignored) {}
            }

            // Click first rate plan if any - re-fetch fresh list
            List<WebElement> freshRateBtns = driver.findElements(By.xpath("//button[contains(@class,'rate') or contains(@class,'plan') or contains(@class,'room_types')]"));
            if (!freshRateBtns.isEmpty()) {
                js.executeScript("arguments[0].scrollIntoView(true); arguments[0].click();", freshRateBtns.get(0));
                System.out.println("\nClicked rate plan button");
                Thread.sleep(2000);

                System.out.println("\n=== AFTER RATE PLAN CLICK - MODALS ===");
                for (WebElement modal : driver.findElements(By.cssSelector(".modal.show"))) {
                    System.out.println("MODAL id='" + modal.getAttribute("id") + "' class='" + modal.getAttribute("class") + "'");
                }

                System.out.println("\n=== AFTER RATE PLAN CLICK - BUTTONS ===");
                for (WebElement btn : driver.findElements(By.tagName("button"))) {
                    try {
                        if (btn.isDisplayed())
                            System.out.println("BUTTON class='" + btn.getAttribute("class") + "' id='" + btn.getAttribute("id") + "' text='" + btn.getText().trim().replace("\n"," ") + "'");
                    } catch (Exception ignored) {}
                }

                System.out.println("\n=== AFTER RATE PLAN CLICK - CLOSE BUTTONS ===");
                for (WebElement btn : driver.findElements(By.xpath("//*[contains(@class,'close') or contains(@class,'btn-close') or @data-dismiss='modal' or @data-bs-dismiss='modal']"))) {
                    try {
                        System.out.println("CLOSE el='" + btn.getTagName() + "' class='" + btn.getAttribute("class") + "' data-dismiss='" + btn.getAttribute("data-dismiss") + "' data-bs-dismiss='" + btn.getAttribute("data-bs-dismiss") + "'");
                    } catch (Exception ignored) {}
                }

                System.out.println("\n=== AFTER RATE PLAN CLICK - VISIBLE INPUTS ===");
                for (WebElement el : driver.findElements(By.tagName("input"))) {
                    try { if (el.isDisplayed()) System.out.println("INPUT id='" + el.getAttribute("id") + "' name='" + el.getAttribute("name") + "'"); } catch (Exception ignored) {}
                }
                for (WebElement el : driver.findElements(By.tagName("select"))) {
                    try { if (el.isDisplayed()) System.out.println("SELECT id='" + el.getAttribute("id") + "' name='" + el.getAttribute("name") + "'"); } catch (Exception ignored) {}
                }
            }

            Thread.sleep(3000);
        } finally {
            driver.quit();
        }
    }
}
