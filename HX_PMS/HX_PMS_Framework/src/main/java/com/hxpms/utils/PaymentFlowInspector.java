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

public class PaymentFlowInspector {
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

            // Wait for room_types OR guest_type
            new WebDriverWait(driver, Duration.ofSeconds(30)).until(d ->
                !d.findElements(By.xpath("//button[contains(@class,'room_types')]")).isEmpty()
                || (!d.findElements(By.id("guest_type")).isEmpty()
                    && d.findElement(By.id("guest_type")).isDisplayed())
            );
            Thread.sleep(500);

            // Handle room selection
            List<WebElement> rtBtns = driver.findElements(By.xpath("//button[contains(@class,'room_types')]"));
            System.out.println("room_types count=" + rtBtns.size());
            if (!rtBtns.isEmpty()) {
                js.executeScript("arguments[0].scrollIntoView(true);arguments[0].click();", rtBtns.get(0));
                Thread.sleep(1500);

                // Wait for room-btn
                new WebDriverWait(driver, Duration.ofSeconds(15)).until(d ->
                    !d.findElements(By.xpath("//button[contains(@class,'room-btn')]")).isEmpty()
                    || (!d.findElements(By.id("guest_type")).isEmpty()
                        && d.findElement(By.id("guest_type")).isDisplayed())
                );
                Thread.sleep(500);

                List<WebElement> roomBtns = driver.findElements(
                    By.xpath("//button[contains(@class,'room-btn') and contains(@class,'available')]"));
                if (roomBtns.isEmpty())
                    roomBtns = driver.findElements(By.xpath("//button[contains(@class,'room-btn')]"));
                if (!roomBtns.isEmpty()) {
                    js.executeScript("arguments[0].scrollIntoView(true);arguments[0].click();", roomBtns.get(0));
                    System.out.println("Clicked room-btn: " + roomBtns.get(0).getAttribute("class"));
                    Thread.sleep(1000);
                }

                // Close modal
                js.executeScript(
                    "var m=document.getElementById('room-types-info-modal');" +
                    "if(m){try{bootstrap.Modal.getInstance(m).hide();}catch(e){m.classList.remove('show');m.style.display='none';}}" +
                    "document.querySelectorAll('.modal-backdrop').forEach(b=>b.remove());" +
                    "document.body.classList.remove('modal-open');" +
                    "document.body.style.removeProperty('overflow');" +
                    "document.body.style.removeProperty('padding-right');");
                Thread.sleep(800);
            }

            // Wait for guest form
            wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("guest_type")));

            // Fill guest form
            new Select(driver.findElement(By.id("guest_type"))).selectByVisibleText("New Guest");
            Thread.sleep(300);
            wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("first_name")));
            fill("first_name", "Jully");
            fill("last_name", "Williams");
            fill("email", "jully.williams@example.com");
            fill("mobile", "5551234567");
            fill("address", "123 Main Street");
            fill("zip", "10001");
            fill("guests_adult", "1");
            fill("guests_kids", "0");
            System.out.println("Guest form filled");

            // Dump booking-form-div structure
            System.out.println("\n=== booking-form-div BUTTONS ===");
            try {
                WebElement formDiv = driver.findElement(By.id("booking-form-div"));
                for (WebElement btn : formDiv.findElements(By.tagName("button"))) {
                    try {
                        System.out.println("BTN class='" + btn.getAttribute("class") +
                            "' id='" + btn.getAttribute("id") +
                            "' text='" + btn.getText().trim().replace("\n", " ") +
                            "' displayed=" + btn.isDisplayed() +
                            "' xpath=" + getXPath(btn));
                    } catch (Exception ignored) {}
                }
            } catch (Exception e) { System.out.println("booking-form-div not found: " + e.getMessage()); }

            // Dump ALL visible buttons
            System.out.println("\n=== ALL VISIBLE BUTTONS ===");
            for (WebElement btn : driver.findElements(By.tagName("button"))) {
                try {
                    if (btn.isDisplayed())
                        System.out.println("BTN class='" + btn.getAttribute("class") +
                            "' text='" + btn.getText().trim().replace("\n", " ") + "'");
                } catch (Exception ignored) {}
            }

            // Click the Next button using user's xpath
            System.out.println("\n=== CLICKING Next via user xpath ===");
            try {
                WebElement nextBtn = driver.findElement(
                    By.xpath("//*[@id='booking-form-div']/div/div/div/div[4]/div/button[1]"));
                System.out.println("Next btn text='" + nextBtn.getText() + "' class='" + nextBtn.getAttribute("class") + "'");
                js.executeScript("arguments[0].scrollIntoView(true);arguments[0].click();", nextBtn);
                Thread.sleep(2000);
            } catch (Exception e) { System.out.println("Next xpath failed: " + e.getMessage()); }

            // Dump payment section
            System.out.println("\n=== AFTER NEXT CLICK - VISIBLE INPUTS ===");
            for (WebElement el : driver.findElements(By.tagName("input"))) {
                try { if (el.isDisplayed()) System.out.println("INPUT id='" + el.getAttribute("id") + "' name='" + el.getAttribute("name") + "'"); }
                catch (Exception ignored) {}
            }
            for (WebElement el : driver.findElements(By.tagName("select"))) {
                try { if (el.isDisplayed()) System.out.println("SELECT id='" + el.getAttribute("id") + "' name='" + el.getAttribute("name") + "'"); }
                catch (Exception ignored) {}
            }
            System.out.println("\n=== AFTER NEXT CLICK - VISIBLE BUTTONS ===");
            for (WebElement btn : driver.findElements(By.tagName("button"))) {
                try {
                    if (btn.isDisplayed())
                        System.out.println("BTN class='" + btn.getAttribute("class") +
                            "' text='" + btn.getText().trim().replace("\n", " ") + "'");
                } catch (Exception ignored) {}
            }

            Thread.sleep(3000);
        } finally {
            driver.quit();
        }
    }

    static String getXPath(WebElement el) {
        try {
            return (String) ((JavascriptExecutor) driver).executeScript(
                "function getXPath(el){" +
                "  if(el.id) return '//*[@id=\"'+el.id+'\"]';" +
                "  if(el===document.body) return '/html/body';" +
                "  var ix=0; var siblings=el.parentNode.childNodes;" +
                "  for(var i=0;i<siblings.length;i++){" +
                "    var sib=siblings[i];" +
                "    if(sib===el) return getXPath(el.parentNode)+'/'+el.tagName.toLowerCase()+'['+(ix+1)+']';" +
                "    if(sib.nodeType===1&&sib.tagName===el.tagName) ix++;" +
                "  }" +
                "} return getXPath(arguments[0]);", el);
        } catch (Exception e) { return "unknown"; }
    }

    static void login() throws Exception {
        driver.get("https://demo.hotelxplore.com/frontdesk");
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.name("username")));
        driver.findElement(By.name("username")).sendKeys("webdev");
        driver.findElement(By.name("password")).sendKeys("1234");
        driver.findElement(By.cssSelector("button[type='submit']")).click();
        wait.until(ExpectedConditions.urlContains("dashboard"));
        System.out.println("Logged in");
    }

    static void goToWalkIn() throws Exception {
        By walkIn = By.xpath("//a[contains(@class,'btn-theam1') and normalize-space()='Walk In']");
        wait.until(ExpectedConditions.elementToBeClickable(walkIn));
        js.executeScript("arguments[0].click();", driver.findElement(walkIn));
        try { wait.until(ExpectedConditions.urlContains("book-room")); }
        catch (Exception e) { driver.get("https://demo.hotelxplore.com/frontdesk/book-room?book-type=Walk%20In"); }
    }

    static void fillAndSearch() throws Exception {
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("checkin_date")));
        String today    = LocalDate.now().format(DateTimeFormatter.ofPattern("MM/dd/yyyy"));
        String tomorrow = LocalDate.now().plusDays(1).format(DateTimeFormatter.ofPattern("MM/dd/yyyy"));
        js.executeScript("var e=document.getElementById('checkin_date');e.value=arguments[0];e.dispatchEvent(new Event('input',{bubbles:true}));e.dispatchEvent(new Event('change',{bubbles:true}));", today);
        Thread.sleep(400);
        js.executeScript("var e=document.getElementById('checkout_date');e.value=arguments[0];e.dispatchEvent(new Event('input',{bubbles:true}));e.dispatchEvent(new Event('change',{bubbles:true}));", tomorrow);
        Thread.sleep(600);
        js.executeScript("var n=document.getElementById('stay-nights');n.value='1';n.dispatchEvent(new Event('input',{bubbles:true}));n.dispatchEvent(new Event('change',{bubbles:true}));");
        new Select(driver.findElement(By.id("booking_type"))).selectByVisibleText("Walk In");
        new Select(driver.findElement(By.id("rooms"))).selectByValue("1");
        driver.findElement(By.cssSelector("button.btn.btn-warning.search")).click();
        System.out.println("Search clicked");
    }

    static void fill(String id, String value) {
        try {
            WebElement el = driver.findElement(By.id(id));
            el.clear();
            el.sendKeys(value);
        } catch (Exception ignored) {}
    }
}
