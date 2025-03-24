package com.mobileappsautomationsuite.reusables;

import org.apache.commons.io.FileUtils;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

public class reportGen {

    public static void main(String[] args) {
        String inputFilePath = ""; //Change this file path as per extent
        String outputFilePath = ".txt";
        String excelFilePath = ""; // change run manager as per suite

        try {
            // Extract passed and warning test cases from HTML
            File inputHtmlFile = new File(inputFilePath);
            Document doc = Jsoup.parse(inputHtmlFile, "UTF-8");

            // Create a StringBuilder to store the combined output
            StringBuilder combinedTestCases = new StringBuilder();

            // Extract passed test cases
            Elements passingTestElements = doc.select(".test.pass");
            combinedTestCases.append("PASSED TEST CASES:\n");
            for (Element passingTestElement : passingTestElements) {
                Element testNameElement = passingTestElement.selectFirst(".test-name");
                if (testNameElement != null) {
                    String testCaseName = testNameElement.text();
                    combinedTestCases.append(testCaseName).append("\n");
                }
            }

            // Extract warning test cases
            Elements warningTestElements = doc.select(".test.warning");
            combinedTestCases.append("\nWARNING TEST CASES:\n");
            for (Element warningTestElement : warningTestElements) {
                Element testNameElement = warningTestElement.selectFirst(".test-name");
                if (testNameElement != null) {
                    String testCaseName = testNameElement.text();
                    combinedTestCases.append(testCaseName).append("\n");
                }
            }

            // Save the combined output to a single file
            File outputFile = new File(outputFilePath);
            FileUtils.writeStringToFile(outputFile, combinedTestCases.toString(), "UTF-8");

            System.out.println("Passed and warning test cases extracted and saved to output file.");

            // Load scenario to test case mapping from Excel
            Map<String, String> scenarioToTestCaseMap = new HashMap<>();
            try (FileInputStream fis = new FileInputStream(new File(excelFilePath))) {
                Workbook workbook;
                if (excelFilePath.endsWith(".xls")) {
                    workbook = new HSSFWorkbook(fis);
                } else if (excelFilePath.endsWith(".xlsx")) {
                    workbook = new XSSFWorkbook(fis);
                } else {
                    throw new IllegalArgumentException("The specified file is not Excel file");
                }

                Sheet sheet = workbook.getSheetAt(0);

                for (Row row : sheet) {
                    String scenarioId = row.getCell(3).getStringCellValue();
                    String testCaseIds = row.getCell(2).getStringCellValue();
                    scenarioToTestCaseMap.put(scenarioId, testCaseIds);
                }
            }

            // Process the combined text file to extract scenario IDs and map to test case IDs
            try (BufferedReader br = new BufferedReader(new FileReader(outputFilePath))) {

                String line;
                StringBuilder ticketsFormat = new StringBuilder();

                while ((line = br.readLine()) != null) {
                    int colonIndex = line.indexOf(':');

                    if (colonIndex != -1) {
                        String scenarioId = line.substring(0, colonIndex);
                        String testCaseIds = scenarioToTestCaseMap.get(scenarioId);
                        if (testCaseIds != null) {
                            // Split multiple IDs, wrap each in double quotes, and join with commas
                            String[] ids = testCaseIds.split(",");
                            for (String id : ids) {
                                ticketsFormat.append("\"").append(id.trim()).append("\",");
                            }
                        }
                    }
                }

                // Remove the trailing comma from ticketsFormat
                if (ticketsFormat.length() > 0) {
                    ticketsFormat.setLength(ticketsFormat.length() - 1);
                }

                // Print the tickets format to the console
                System.out.println("Test Case IDs in Tickets Format:");
                System.out.println(ticketsFormat.toString());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
