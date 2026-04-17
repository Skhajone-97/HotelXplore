package com.hxpms.utils;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

public class PageInspector {
    public static void main(String[] args) {
        WebDriverManager.chromedriver().setup();
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless");
        WebDriver driver = new ChromeDriver(options);
        driver.get("https://demo.hotelxplore.com/frontdesk");
        System.out.println("Page Title: " + driver.getTitle());
        System.out.println("Page Source:");
        System.out.println(driver.getPageSource());
        driver.quit();
    }
}