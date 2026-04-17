package com.hxpms.stepdefinitions;

import com.hxpms.utils.BaseTest;
import com.hxpms.utils.CucumberReportGenerator;
import io.cucumber.java.After;
import io.cucumber.java.AfterAll;
import io.cucumber.java.Before;
import io.cucumber.java.Scenario;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;

public class Hooks {
    WebDriver driver;

    @Before
    public void beforeScenario() {
        BaseTest.initializeDriver();
        driver = BaseTest.driver;
        driver.manage().deleteAllCookies();
        driver.get("https://demo.hotelxplore.com/frontdesk");
    }

    @After
    public void afterScenario(Scenario scenario) {
        // Attach screenshot on failure
        if (scenario.isFailed() && driver != null) {
            try {
                byte[] screenshot = ((TakesScreenshot) driver).getScreenshotAs(OutputType.BYTES);
                scenario.attach(screenshot, "image/png", "Screenshot on Failure");
            } catch (Exception ignored) {}
        }

        if (driver != null) {
            driver.manage().deleteAllCookies();
            BaseTest.tearDown();
            driver = null;
        }
    }

    @AfterAll
    public static void afterAll() {
        // Auto-generate emailable HTML report after all scenarios finish
        try {
            CucumberReportGenerator.generateReport();
        } catch (Exception e) {
            System.out.println("[Hooks] Report generation failed: " + e.getMessage());
        }
    }
}
