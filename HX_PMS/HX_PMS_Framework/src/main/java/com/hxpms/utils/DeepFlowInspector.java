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

public class DeepFlowInspector {
    static WebDriver driver;
    static JavascriptExecutor js;
    static WebDriverWait wait;

    public static void main(String[] args) throws Exception {
        WebDriverManager.chromedriver().setup();
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--no-sandbox", "--disable-dev-shm-usage");
        driver = new ChromeDriver(options);
        js = (JavascriptExecutor) driver;
        wait = new WebDriverWait(driver, Duration.ofSeconds(20));

        try {
            login();
            goToWalkIn();
            fillAndSearch();

            step("AFTER SEARCH - all visible buttons");
            dumpButtons();

            // Click first room_types button
            List<WebElement> rtBtns = driver.findElements(By.xpath("//button[contains(@class,'room_types')]"));
            System.out.println("room_types count=" + rtBtns.size());
            if (!rtBtns.isEmpty()) {
                js.executeScript("arguments[0].scrollIntoView(true);arguments[0].click();", rtBtns.get(0));
                Thread.sleep(2000);
            }

            step("AFTER room_types CLICK - modals");
            dumpModals();
            step("AFTER room_types CLICK - buttons");
            dumpButtons();
            step("AFTER room_types CLICK - inputs/selects");
            dumpInputs();

            // Dump full HTML of room-types-info-modal
            step("room-types-info-modal FULL HTML");
            try {
                WebElement m = driver.findElement(By.id("room-types-info-modal"));
                String html = m.getAttribute("outerHTML");
                // print first 3000 chars
                System.out.println(html.substring(0, Math.min(3000, html.length())));
            } catch (Exception e) { System.out.println("modal not found: " + e.getMessage()); }

            // Try clicking inside the modal - any button
            step("CLICKING FIRST BUTTON INSIDE room-types-info-modal");
            try {
                List<WebElement> modalBtns = driver.findElements(
                    By.xpath("//*[@id='room-types-info-modal']//button"));
                System.out.println("buttons inside modal: " + modalBtns.size());
                for (WebElement b : modalBtns) {
                    System.out.println("  btn class='" + b.getAttribute("class") + "' text='" + b.getText().trim().replace("\n"," ") + "' displayed=" + b.isDisplayed());
                }
                if (!modalBtns.isEmpty()) {
                    js.executeScript("arguments[0].scrollIntoView(true);arguments[0].click();", modalBtns.get(0));
                    Thread.sleep(2000);
                }
            } catch (Exception e) { System.out.println("error: " + e.getMessage()); }

            step("AFTER MODAL BUTTON CLICK - modals");
            dumpModals();
            step("AFTER MODAL BUTTON CLICK - buttons");
            dumpButtons();

            // Try ESC
            step("SENDING ESC TO BODY");
            driver.findElement(By.tagName("body")).sendKeys(Keys.ESCAPE);
            Thread.sleep(1500);

            step("AFTER ESC - modals");
            dumpModals();
            step("AFTER ESC - visible inputs");
            dumpInputs();
            step("AFTER ESC - buttons");
            dumpButtons();

            Thread.sleep(3000);
        } finally {
            driver.quit();
        }
    }

    static void login() throws Exception {
        driver.get("https://demo.hotelxplore.com/frontdesk");
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.name("username")));
        driver.findElement(By.name("username")).sendKeys("webdev");
        driver.findElement(By.name("password")).sendKeys("1234");
        driver.findElement(By.cssSelector("button[type='submit']")).click();
        wait.until(ExpectedConditions.urlContains("dashboard"));
        System.out.println("=== Logged in ===");
    }

    static void goToWalkIn() throws Exception {
        By walkIn = By.xpath("//a[contains(@class,'btn-theam1') and normalize-space()='Walk In']");
        wait.until(ExpectedConditions.elementToBeClickable(walkIn));
        js.executeScript("arguments[0].click();", driver.findElement(walkIn));
        try { wait.until(ExpectedConditions.urlContains("book-room")); }
        catch (Exception e) { driver.get("https://demo.hotelxplore.com/frontdesk/book-room?book-type=Walk%20In"); }
        System.out.println("=== On book-room: " + driver.getCurrentUrl() + " ===");
    }

    static void fillAndSearch() throws Exception {
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("checkin_date")));
        String today    = LocalDate.now().format(DateTimeFormatter.ofPattern("MM/dd/yyyy"));
        String tomorrow = LocalDate.now().plusDays(1).format(DateTimeFormatter.ofPattern("MM/dd/yyyy"));
        js.executeScript("arguments[0].value=arguments[1];arguments[0].dispatchEvent(new Event('change',{bubbles:true}));", driver.findElement(By.id("checkin_date")), today);
        js.executeScript("arguments[0].value=arguments[1];arguments[0].dispatchEvent(new Event('change',{bubbles:true}));", driver.findElement(By.id("checkout_date")), tomorrow);
        Thread.sleep(600);
        js.executeScript("var n=document.getElementById('stay-nights');n.value='1';n.dispatchEvent(new Event('change',{bubbles:true}));");
        driver.findElement(By.cssSelector("button.btn.btn-warning.search")).click();
        Thread.sleep(3000);
        System.out.println("=== Search clicked ===");
    }

    static void step(String label) { System.out.println("\n======== " + label + " ========"); }

    static void dumpModals() {
        for (WebElement m : driver.findElements(By.cssSelector(".modal"))) {
            try { System.out.println("MODAL id='" + m.getAttribute("id") + "' class='" + m.getAttribute("class") + "' displayed=" + m.isDisplayed()); } catch (Exception ignored) {}
        }
    }

    static void dumpButtons() {
        for (WebElement b : driver.findElements(By.tagName("button"))) {
            try {
                if (b.isDisplayed())
                    System.out.println("BTN class='" + b.getAttribute("class") + "' text='" + b.getText().trim().replace("\n"," ") + "' data-bs-dismiss='" + b.getAttribute("data-bs-dismiss") + "'");
            } catch (Exception ignored) {}
        }
    }

    static void dumpInputs() {
        for (WebElement el : driver.findElements(By.tagName("input"))) {
            try { if (el.isDisplayed()) System.out.println("INPUT id='" + el.getAttribute("id") + "' name='" + el.getAttribute("name") + "'"); } catch (Exception ignored) {}
        }
        for (WebElement el : driver.findElements(By.tagName("select"))) {
            try { if (el.isDisplayed()) System.out.println("SELECT id='" + el.getAttribute("id") + "' name='" + el.getAttribute("name") + "'"); } catch (Exception ignored) {}
        }
    }
}
