package com.hxpms.stepdefinitions;

import com.hxpms.utils.BaseTest;
import io.cucumber.java.After;
import io.cucumber.java.Before;
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
    public void afterScenario() {
        if (driver != null) {
            driver.manage().deleteAllCookies();
            BaseTest.tearDown();
            driver = null;
        }
    }
}