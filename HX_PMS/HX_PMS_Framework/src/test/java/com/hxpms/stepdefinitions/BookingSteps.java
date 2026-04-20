package com.hxpms.stepdefinitions;

import com.hxpms.utils.BaseTest;
import io.cucumber.java.en.When;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Then;
import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;
import java.time.Duration;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class BookingSteps {
    WebDriver driver = BaseTest.driver;
    JavascriptExecutor js = (JavascriptExecutor) BaseTest.driver;
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("MM/dd/yyyy");

    @When("I navigate to bookings page")
    public void iNavigateToBookingsPage() {
        driver.get("https://demo.hotelxplore.com/frontdesk/dashboard");
        new WebDriverWait(driver, Duration.ofSeconds(30)).until(ExpectedConditions.urlContains("dashboard"));
        dismissAlert();
    }

    // ══════════════════════════════════════════════════════════════════════════
    // SCENARIO 1 — Walk-In: 1 room, 1 night, Jully Williams, Cash
    // ══════════════════════════════════════════════════════════════════════════
    @And("I create a new booking with guest details")
    public void iCreateANewBookingWithGuestDetails() throws InterruptedException {
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(40));
        openWalkInBooking(wait);
        fillSearchForm(wait, "Walk In", "1", 1);
        clickSearchAndWait(wait);
        handleRoomSelection(wait);
        fillGuestDetails(wait, "Jully", "Williams", "jully.williams@example.com", "5551234567", "123 Main Street", "10001", "1", "0");
        clickNextToPayment(wait);
        waitForPaymentSection(wait);
        selectCashPayment(wait);
        confirmBooking(wait);
        completePostPayment(wait);
        dismissAlert();
    }

    // ══════════════════════════════════════════════════════════════════════════
    // SCENARIO 2 — Reservation: 5 rooms, 6 nights, Kelvin Disuza, Cash
    // ══════════════════════════════════════════════════════════════════════════
    @And("I create a reservation booking for 5 rooms and 6 nights")
    public void iCreateAReservationBookingFor5RoomsAnd6Nights() throws InterruptedException {
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(40));
        openReservationBooking(wait);
        fillSearchForm(wait, "Reservation", "5", 6);
        clickSearchAndWait(wait);
        handleRoomSelection(wait);
        fillGuestDetails(wait, "Kelvin", "Disuza", "kelvin.disuza@example.com", "5559876543", "456 Ocean Avenue", "90210", "5", "0");
        clickNextToPayment(wait);
        waitForPaymentSection(wait);
        selectCashPayment(wait);
        confirmBooking(wait);
        completePostPayment(wait);
        dismissAlert();
    }

    // ── Open Walk In page ──────────────────────────────────────────────────────
    private void openWalkInBooking(WebDriverWait wait) {
        By walkIn = By.xpath("//a[contains(@class,'btn-theam1') and normalize-space()='Walk In']");
        wait.until(ExpectedConditions.elementToBeClickable(walkIn));
        jsClick(driver.findElement(walkIn));
        try { wait.until(ExpectedConditions.urlContains("book-room")); }
        catch (Exception e) {
            driver.get("https://demo.hotelxplore.com/frontdesk/book-room?book-type=Walk%20In");
            wait.until(ExpectedConditions.urlContains("book-room"));
        }
    }

    // ── Open Reservation page ──────────────────────────────────────────────────
    private void openReservationBooking(WebDriverWait wait) {
        By resBtn = By.xpath("//a[contains(@class,'btn-theam1') and normalize-space()='Reservation']");
        try {
            wait.until(ExpectedConditions.elementToBeClickable(resBtn));
            jsClick(driver.findElement(resBtn));
        } catch (Exception e) {
            driver.get("https://demo.hotelxplore.com/frontdesk/book-room?book-type=Reservation");
        }
        try { wait.until(ExpectedConditions.urlContains("book-room")); }
        catch (Exception e) {
            driver.get("https://demo.hotelxplore.com/frontdesk/book-room?book-type=Reservation");
            wait.until(ExpectedConditions.urlContains("book-room"));
        }
        System.out.println("[BookingSteps] On reservation page");
    }

    // ── Fill search form ───────────────────────────────────────────────────────
    // Key fix: page pre-fills dates on load — only set if empty, never clear
    private void fillSearchForm(WebDriverWait wait, String bookingType, String rooms, int nights)
            throws InterruptedException {
        LocalDate checkIn  = LocalDate.now();
        LocalDate checkOut = checkIn.plusDays(nights);

        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("checkin_date")));
        Thread.sleep(500);

        // Only set date if the field is empty — page pre-fills today/tomorrow on load
        setDateIfEmpty("checkin_date",  checkIn.format(DATE_FMT));
        setDateIfEmpty("checkout_date", checkOut.format(DATE_FMT));
        Thread.sleep(500);

        // Force stay-nights value
        js.executeScript(
            "var n=document.getElementById('stay-nights');" +
            "n.value='" + nights + "';" +
            "n.dispatchEvent(new Event('input',{bubbles:true}));" +
            "n.dispatchEvent(new Event('change',{bubbles:true}));");

        selectByValue(By.id("booking_type"), bookingType);
        selectByValue(By.id("rooms"), rooms);
        try { new Select(driver.findElement(By.id("applied-offer-code-dropdown"))).selectByIndex(0); }
        catch (Exception ignored) {}

        System.out.println("[BookingSteps] Search form filled: type=" + bookingType + " rooms=" + rooms + " nights=" + nights);
    }

    // ── Click Search and wait for results ─────────────────────────────────────
    private void clickSearchAndWait(WebDriverWait wait) throws InterruptedException {
        By searchBtn = By.cssSelector("button.btn.btn-warning.search");
        wait.until(ExpectedConditions.elementToBeClickable(searchBtn));
        driver.findElement(searchBtn).click();
        System.out.println("[BookingSteps] Search clicked");

        // Wait up to 30s for room_types buttons OR room-btn OR guest_type
        new WebDriverWait(driver, Duration.ofSeconds(30)).until(d -> {
            if (!d.findElements(By.xpath("//button[contains(@class,'room_types')]")).isEmpty()) return true;
            if (!d.findElements(By.xpath("//button[contains(@class,'room-btn')]")).isEmpty()) return true;
            List<WebElement> gt = d.findElements(By.id("guest_type"));
            return !gt.isEmpty() && gt.get(0).isDisplayed();
        });
        Thread.sleep(500);
        System.out.println("[BookingSteps] Search results ready");
    }

    // ── Handle room selection + close modal ────────────────────────────────────
    private void handleRoomSelection(WebDriverWait wait) throws InterruptedException {
        // Skip if guest form already visible
        List<WebElement> gt = driver.findElements(By.id("guest_type"));
        if (!gt.isEmpty() && gt.get(0).isDisplayed()) {
            System.out.println("[BookingSteps] Guest form already visible");
            return;
        }

        // Click first room_types button
        List<WebElement> roomTypeBtns = driver.findElements(By.xpath("//button[contains(@class,'room_types')]"));
        if (!roomTypeBtns.isEmpty()) {
            jsClick(roomTypeBtns.get(0));
            System.out.println("[BookingSteps] Clicked room_types button");
            Thread.sleep(1500);
        }

        // Wait for room-btn OR guest form
        new WebDriverWait(driver, Duration.ofSeconds(15)).until(d -> {
            if (!d.findElements(By.xpath("//button[contains(@class,'room-btn')]")).isEmpty()) return true;
            if (!d.findElements(By.xpath("//*[@id='room-types-info-modal']//button")).isEmpty()) return true;
            List<WebElement> g = d.findElements(By.id("guest_type"));
            return !g.isEmpty() && g.get(0).isDisplayed();
        });
        Thread.sleep(500);

        // If guest form appeared, done
        gt = driver.findElements(By.id("guest_type"));
        if (!gt.isEmpty() && gt.get(0).isDisplayed()) {
            System.out.println("[BookingSteps] Guest form visible after room_types click");
            return;
        }

        // Click first available room button
        for (By candidate : new By[]{
            By.xpath("//button[contains(@class,'room-btn') and contains(@class,'available')]"),
            By.xpath("//*[@id='room-types-info-modal']//button[contains(@class,'room-btn')]"),
            By.xpath("//button[contains(@class,'room-btn')]"),
            By.xpath("//*[@id='room-types-info-modal']//button")
        }) {
            List<WebElement> btns = driver.findElements(candidate);
            if (!btns.isEmpty()) {
                jsClick(btns.get(0));
                System.out.println("[BookingSteps] Clicked room button: " + candidate);
                Thread.sleep(1000);
                break;
            }
        }

        // Force close static modal
        js.executeScript(
            "var m=document.getElementById('room-types-info-modal');" +
            "if(m){try{bootstrap.Modal.getInstance(m).hide();}catch(e){m.classList.remove('show');m.style.display='none';m.setAttribute('aria-hidden','true');m.removeAttribute('aria-modal');}}" +
            "document.querySelectorAll('.modal-backdrop').forEach(b=>b.remove());" +
            "document.body.classList.remove('modal-open');" +
            "document.body.style.removeProperty('overflow');" +
            "document.body.style.removeProperty('padding-right');");
        Thread.sleep(800);
        System.out.println("[BookingSteps] Modal closed");

        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("guest_type")));
    }

    // ── Fill guest details ─────────────────────────────────────────────────────
    private void fillGuestDetails(WebDriverWait wait, String firstName, String lastName,
            String email, String mobile, String address, String zip,
            String adults, String kids) throws InterruptedException {
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("guest_type")));
        Thread.sleep(300);
        selectByValue(By.id("guest_type"), "New Guest");
        Thread.sleep(300);
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("first_name")));
        typeInto(By.id("first_name"),   firstName);
        typeInto(By.id("last_name"),    lastName);
        typeInto(By.id("email"),        email);
        typeInto(By.id("mobile"),       mobile);
        typeInto(By.id("address"),      address);
        typeInto(By.id("zip"),          zip);
        typeInto(By.id("guests_adult"), adults);
        typeInto(By.id("guests_kids"),  kids);
        System.out.println("[BookingSteps] Guest details filled: " + firstName + " " + lastName);
    }

    // ── Click Next ─────────────────────────────────────────────────────────────
    private void clickNextToPayment(WebDriverWait wait) throws InterruptedException {
        By nextXpath = By.xpath("//*[@id='booking-form-div']/div[1]/div[1]/div[1]/div[4]/div[1]/button[1]");
        wait.until(ExpectedConditions.elementToBeClickable(nextXpath));
        jsClick(driver.findElement(nextXpath));
        System.out.println("[BookingSteps] Clicked Next");
        Thread.sleep(1500);
    }

    // ── Wait for payment section ───────────────────────────────────────────────
    private void waitForPaymentSection(WebDriverWait wait) {
        try {
            wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("guarantee-method")));
            System.out.println("[BookingSteps] Payment modal visible");
        } catch (Exception ignored) {
            try { wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("payment_type"))); }
            catch (Exception ignored2) {}
        }
    }

    // ── Select Cash ────────────────────────────────────────────────────────────
    private void selectCashPayment(WebDriverWait wait) {
        try {
            wait.until(ExpectedConditions.elementToBeClickable(By.id("payment_type")));
            new Select(driver.findElement(By.id("payment_type"))).selectByValue("Cash");
            System.out.println("[BookingSteps] Cash selected");
        } catch (Exception e) {
            System.out.println("[BookingSteps] selectCashPayment failed: " + e.getMessage());
        }
    }

    // ── Confirm booking ────────────────────────────────────────────────────────
    private void confirmBooking(WebDriverWait wait) throws InterruptedException {
        By confirmXpath = By.xpath("//*[@id='guarantee-method']/div[1]/div[1]/div[3]/button[2]");
        try {
            wait.until(ExpectedConditions.elementToBeClickable(confirmXpath));
            jsClick(driver.findElement(confirmXpath));
            System.out.println("[BookingSteps] Booking confirmed");
            Thread.sleep(1000);
            return;
        } catch (Exception ignored) {}

        for (By sel : List.of(
            By.cssSelector("#guarantee-method button.btn-primary.btn-book.btn-confirm"),
            By.cssSelector("button.btn-primary.btn-lg.btn-book.btn-confirm"),
            By.cssSelector("button.btn-book.btn-confirm:not(.d-none)"),
            By.xpath("//button[contains(@class,'btn-book') and contains(@class,'btn-confirm') and not(contains(@class,'d-none'))]")
        )) {
            try {
                wait.until(ExpectedConditions.elementToBeClickable(sel));
                WebElement btn = driver.findElement(sel);
                if (btn.isDisplayed() && btn.isEnabled()) {
                    jsClick(btn);
                    System.out.println("[BookingSteps] Confirmed via fallback: " + sel);
                    Thread.sleep(1000);
                    return;
                }
            } catch (Exception ignored) {}
        }
        System.out.println("[BookingSteps] Confirm button not found");
    }

    // ── Post-payment modal ─────────────────────────────────────────────────────
    private void completePostPayment(WebDriverWait wait) {
        try {
            wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("#postPayment button.btn-primary")));
            driver.findElement(By.cssSelector("#postPayment button.btn-primary")).click();
            wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("#postPayment a")));
            driver.findElement(By.cssSelector("#postPayment a")).click();
        } catch (Exception ignored) {}
    }

    // ── Assertions ─────────────────────────────────────────────────────────────
    @Then("the booking should be created successfully")
    public void theBookingShouldBeCreatedSuccessfully() {
        new WebDriverWait(driver, Duration.ofSeconds(60)).until(ExpectedConditions.or(
            ExpectedConditions.urlContains("booking/"),
            ExpectedConditions.visibilityOfElementLocated(By.cssSelector("#postPayment"))
        ));
    }

    @Then("the reservation booking should be created successfully")
    public void theReservationBookingShouldBeCreatedSuccessfully() {
        new WebDriverWait(driver, Duration.ofSeconds(60)).until(ExpectedConditions.or(
            ExpectedConditions.urlContains("booking/"),
            ExpectedConditions.visibilityOfElementLocated(By.cssSelector("#postPayment"))
        ));
        System.out.println("[BookingSteps] Reservation booking created successfully");
    }

    // ── Helpers ────────────────────────────────────────────────────────────────

    // Only set date via JS if the field is currently empty — never overwrite pre-filled value
    private void setDateIfEmpty(String fieldId, String value) {
        try {
            String current = (String) js.executeScript(
                "return document.getElementById('" + fieldId + "').value;");
            if (current == null || current.trim().isEmpty()) {
                js.executeScript(
                    "var e=document.getElementById('" + fieldId + "');" +
                    "e.value=arguments[0];" +
                    "e.dispatchEvent(new Event('input',{bubbles:true}));" +
                    "e.dispatchEvent(new Event('change',{bubbles:true}));", value);
                System.out.println("[BookingSteps] Set " + fieldId + " = " + value);
            } else {
                System.out.println("[BookingSteps] " + fieldId + " already has value: " + current + " (keeping)");
            }
        } catch (Exception ignored) {}
    }

    private void jsClick(WebElement el) {
        js.executeScript("arguments[0].scrollIntoView({block:'center'}); arguments[0].click();", el);
    }

    private void typeInto(By locator, String value) {
        try {
            WebElement el = driver.findElement(locator);
            el.clear();
            el.sendKeys(value);
        } catch (Exception ignored) {}
    }

    private void selectByValue(By locator, String value) {
        try { new Select(driver.findElement(locator)).selectByVisibleText(value); }
        catch (Exception e) {
            try { new Select(driver.findElement(locator)).selectByValue(value); }
            catch (Exception ignored) {}
        }
    }

    private void dismissAlert() {
        try {
            new WebDriverWait(driver, Duration.ofSeconds(2)).until(ExpectedConditions.alertIsPresent());
            driver.switchTo().alert().accept();
        } catch (Exception ignored) {}
    }
}
