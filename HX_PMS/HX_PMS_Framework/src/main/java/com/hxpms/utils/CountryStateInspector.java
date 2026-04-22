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

public class CountryStateInspector {
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

            // Click search (type=submit navigates to room/search)
            System.out.println("=== CLICKING SEARCH ===");
            System.out.println("URL before: " + driver.getCurrentUrl());
            driver.findElement(By.cssSelector("button.btn.btn-warning.search")).click();
            Thread.sleep(3000);
            System.out.println("URL after: " + driver.getCurrentUrl());

            // Wait for room_types
            new WebDriverWait(driver, Duration.ofSeconds(30)).until(
                ExpectedConditions.presenceOfElementLocated(By.xpath("//button[contains(@class,'room_types')]")));
            Thread.sleep(500);

            // Click first room_types button
            List<WebElement> rtBtns = driver.findElements(By.xpath("//button[contains(@class,'room_types')]"));
            System.out.println("room_types count=" + rtBtns.size());
            js.executeScript("arguments[0].scrollIntoView(true);arguments[0].click();", rtBtns.get(0));
            Thread.sleep(1500);

            // Click first available room-btn
            List<WebElement> roomBtns = driver.findElements(By.xpath("//button[contains(@class,'room-btn') and contains(@class,'available')]"));
            if (!roomBtns.isEmpty()) {
                js.executeScript("arguments[0].scrollIntoView(true);arguments[0].click();", roomBtns.get(0));
                Thread.sleep(1000);
            }

            // Close modal
            js.executeScript(
                "var m=document.getElementById('room-types-info-modal');" +
                "if(m){try{bootstrap.Modal.getInstance(m).hide();}catch(e){m.classList.remove('show');m.style.display='none';}}" +
                "document.querySelectorAll('.modal-backdrop').forEach(b=>b.remove());" +
                "document.body.classList.remove('modal-open');");
            Thread.sleep(800);

            // Wait for guest form
            wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("guest_type")));
            System.out.println("\n=== GUEST FORM VISIBLE ===");

            // Dump ALL select2 containers
            System.out.println("\n=== SELECT2 CONTAINERS ===");
            for (WebElement el : driver.findElements(By.xpath("//*[contains(@id,'select2') and contains(@id,'container')]"))) {
                System.out.println("id='" + el.getAttribute("id") + "' text='" + el.getText().trim() + "'");
            }

            // Dump country dropdown structure
            System.out.println("\n=== COUNTRY DROPDOWN STRUCTURE ===");
            try {
                WebElement countryContainer = driver.findElement(By.id("select2-country-dropdown-container"));
                System.out.println("container text='" + countryContainer.getText() + "'");
                WebElement countrySpan = driver.findElement(By.xpath("//*[@id='select2-country-dropdown-container']/.."));
                System.out.println("parent class='" + countrySpan.getAttribute("class") + "'");
                // Find the actual select element
                WebElement countrySelect = driver.findElement(By.id("country-dropdown"));
                System.out.println("select id='" + countrySelect.getAttribute("id") + "' name='" + countrySelect.getAttribute("name") + "'");
                Select sel = new Select(countrySelect);
                List<WebElement> opts = sel.getOptions();
                System.out.println("Total options: " + opts.size());
                // Print first 5 options with text
                for (int i = 0; i < Math.min(5, opts.size()); i++) {
                    System.out.println("  option[" + i + "] value='" + opts.get(i).getAttribute("value") + "' text='" + opts.get(i).getText() + "'");
                }
                // Find USA option
                for (WebElement opt : opts) {
                    String t = opt.getText().toLowerCase();
                    if (t.contains("united states") || t.contains("usa") || t.contains("u.s.")) {
                        System.out.println("  USA option: value='" + opt.getAttribute("value") + "' text='" + opt.getText() + "'");
                        break;
                    }
                }
            } catch (Exception e) { System.out.println("Country error: " + e.getMessage()); }

            // Dump state dropdown
            System.out.println("\n=== STATE DROPDOWN STRUCTURE ===");
            try {
                WebElement stateSelect = driver.findElement(By.id("state-dropdown"));
                System.out.println("state select id='" + stateSelect.getAttribute("id") + "'");
                System.out.println("state select2 container id=select2-state-dropdown-container exists: " +
                    !driver.findElements(By.id("select2-state-dropdown-container")).isEmpty());
            } catch (Exception e) { System.out.println("State error: " + e.getMessage()); }

            // Dump city dropdown
            System.out.println("\n=== CITY DROPDOWN STRUCTURE ===");
            try {
                WebElement citySelect = driver.findElement(By.id("city-dropdown"));
                System.out.println("city select id='" + citySelect.getAttribute("id") + "'");
                System.out.println("city select2 container id=select2-city-dropdown-container exists: " +
                    !driver.findElements(By.id("select2-city-dropdown-container")).isEmpty());
            } catch (Exception e) { System.out.println("City error: " + e.getMessage()); }

            // Try selecting country via Select2 — click the container then type
            System.out.println("\n=== TRYING SELECT2 COUNTRY SELECTION ===");
            try {
                // Click the Select2 container to open dropdown
                WebElement countryContainer = driver.findElement(
                    By.xpath("//*[@id='select2-country-dropdown-container']/.."));
                js.executeScript("arguments[0].click();", countryContainer);
                Thread.sleep(800);

                // Find the search input that appears
                List<WebElement> searchInputs = driver.findElements(
                    By.xpath("//input[@class='select2-search__field']"));
                System.out.println("Select2 search inputs found: " + searchInputs.size());
                if (!searchInputs.isEmpty()) {
                    searchInputs.get(0).sendKeys("United");
                    Thread.sleep(800);
                    // Find results
                    List<WebElement> results = driver.findElements(
                        By.xpath("//li[contains(@class,'select2-results__option')]"));
                    System.out.println("Results count: " + results.size());
                    for (int i = 0; i < Math.min(5, results.size()); i++) {
                        System.out.println("  result[" + i + "] text='" + results.get(i).getText() + "'");
                    }
                    // Click first result
                    if (!results.isEmpty()) {
                        results.get(0).click();
                        Thread.sleep(500);
                        System.out.println("Clicked first result");
                    }
                }

                // Check state options after country selection
                Thread.sleep(1000);
                System.out.println("\n=== STATE OPTIONS AFTER COUNTRY SELECT ===");
                WebElement stateSelect = driver.findElement(By.id("state-dropdown"));
                Select stateSel = new Select(stateSelect);
                System.out.println("State options count: " + stateSel.getOptions().size());
                for (int i = 0; i < Math.min(5, stateSel.getOptions().size()); i++) {
                    System.out.println("  state[" + i + "] value='" + stateSel.getOptions().get(i).getAttribute("value") + "' text='" + stateSel.getOptions().get(i).getText() + "'");
                }

            } catch (Exception e) { System.out.println("Select2 error: " + e.getMessage()); }

            Thread.sleep(3000);
        } finally {
            driver.quit();
        }
    }
}
