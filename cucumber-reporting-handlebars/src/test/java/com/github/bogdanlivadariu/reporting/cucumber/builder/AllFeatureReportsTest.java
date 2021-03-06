package com.github.bogdanlivadariu.reporting.cucumber.builder;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.JAXBException;

import org.junit.Before;
import org.junit.Test;

import com.github.bogdanlivadariu.reporting.cucumber.helpers.SpecialProperties;

public class AllFeatureReportsTest {

    private CucumberReportBuilder builder;

    private AllFeatureReports reports;

    @Before
    public void setUp() throws JAXBException, IOException {
        List<String> jsonReports = new ArrayList<>();

        String firstReport = AllFeatureReportsTest.class.getResource("/result.json").getPath();
        String secondReport = AllFeatureReportsTest.class.getResource("/cucumber.json").getPath();

        jsonReports.add(firstReport);
        jsonReports.add(secondReport);

        builder = new CucumberReportBuilder(jsonReports, "output", new SpecialProperties());
        reports = new AllFeatureReports("title", builder.getProcessedFeatures());
    }

    @Test
    public void tagSizeTest() {
        assertEquals(9, reports.getAllTags().size());
    }

    @Test
    public void featureSizeTest() {
        assertEquals(4, reports.getFeatures().size());
        reports.getPageTitle();
        reports.getScenariosTotal();
        reports.getScenariosTotalFailed();
        reports.getScenariosTotalPassed();
        reports.getStepsTotal();
    }

    @Test
    public void scenariosTest() {
        assertEquals(21, reports.getScenariosTotal());
        assertEquals(1, reports.getScenariosTotalFailed());
        assertEquals(20, reports.getScenariosTotalPassed());
    }

    @Test
    public void stepsTest() {
        assertEquals(92, reports.getStepsTotal());
        assertEquals(1, reports.getStepsTotalFailed());
        assertEquals(88, reports.getStepsTotalPassed());
        assertEquals(3, reports.getStepsTotalSkipped());
        assertEquals(0, reports.getStepsTotalUndefined());
    }

    @Test
    public void totalDurationTest() {
        long duration = Long.parseLong("206919001170");
        assertEquals(duration, reports.getTotalDuration());
    }

    @Test
    public void builderShouldFail() throws IOException {
        assertFalse(builder.writeReportsOnDisk());
    }
}
