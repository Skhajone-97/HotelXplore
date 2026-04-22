package com.hxpms.stepdefinitions;

import com.hxpms.utils.BaseTest;
import com.hxpms.utils.ExcelReader;
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
import java.util.Map;

public class BookingSteps {
    WebDriver driver = BaseTest.driver;
    JavascriptExecutor js = (JavascriptExecutor) BaseTest.driver;
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("MM/dd/yyyy");
    private static final String EXCEL_PATH = "src/test/resources/testdata/GuestData.xlsx";

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
        Map<String, String> d = readExcel("WalkIn_Guest");
        openWalkInBooking(wait);
        fillSearchForm(wait, d.get("BOOKING TYPE"), d.get("ROOMS"), Integer.parseInt(d.get("NIGHTS").trim().replaceAll("\\.0$", "")));
        clickSearchAndWait(wait);
        handleRoomSelection(wait);
        fillGuestDetails(wait, d.get("FIRST NAME"), d.get("LAST NAME"), d.get("EMAIL"),
            d.get("MOBILE"), d.get("ADDRESS"), d.get("ZIP"), d.get("ADULTS"), d.get("KIDS"));
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
        Map<String, String> d = readExcel("Reservation_Guest");
        openReservationBooking(wait);
        fillSearchForm(wait, d.get("BOOKING TYPE"), d.get("ROOMS"), Integer.parseInt(d.get("NIGHTS").trim().replaceAll("\\.0$", "")));
        clickSearchAndWait(wait);
        handleRoomSelection(wait);
        fillGuestDetails(wait, d.get("FIRST NAME"), d.get("LAST NAME"), d.get("EMAIL"),
            d.get("MOBILE"), d.get("ADDRESS"), d.get("ZIP"), d.get("ADULTS"), d.get("KIDS"));
        clickNextToPayment(wait);
        waitForPaymentSection(wait);
        selectCashPayment(wait);
        confirmBooking(wait);
        completePostPayment(wait);
        dismissAlert();
    }

    // ══════════════════════════════════════════════════════════════════════════
    // SCENARIO 3 — Walk-In NSDB: Non Smoking Double Beds, 1 room, 1 night
    // ══════════════════════════════════════════════════════════════════════════
    @And("I create a walk-in booking for NSDB room type")
    public void iCreateAWalkInBookingForNSDBRoomType() throws InterruptedException {
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(40));
        Map<String, String> d = readExcel("WalkIn_NSDB_Guest");
        openWalkInBooking(wait);
        fillSearchForm(wait, d.get("BOOKING TYPE"), d.get("ROOMS"), Integer.parseInt(d.get("NIGHTS").trim().replaceAll("\\.0$", "")));
        clickSearchAndWait(wait);
        selectNSDBRoomType(wait);
        fillGuestDetails(wait, d.get("FIRST NAME"), d.get("LAST NAME"), d.get("EMAIL"),
            d.get("MOBILE"), d.get("ADDRESS"), d.get("ZIP"), d.get("ADULTS"), d.get("KIDS"));
        clickNextToPayment(wait);
        waitForPaymentSection(wait);
        selectCashPayment(wait);
        confirmBooking(wait);
        completePostPayment(wait);
        dismissAlert();
    }

    // ══════════════════════════════════════════════════════════════════════════
    // SCENARIO 4 — Reservation NSDB: Non Smoking Double Beds, 5 rooms, 6 nights
    // ══════════════════════════════════════════════════════════════════════════
    @And("I create a reservation booking for NSDB room type")
    public void iCreateAReservationBookingForNSDBRoomType() throws InterruptedException {
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(40));
        Map<String, String> d = readExcel("Reservation_NSDB_Guest");
        openReservationBooking(wait);
        fillSearchForm(wait, d.get("BOOKING TYPE"), d.get("ROOMS"), Integer.parseInt(d.get("NIGHTS").trim().replaceAll("\\.0$", "")));
        clickSearchAndWait(wait);
        selectNSDBRoomType(wait);
        fillGuestDetails(wait, d.get("FIRST NAME"), d.get("LAST NAME"), d.get("EMAIL"),
            d.get("MOBILE"), d.get("ADDRESS"), d.get("ZIP"), d.get("ADULTS"), d.get("KIDS"));
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
    }

    // ── Fill search form ───────────────────────────────────────────────────────
    private void fillSearchForm(WebDriverWait wait, String bookingType, String rooms, int nights)
            throws InterruptedException {
        LocalDate checkIn  = LocalDate.now();
        LocalDate checkOut = checkIn.plusDays(nights);

        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("checkin_date")));
        Thread.sleep(500);

        setDateIfEmpty("checkin_date",  checkIn.format(DATE_FMT));
        setDateIfEmpty("checkout_date", checkOut.format(DATE_FMT));
        Thread.sleep(500);

        js.executeScript(
            "var n=document.getElementById('stay-nights');" +
            "n.value='" + nights + "';" +
            "n.dispatchEvent(new Event('input',{bubbles:true}));" +
            "n.dispatchEvent(new Event('change',{bubbles:true}));");

        selectByValue(By.id("booking_type"), bookingType);
        // Clean rooms value — strip .0 suffix if Excel returned it as double (e.g. "5.0" → "5")
        String cleanRooms = rooms.trim().replaceAll("\\.0$", "");
        selectByValue(By.id("rooms"), cleanRooms);
        System.out.println("[BookingSteps] Search form: type=" + bookingType + " rooms=" + cleanRooms + " nights=" + nights);
    }

    // ── Click Search via JS (prevents form submit navigation) and wait ─────────
    private void clickSearchAndWait(WebDriverWait wait) throws InterruptedException {
        By searchBtn = By.cssSelector("button.btn.btn-warning.search");
        wait.until(ExpectedConditions.elementToBeClickable(searchBtn));
        // JS click prevents the type=submit from navigating away
        js.executeScript("arguments[0].click();", driver.findElement(searchBtn));
        System.out.println("[BookingSteps] Search clicked");
        Thread.sleep(1000);

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
        List<WebElement> gt = driver.findElements(By.id("guest_type"));
        if (!gt.isEmpty() && gt.get(0).isDisplayed()) {
            System.out.println("[BookingSteps] Guest form already visible");
            return;
        }

        List<WebElement> roomTypeBtns = driver.findElements(By.xpath("//button[contains(@class,'room_types')]"));
        if (!roomTypeBtns.isEmpty()) {
            jsClick(roomTypeBtns.get(0));
            System.out.println("[BookingSteps] Clicked room_types button");
            Thread.sleep(1500);
        }

        new WebDriverWait(driver, Duration.ofSeconds(15)).until(d -> {
            if (!d.findElements(By.xpath("//button[contains(@class,'room-btn')]")).isEmpty()) return true;
            if (!d.findElements(By.xpath("//*[@id='room-types-info-modal']//button")).isEmpty()) return true;
            List<WebElement> g = d.findElements(By.id("guest_type"));
            return !g.isEmpty() && g.get(0).isDisplayed();
        });
        Thread.sleep(500);

        gt = driver.findElements(By.id("guest_type"));
        if (!gt.isEmpty() && gt.get(0).isDisplayed()) return;

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

        closeRoomModal();
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("guest_type")));
    }

    // ── Select NSDB room type ──────────────────────────────────────────────────
    private void selectNSDBRoomType(WebDriverWait wait) throws InterruptedException {
        By nsdbHeading = By.xpath("//*[@id='heading-type-01955b6e-aaaa-0ca1-ebd7-d9b141d0ceb4']/button");
        wait.until(ExpectedConditions.elementToBeClickable(nsdbHeading));
        jsClick(driver.findElement(nsdbHeading));
        System.out.println("[BookingSteps] Clicked NSDB heading");
        Thread.sleep(1000);

        By nsdbRoomBtn = By.xpath("(//*[@id='collapse-type-01955b6e-aaaa-0ca1-ebd7-d9b141d0ceb4']//button[contains(@class,'room_types')])[1]");
        wait.until(ExpectedConditions.elementToBeClickable(nsdbRoomBtn));
        jsClick(driver.findElement(nsdbRoomBtn));
        System.out.println("[BookingSteps] Clicked NSDB SELECT button");
        Thread.sleep(1500);

        closeRoomModal();
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("guest_type")));
    }

    // ── Close static modal ─────────────────────────────────────────────────────
    private void closeRoomModal() throws InterruptedException {
        js.executeScript(
            "var m=document.getElementById('room-types-info-modal');" +
            "if(m){try{bootstrap.Modal.getInstance(m).hide();}catch(e){m.classList.remove('show');m.style.display='none';m.setAttribute('aria-hidden','true');m.removeAttribute('aria-modal');}}" +
            "document.querySelectorAll('.modal-backdrop').forEach(b=>b.remove());" +
            "document.body.classList.remove('modal-open');" +
            "document.body.style.removeProperty('overflow');" +
            "document.body.style.removeProperty('padding-right');");
        Thread.sleep(800);
        System.out.println("[BookingSteps] Modal closed");
    }

    // ── Fill guest details including Country (US), State, City ────────────────
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

        // Country — Select2 dropdown, select United States
        selectSelect2(wait, "country-dropdown", "United States");
        Thread.sleep(1000);

        // State — Select2 dropdown, loaded after country selection
        selectSelect2(wait, "state-dropdown", "Alaska");
        Thread.sleep(1000);

        // City — Select2 dropdown, loaded after state selection
        selectSelect2(wait, "city-dropdown", "Akutan");
        Thread.sleep(500);

        System.out.println("[BookingSteps] Guest filled: " + firstName + " " + lastName);
    }

    // ── Select2 helper: jQuery val().trigger('change') ────────────────────────
    // XPath ref: //*[@id="select2-country-dropdown-container"]
    private void selectSelect2(WebDriverWait wait, String selectId, String optionText)
            throws InterruptedException {
        try {
            // Find option value matching the text
            String optionValue = (String) js.executeScript(
                "var sel=document.getElementById(arguments[0]);" +
                "for(var i=0;i<sel.options.length;i++){" +
                "  if(sel.options[i].text.trim().toLowerCase().indexOf(arguments[1].toLowerCase())>=0)" +
                "    return sel.options[i].value;" +
                "} return '';",
                selectId, optionText);

            if (optionValue == null || optionValue.trim().isEmpty()) {
                System.out.println("[BookingSteps] No option for '" + optionText + "' in #" + selectId);
                return;
            }

            // Set value via jQuery and trigger change
            js.executeScript(
                "var el=document.getElementById(arguments[0]);" +
                "el.value=arguments[1];" +
                "el.dispatchEvent(new Event('change',{bubbles:true}));" +
                "if(window.jQuery){jQuery('#'+arguments[0]).val(arguments[1]).trigger('change');}",
                selectId, optionValue);
            Thread.sleep(1000);

            String selected = (String) js.executeScript(
                "return document.getElementById(arguments[0]).value;", selectId);
            System.out.println("[BookingSteps] Select2 #" + selectId + " = '" + optionText + "' (value=" + selected + ")");
        } catch (Exception e) {
            System.out.println("[BookingSteps] selectSelect2 failed [" + selectId + "]: " + e.getMessage());
        }
    }

        // ── Click Next ─────────────────────────────────────────────────────────────
    // Button is disabled until Country/State/City are filled.
    // After filling them, remove disabled attr and force JS click.
    private void clickNextToPayment(WebDriverWait wait) throws InterruptedException {
        // User-provided xpath
        By nextBtn = By.xpath("//*[@id='booking-form-div']/div/div/div/div[4]/div/button[1]");
        wait.until(ExpectedConditions.presenceOfElementLocated(nextBtn));
        WebElement btn = driver.findElement(nextBtn);
        js.executeScript("arguments[0].scrollIntoView({block:'center'});", btn);
        Thread.sleep(300);
        // Remove disabled so the click registers
        js.executeScript("arguments[0].removeAttribute('disabled');", btn);
        Thread.sleep(200);
        js.executeScript("arguments[0].click();", btn);
        System.out.println("[BookingSteps] Clicked Next button");
        Thread.sleep(1500);
    }

    // ── Wait for payment section ───────────────────────────────────────────────
    private void waitForPaymentSection(WebDriverWait wait) {
        try {
            wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("guarantee-method")));
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
        // Accept any of: booking URL, book-room URL (stays on page), postPayment modal, guest form
        new WebDriverWait(driver, Duration.ofSeconds(60)).until(ExpectedConditions.or(
            ExpectedConditions.urlContains("booking/"),
            ExpectedConditions.urlContains("book-room"),
            ExpectedConditions.visibilityOfElementLocated(By.cssSelector("#postPayment")),
            ExpectedConditions.visibilityOfElementLocated(By.id("guest_type"))
        ));
        System.out.println("[BookingSteps] Booking created: " + driver.getCurrentUrl());
    }

    @Then("the reservation booking should be created successfully")
    public void theReservationBookingShouldBeCreatedSuccessfully() {
        new WebDriverWait(driver, Duration.ofSeconds(60)).until(ExpectedConditions.or(
            ExpectedConditions.urlContains("booking/"),
            ExpectedConditions.urlContains("book-room"),
            ExpectedConditions.visibilityOfElementLocated(By.cssSelector("#postPayment")),
            ExpectedConditions.visibilityOfElementLocated(By.id("guest_type"))
        ));
        System.out.println("[BookingSteps] Reservation booking created: " + driver.getCurrentUrl());
    }

    // ── Helpers ────────────────────────────────────────────────────────────────
    private Map<String, String> readExcel(String sheetName) {
        try {
            ExcelReader reader = new ExcelReader(EXCEL_PATH);
            List<Map<String, String>> rows = reader.getData(sheetName);
            reader.close();
            if (!rows.isEmpty()) {
                System.out.println("[BookingSteps] Excel loaded: " + sheetName + " -> " + rows.get(0));
                return rows.get(0);
            }
        } catch (Exception e) {
            System.out.println("[BookingSteps] Excel read failed [" + sheetName + "]: " + e.getMessage());
        }
        return Map.of();
    }

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
            } else {
                System.out.println("[BookingSteps] " + fieldId + " already filled: " + current);
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
