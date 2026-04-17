package com.hxpms.runners;

import io.cucumber.testng.AbstractTestNGCucumberTests;
import io.cucumber.testng.CucumberOptions;

@CucumberOptions(
    features = "src/test/resources/features/Booking.feature",
    glue = {"com.hxpms.stepdefinitions"},
    plugin = {"pretty", "html:target/cucumber-reports.html", "json:target/cucumber-reports.json"},
    monochrome = true
)
public class TestNGRunner extends AbstractTestNGCucumberTests {
}
