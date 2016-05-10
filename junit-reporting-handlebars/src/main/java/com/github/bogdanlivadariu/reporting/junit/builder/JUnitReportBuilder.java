package com.github.bogdanlivadariu.reporting.junit.builder;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.apache.commons.io.FileUtils;

import com.github.bogdanlivadariu.reporting.junit.helpers.Constants;
import com.github.bogdanlivadariu.reporting.junit.helpers.Helpers;
import com.github.bogdanlivadariu.reporting.junit.xml.models.TestCaseModel;
import com.github.bogdanlivadariu.reporting.junit.xml.models.TestSuiteModel;
import com.github.jknack.handlebars.Handlebars;
import com.github.jknack.handlebars.Template;

public class JUnitReportBuilder {
    private String TEST_SUMMARY_REPORT = "junit-reporting/testCaseSummaryReport";

    private String TEST_OVERVIEW_REPORT = "junit-reporting/testOverviewReport";

    private final String TEST_OVERVIEW_PATH;

    private final String TEST_SUMMARY_PATH;

    private List<TestSuiteModel> processedTestSuites;

    public List<TestSuiteModel> getProcessedTestSuites() {
        return processedTestSuites;
    }

    public JUnitReportBuilder(List<String> xmlReports, String targetBuildPath) throws FileNotFoundException,
        JAXBException {
        TEST_OVERVIEW_PATH = targetBuildPath + "/";
        TEST_SUMMARY_PATH = targetBuildPath + "/test-summary/";
        processedTestSuites = new ArrayList<>();

        processXmlReports(xmlReports);
    }

    private List<TestSuiteModel> processXmlReports(List<String> xmlReports) throws JAXBException {
        JAXBContext cntx = JAXBContext.newInstance(TestSuiteModel.class);

        Unmarshaller unm = cntx.createUnmarshaller();

        for (String xml : xmlReports) {

            Logger.getGlobal().info("Processing: " + xml);
            TestSuiteModel ts = (TestSuiteModel) unm.unmarshal(new File(xml));
            for (TestCaseModel tc : ts.getTestcase()) {
                if (tc.getError() != null) {
                    System.out.println();
                }
            }
            ts.postProcess();
            for (TestCaseModel tc : ts.getTestcase()) {
                tc.postProcess();
            }
            processedTestSuites.add(ts);
        }
        return processedTestSuites;
    }

    private void writeTestOverviewReport() throws IOException {
        Template template = new Helpers(new Handlebars()).registerHelpers().compile(TEST_OVERVIEW_REPORT);
        AllJUnitReports allFeatures = new AllJUnitReports("Test suites overview", processedTestSuites);
        FileUtils.writeStringToFile(new File(TEST_OVERVIEW_PATH + "testSuitesOverview.html"),
            template.apply(allFeatures));
    }

    private void writeTestCaseSummaryReport() throws IOException {
        Template template = new Helpers(new Handlebars()).registerHelpers().compile(TEST_SUMMARY_REPORT);
        for (TestSuiteModel ts : processedTestSuites) {
            String content = template.apply(ts);
            FileUtils.writeStringToFile(new File(TEST_SUMMARY_PATH + ts.getUniqueID() + ".html"),
                content);
        }
    }

    private void writeTestsPassedReport() throws IOException {
        Template template = new Helpers(new Handlebars()).registerHelpers().compile(TEST_OVERVIEW_REPORT);

        List<TestSuiteModel> onlyPassed = new ArrayList<>(getProcessedTestSuites());
        for (Iterator<TestSuiteModel> it = onlyPassed.listIterator(); it.hasNext();) {
            TestSuiteModel f = it.next();
            if (f.getOverallStatus().equalsIgnoreCase(Constants.FAILED)) {
                it.remove();
            }
        }

        AllJUnitReports allTestSuites = new AllJUnitReports("Passed test suites report", onlyPassed);
        FileUtils.writeStringToFile(new File(TEST_OVERVIEW_PATH + "testsPassed.html"),
            template.apply(allTestSuites));
    }

    private void writeTestsFailedReport() throws IOException {
        Template template = new Helpers(new Handlebars()).registerHelpers().compile(TEST_OVERVIEW_REPORT);

        List<TestSuiteModel> onlyFailed = new ArrayList<>(getProcessedTestSuites());
        for (Iterator<TestSuiteModel> it = onlyFailed.listIterator(); it.hasNext();) {
            TestSuiteModel f = it.next();
            if (f.getOverallStatus().equalsIgnoreCase(Constants.PASSED)) {
                it.remove();
            }
        }

        AllJUnitReports allTestSuites = new AllJUnitReports("Failed test suites report", onlyFailed);
        FileUtils.writeStringToFile(new File(TEST_OVERVIEW_PATH + "testsFailed.html"),
            template.apply(allTestSuites));
    }

    public boolean writeReportsOnDisk() throws IOException {
        writeTestOverviewReport();
        writeTestCaseSummaryReport();
        writeTestsPassedReport();
        writeTestsFailedReport();
        for (TestSuiteModel ts : processedTestSuites) {
            if (Integer.parseInt(ts.getFailures()) >= 1
                || Integer.parseInt(ts.getErrors()) >= 1
                || Integer.parseInt(ts.getSkipped()) >= 1) {
                return false;
            }
        }
        return true;
    }

}
