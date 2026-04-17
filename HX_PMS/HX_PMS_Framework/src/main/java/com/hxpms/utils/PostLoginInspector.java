package com.hxpms.utils;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

public class PostLoginInspector {
    public static void main(String[] args) {
        WebDriverManager.chromedriver().setup();
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-dev-shm-usage");
        WebDriver driver = new ChromeDriver(options);
        try {
            driver.manage().window().maximize();
            driver.get("https://demo.hotelxplore.com/frontdesk");
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(20));
            wait.until(ExpectedConditions.visibilityOfElementLocated(By.name("username")));
            driver.findElement(By.name("username")).sendKeys("webdev");
            driver.findElement(By.name("password")).sendKeys("1234");
            driver.findElement(By.cssSelector("button[type='submit']")).click();
            wait.until(ExpectedConditions.urlContains("dashboard"));
            System.out.println("Logged in page title: " + driver.getTitle());
            System.out.println("Logged in URL: " + driver.getCurrentUrl());
            System.out.println("=== Link text containing booking/arrival ===");
            driver.findElements(By.tagName("a")).stream()
                .filter(el -> {
                    String text = el.getText();
                    return text != null && (text.toLowerCase().contains("book") || text.toLowerCase().contains("arriv") || text.toLowerCase().contains("dashboard"));
                })
                .forEach(el -> System.out.println("LINK: [" + el.getText() + "] -> " + el.getAttribute("href")));
            System.out.println("=== All hrefs containing '/booking' ===");
            driver.findElements(By.cssSelector("a[href*='booking']")).forEach(el ->
                System.out.println("LINK: [" + el.getText() + "] -> " + el.getAttribute("href")));
            System.out.println("=== Stayover Page Inspection ===");
            driver.get("https://demo.hotelxplore.com/frontdesk/booking/stayover");
            WebDriverWait wait2 = new WebDriverWait(driver, Duration.ofSeconds(20));
            wait2.until(ExpectedConditions.urlContains("booking/stayover"));
            System.out.println("Stayover title: " + driver.getTitle());
            System.out.println("Stayover URL: " + driver.getCurrentUrl());
            System.out.println("Stayover page snippet:");
            String staySrc = driver.getPageSource();
            String lower = staySrc.toLowerCase();
            int idx = lower.indexOf("active bookings");
            if (idx >= 0) {
                System.out.println(staySrc.substring(Math.max(0, idx - 200), Math.min(staySrc.length(), idx + 400)));
            } else {
                System.out.println("No 'Active Bookings' text found on stayover page.");
            }
            System.out.println("=== Stayover links ===");
            driver.findElements(By.tagName("a")).forEach(el -> {
                String text = el.getText();
                String href = el.getAttribute("href");
                if (text != null && !text.isBlank() && href != null && href.contains("booking")) {
                    System.out.println("LINK: [" + text + "] -> " + href);
                }
            });
            System.out.println("=== New Booking Page Inspection ===");
            driver.get("https://demo.hotelxplore.com/frontdesk/booking/todays/new-booking");
            WebDriverWait wait3 = new WebDriverWait(driver, Duration.ofSeconds(20));
            wait3.until(ExpectedConditions.urlContains("new-booking"));
            System.out.println("New Booking title: " + driver.getTitle());
            System.out.println("New Booking URL: " + driver.getCurrentUrl());
            System.out.println("New Booking input names and placeholders:");
            driver.findElements(By.tagName("input")).forEach(el -> {
                String name = el.getAttribute("name");
                String placeholder = el.getAttribute("placeholder");
                if (name != null && !name.isBlank()) {
                    System.out.println("INPUT: name='" + name + "' placeholder='" + placeholder + "'");
                }
            });
            System.out.println("New Booking select names:");
            driver.findElements(By.tagName("select")).forEach(el -> {
                String name = el.getAttribute("name");
                if (name != null && !name.isBlank()) {
                    System.out.println("SELECT: name='" + name + "'");
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            driver.quit();
        }
    }
}