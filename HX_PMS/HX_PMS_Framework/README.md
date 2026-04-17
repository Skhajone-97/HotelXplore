# HX PMS Automation Framework

This is a Selenium-based BDD framework using Cucumber for automating test cases for Hotel Xplore PMS.

## Prerequisites
- Java 11+
- Maven 3.6+

## Setup
1. Clone or download the project.
2. Run `mvn clean install` to download dependencies.

## Running Tests
- Run `mvn test` to execute all Cucumber tests.
- Reports will be generated in `target/cucumber-reports.html`.

## Framework Structure
- `src/test/resources/features`: Cucumber feature files
- `src/test/java/com/hxpms/stepdefinitions`: Step definition classes
- `src/test/java/com/hxpms/runners`: Test runner
- `src/main/java/com/hxpms/utils`: Utility classes for WebDriver and Excel reading

## Data-Driven Testing
The framework supports data-driven testing using Excel files. Place your test data in `src/test/resources/testdata.xlsx` and use the ExcelReader utility.

## Application Details
- URL: https://demo.hotelxplore.com/frontdesk
- Username: webdev
- Password: 1234