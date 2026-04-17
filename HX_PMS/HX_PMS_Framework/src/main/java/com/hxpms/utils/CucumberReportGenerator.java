package com.hxpms.utils;

import net.masterthought.cucumber.Configuration;
import net.masterthought.cucumber.ReportBuilder;
import net.masterthought.cucumber.presentation.PresentationMode;
import net.masterthought.cucumber.sorting.SortingMethod;

import java.io.File;
import java.util.Collections;
import java.util.List;

public class CucumberReportGenerator {

    public static void generateReport() {
        File reportOutputDir = new File("target/cucumber-html-reports");
        reportOutputDir.mkdirs();

        List<String> jsonFiles = Collections.singletonList(
            "target/cucumber-reports/cucumber.json"
        );

        Configuration config = new Configuration(reportOutputDir, "HX PMS Automation");
        config.addPresentationModes(PresentationMode.EXPAND_ALL_STEPS);
        config.setSortingMethod(SortingMethod.NATURAL);
        config.setNotFailingStatuses(Collections.emptySet());

        // Build report
        ReportBuilder reportBuilder = new ReportBuilder(jsonFiles, config);
        reportBuilder.generateReports();

        System.out.println("\n========================================");
        System.out.println("  Emailable Report: target/cucumber-html-reports/overview-features.html");
        System.out.println("========================================\n");
    }

    public static void main(String[] args) {
        generateReport();
    }
}
