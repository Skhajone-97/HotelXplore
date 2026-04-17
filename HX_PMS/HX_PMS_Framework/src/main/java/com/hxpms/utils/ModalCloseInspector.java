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

public class ModalCloseInspector {
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

            // Walk In
            By walkIn = By.xpath("//a[contains(@class,'btn-theam1') and normalize-space()='Walk In']");
            wait.until(ExpectedConditions.elementToBeClickable(walkIn));
            js.executeScript("arguments[0].click();", driver.findElement(walkIn));
            try { wait.until(ExpectedConditions.urlContains("book-room")); }
            catch (Exception e) { driver.get("https://demo.hotelxplore.com/frontdesk/book-room?book-type=Walk%20In"); }

            // Fill dates + search
            wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("checkin_date")));
            String today    = LocalDate.now().format(DateTimeFormatter.ofPattern("MM/dd/yyyy"));
            String tomorrow = LocalDate.now().plusDays(1).format(DateTimeFormatter.ofPattern("MM/dd/yyyy"));
            js.executeScript("arguments[0].value=arguments[1]; arguments[0].dispatchEvent(new Event('change',{bubbles:true}));", driver.findElement(By.id("checkin_date")), today);
            js.executeScript("arguments[0].value=arguments[1]; arguments[0].dispatchEvent(new Event('change',{bubbles:true}));", driver.findElement(By.id("checkout_date")), tomorrow);
            Thread.sleep(500);
            js.executeScript("var n=document.getElementById('stay-nights'); n.value='1'; n.dispatchEvent(new Event('change',{bubbles:true}));");
            driver.findElement(By.cssSelector("button.btn.btn-warning.search")).click();
            Thread.sleep(3000);

            // Click first room_types button
            List<WebElement> roomTypeBtns = driver.findElements(By.xpath("//button[contains(@class,'room_types')]"));
            System.out.println("room_types buttons: " + roomTypeBtns.size());
            if (!roomTypeBtns.isEmpty()) {
                js.executeScript("arguments[0].scrollIntoView(true); arguments[0].click();", roomTypeBtns.get(0));
                Thread.sleep(2000);
            }

            // Dump the room-types-info-modal
            System.out.println("\n=== room-types-info-modal HTML (inner) ===");
            try {
                WebElement modal = driver.findElement(By.id("room-types-info-modal"));
                System.out.println("MODAL id=" + modal.getAttribute("id") + " class=" + modal.getAttribute("class"));
                // All buttons inside modal
                System.out.println("\n--- Buttons inside modal ---");
                for (WebElement btn : modal.findElements(By.tagName("button"))) {
                    System.out.println("  BUTTON class='" + btn.getAttribute("class") + "' data-dismiss='" + btn.getAttribute("data-dismiss") + "' data-bs-dismiss='" + btn.getAttribute("data-bs-dismiss") + "' text='" + btn.getText().trim().replace("\n"," ") + "'");
                }
                // All close/dismiss elements
                System.out.println("\n--- Close/dismiss elements inside modal ---");
                for (WebElement el : modal.findElements(By.xpath(".//*[@data-dismiss or @data-bs-dismiss or contains(@class,'close') or contains(@class,'btn-close')]"))) {
                    System.out.println("  EL tag=" + el.getTagName() + " class='" + el.getAttribute("class") + "' data-dismiss='" + el.getAttribute("data-dismiss") + "' data-bs-dismiss='" + el.getAttribute("data-bs-dismiss") + "'");
                }
                // All links inside modal
                System.out.println("\n--- Links/anchors inside modal ---");
                for (WebElement a : modal.findElements(By.tagName("a"))) {
                    System.out.println("  A class='" + a.getAttribute("class") + "' text='" + a.getText().trim() + "'");
                }
            } catch (Exception e) {
                System.out.println("room-types-info-modal not found: " + e.getMessage());
            }

            // Also dump all visible buttons on page
            System.out.println("\n=== ALL VISIBLE BUTTONS ON PAGE ===");
            for (WebElement btn : driver.findElements(By.tagName("button"))) {
                try {
                    if (btn.isDisplayed())
                        System.out.println("BUTTON class='" + btn.getAttribute("class") + "' data-bs-dismiss='" + btn.getAttribute("data-bs-dismiss") + "' text='" + btn.getText().trim().replace("\n"," ") + "'");
                } catch (Exception ignored) {}
            }

            // Now close the modal using data-bs-dismiss or close button
            System.out.println("\n=== TRYING TO CLOSE MODAL ===");
            boolean closed = false;

            // Try data-bs-dismiss inside modal
            try {
                WebElement closeBtn = driver.findElement(By.xpath("//*[@id='room-types-info-modal']//*[@data-bs-dismiss='modal']"));
                js.executeScript("arguments[0].click();", closeBtn);
                System.out.println("Closed via data-bs-dismiss inside modal");
                closed = true;
            } catch (Exception ignored) {}

            if (!closed) {
                try {
                    WebElement closeBtn = driver.findElement(By.xpath("//*[@id='room-types-info-modal']//button[contains(@class,'close') or contains(@class,'btn-close')]"));
                    js.executeScript("arguments[0].click();", closeBtn);
                    System.out.println("Closed via close button inside modal");
                    closed = true;
                } catch (Exception ignored) {}
            }

            if (!closed) {
                // Force close via JS Bootstrap
                js.executeScript("var m = document.getElementById('room-types-info-modal'); if(m){ var bsModal = bootstrap.Modal.getInstance(m); if(bsModal) bsModal.hide(); else m.classList.remove('show'); }");
                System.out.println("Closed via JS bootstrap hide");
            }

            Thread.sleep(1500);

            // Dump state after close
            System.out.println("\n=== AFTER MODAL CLOSE - VISIBLE INPUTS ===");
            for (WebElement el : driver.findElements(By.tagName("input"))) {
                try { if (el.isDisplayed()) System.out.println("INPUT id='" + el.getAttribute("id") + "' name='" + el.getAttribute("name") + "'"); } catch (Exception ignored) {}
            }
            for (WebElement el : driver.findElements(By.tagName("select"))) {
                try { if (el.isDisplayed()) System.out.println("SELECT id='" + el.getAttribute("id") + "' name='" + el.getAttribute("name") + "'"); } catch (Exception ignored) {}
            }
            System.out.println("\n=== AFTER MODAL CLOSE - VISIBLE BUTTONS ===");
            for (WebElement btn : driver.findElements(By.tagName("button"))) {
                try { if (btn.isDisplayed()) System.out.println("BUTTON class='" + btn.getAttribute("class") + "' text='" + btn.getText().trim().replace("\n"," ") + "'"); } catch (Exception ignored) {}
            }

            Thread.sleep(3000);
        } finally {
            driver.quit();
        }
    }
}
