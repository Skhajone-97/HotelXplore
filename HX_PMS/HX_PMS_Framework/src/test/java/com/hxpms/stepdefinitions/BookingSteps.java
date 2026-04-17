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

    @And("I create a new booking with guest details")
    public void iCreateANewBookingWithGuestDetails() throws InterruptedException {
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(40));
        openWalkInBooking(wait);
        fillSearchForm(wait);
        clickSearchAndWait(wait);
        handleRoomSelection(wait);
        fillNewGuestDetails(wait);
        clickNextToPayment(wait);
        waitForPaymentSection(wait);
        selectCashPayment(wait);
        confirmBooking(wait);
        completePostPayment(wait);
        dismissAlert();
    }

    // ── Step 1 ─────────────────────────────────────────────────────────────────
    private void openWalkInBooking(WebDriverWait wait) {
        By walkIn = By.xpath("//a[contains(@class,'btn-theam1') and normalize-space()='Walk In']");
        wait.until(ExpectedConditions.elementToBeClickable(walkIn));
        jsClick(driver.findElement(walkIn));
        try {
            wait.until(ExpectedConditions.urlContains("book-room"));
        } catch (Exception e) {
            driver.get("https://demo.hotelxplore.com/frontdesk/book-room?book-type=Walk%20In");
            wait.until(ExpectedConditions.urlContains("book-room"));
        }
    }

    // ── Step 2 ─────────────────────────────────────────────────────────────────
    private void fillSearchForm(WebDriverWait wait) throws InterruptedException {
        LocalDate today    = LocalDate.now();
        LocalDate tomorrow = today.plusDays(1);

        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("checkin_date")));

        js.executeScript(
            "var e=document.getElementById('checkin_date');" +
            "e.value=arguments[0];" +
            "e.dispatchEvent(new Event('input',{bubbles:true}));" +
            "e.dispatchEvent(new Event('change',{bubbles:true}));",
            today.format(DATE_FMT));
        Thread.sleep(400);

        js.executeScript(
            "var e=document.getElementById('checkout_date');" +
            "e.value=arguments[0];" +
            "e.dispatchEvent(new Event('input',{bubbles:true}));" +
            "e.dispatchEvent(new Event('change',{bubbles:true}));",
            tomorrow.format(DATE_FMT));
        Thread.sleep(600);

        js.executeScript(
            "var n=document.getElementById('stay-nights');" +
            "n.value='1';" +
            "n.dispatchEvent(new Event('input',{bubbles:true}));" +
            "n.dispatchEvent(new Event('change',{bubbles:true}));");

        selectByValue(By.id("booking_type"), "Walk In");
        selectByValue(By.id("rooms"), "1");
        try { new Select(driver.findElement(By.id("applied-offer-code-dropdown"))).selectByIndex(0); }
        catch (Exception ignored) {}
    }

    // ── Step 3: click Search, wait for room buttons OR guest form ──────────────
    private void clickSearchAndWait(WebDriverWait wait) throws InterruptedException {
        By searchBtn = By.cssSelector("button.btn.btn-warning.search");
        wait.until(ExpectedConditions.elementToBeClickable(searchBtn));
        driver.findElement(searchBtn).click();
        System.out.println("[BookingSteps] Search clicked");

        new WebDriverWait(driver, Duration.ofSeconds(30)).until(d ->
            !d.findElements(By.xpath("//button[contains(@class,'room_types')]")).isEmpty()
            || !d.findElements(By.xpath("//button[contains(@class,'room-btn')]")).isEmpty()
            || (!d.findElements(By.id("guest_type")).isEmpty()
                && d.findElement(By.id("guest_type")).isDisplayed())
        );
        Thread.sleep(500);
        System.out.println("[BookingSteps] Search results ready");
    }

    // ── Step 4: handle room selection + close modal ────────────────────────────
    private void handleRoomSelection(WebDriverWait wait) throws InterruptedException {
        // If guest form already visible, skip room selection entirely
        List<WebElement> guestTypeEls = driver.findElements(By.id("guest_type"));
        if (!guestTypeEls.isEmpty() && guestTypeEls.get(0).isDisplayed()) {
            System.out.println("[BookingSteps] Guest form already visible, skipping room selection");
            return;
        }

        // Click first room_types button
        List<WebElement> roomTypeBtns = driver.findElements(By.xpath("//button[contains(@class,'room_types')]"));
        if (!roomTypeBtns.isEmpty()) {
            jsClick(roomTypeBtns.get(0));
            System.out.println("[BookingSteps] Clicked room_types button");
            Thread.sleep(1500);
        }

        // Wait for modal body to populate OR room-btn to appear OR guest form
        new WebDriverWait(driver, Duration.ofSeconds(15)).until(d ->
            !d.findElements(By.xpath("//*[@id='room-types-info-modal']//button")).isEmpty()
            || !d.findElements(By.xpath("//button[contains(@class,'room-btn')]")).isEmpty()
            || (!d.findElements(By.id("guest_type")).isEmpty()
                && d.findElement(By.id("guest_type")).isDisplayed())
        );
        Thread.sleep(500);

        // If guest form appeared after room_types click, done
        guestTypeEls = driver.findElements(By.id("guest_type"));
        if (!guestTypeEls.isEmpty() && guestTypeEls.get(0).isDisplayed()) {
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
                System.out.println("[BookingSteps] Clicked room button via: " + candidate);
                Thread.sleep(1000);
                break;
            }
        }

        // Force close modal — data-bs-backdrop=static so ESC won't work
        js.executeScript(
            "var m=document.getElementById('room-types-info-modal');" +
            "if(m){" +
            "  try{bootstrap.Modal.getInstance(m).hide();}" +
            "  catch(e){" +
            "    m.classList.remove('show');" +
            "    m.style.display='none';" +
            "    m.setAttribute('aria-hidden','true');" +
            "    m.removeAttribute('aria-modal');" +
            "  }" +
            "}" +
            "document.querySelectorAll('.modal-backdrop').forEach(b=>b.remove());" +
            "document.body.classList.remove('modal-open');" +
            "document.body.style.removeProperty('overflow');" +
            "document.body.style.removeProperty('padding-right');"
        );
        Thread.sleep(800);
        System.out.println("[BookingSteps] Modal closed");

        // Wait for guest form to be ready
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("guest_type")));
    }

    // ── Step 5: fill new guest details ─────────────────────────────────────────
    private void fillNewGuestDetails(WebDriverWait wait) throws InterruptedException {
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("guest_type")));
        Thread.sleep(300);

        selectByValue(By.id("guest_type"), "New Guest");
        Thread.sleep(300);

        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("first_name")));

        typeInto(By.id("first_name"),   "Jully");
        typeInto(By.id("last_name"),    "Williams");
        typeInto(By.id("email"),        "jully.williams@example.com");
        typeInto(By.id("mobile"),       "5551234567");
        typeInto(By.id("address"),      "123 Main Street");
        typeInto(By.id("zip"),          "10001");
        typeInto(By.id("guests_adult"), "1");
        typeInto(By.id("guests_kids"),  "0");
        System.out.println("[BookingSteps] Guest details filled");
    }

    // ── Step 6: click Next button ──────────────────────────────────────────────
    private void clickNextToPayment(WebDriverWait wait) throws InterruptedException {
        // Exact xpath from live inspection
        By nextXpath = By.xpath("//*[@id='booking-form-div']/div[1]/div[1]/div[1]/div[4]/div[1]/button[1]");
        wait.until(ExpectedConditions.elementToBeClickable(nextXpath));
        jsClick(driver.findElement(nextXpath));
        System.out.println("[BookingSteps] Clicked Next button");
        Thread.sleep(1500);
    }

    // ── Step 7: wait for guarantee-method modal (contains payment form) ─────────
    private void waitForPaymentSection(WebDriverWait wait) {
        try {
            wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.xpath("//*[@id='guarantee-method']") ));
            System.out.println("[BookingSteps] Payment modal visible");
        } catch (Exception ignored) {
            // fallback: wait for payment_type select
            try { wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("payment_type"))); }
            catch (Exception ignored2) {}
        }
    }

    // ── Step 8: select Cash from payment_type inside guarantee-method modal ─────
    private void selectCashPayment(WebDriverWait wait) {
        try {
            wait.until(ExpectedConditions.elementToBeClickable(By.id("payment_type")));
            new Select(driver.findElement(By.id("payment_type"))).selectByValue("Cash");
            System.out.println("[BookingSteps] Cash selected");
        } catch (Exception e) {
            System.out.println("[BookingSteps] selectCashPayment failed: " + e.getMessage());
        }
    }

    // ── Step 9: confirm booking via guarantee-method modal confirm button ────────
    private void confirmBooking(WebDriverWait wait) throws InterruptedException {
        // Exact xpath from live inspection: guarantee-method modal → div[3] → button[2]
        By confirmXpath = By.xpath("//*[@id='guarantee-method']/div[1]/div[1]/div[3]/button[2]");
        try {
            wait.until(ExpectedConditions.elementToBeClickable(confirmXpath));
            jsClick(driver.findElement(confirmXpath));
            System.out.println("[BookingSteps] Booking confirmed via guarantee-method");
            Thread.sleep(1000);
            return;
        } catch (Exception ignored) {}

        // Fallback selectors
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

    // ── Step 10: post-payment modal ─────────────────────────────────────────────
    private void completePostPayment(WebDriverWait wait) {
        try {
            wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("#postPayment button.btn-primary")));
            driver.findElement(By.cssSelector("#postPayment button.btn-primary")).click();
            wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("#postPayment a")));
            driver.findElement(By.cssSelector("#postPayment a")).click();
        } catch (Exception ignored) {}
    }

    // ── Assertion ──────────────────────────────────────────────────────────────
    @Then("the booking should be created successfully")
    public void theBookingShouldBeCreatedSuccessfully() {
        new WebDriverWait(driver, Duration.ofSeconds(60)).until(ExpectedConditions.or(
            ExpectedConditions.urlContains("booking/"),
            ExpectedConditions.visibilityOfElementLocated(By.cssSelector("#postPayment"))
        ));
    }

    // ── Helpers ────────────────────────────────────────────────────────────────
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
