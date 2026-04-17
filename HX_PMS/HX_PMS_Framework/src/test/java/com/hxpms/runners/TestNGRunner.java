package com.hxpms.runners;

import io.cucumber.testng.AbstractTestNGCucumberTests;
import io.cucumber.testng.CucumberOptions;

@CucumberOptions(
    features  = "src/test/resources/features",
    glue      = {"com.hxpms.stepdefinitions"},
    plugin    = {
        "pretty",
        "html:target/cucumber-reports/cucumber.html",
        "json:target/cucumber-reports/cucumber.json",
        "junit:target/cucumber-reports/cucumber.xml",
        "rerun:target/cucumber-reports/rerun.txt"
    },
    monochrome = true
)
public class TestNGRunner extends AbstractTestNGCucumberTests {
}
