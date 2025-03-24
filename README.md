
## **Purpose of the Code**
This Java program extracts test case results from an HTML report, maps test scenarios to test case IDs using an Excel file, and formats them into a ticket-friendly format.

---

## **Code Breakdown**

### **1. Import Statements**
The program uses:
- **Apache POI** (`HSSFWorkbook`, `XSSFWorkbook`, `Sheet`, `Row`) for reading Excel files.
- **JSoup** for parsing HTML.
- **Apache Commons IO** (`FileUtils`) for file operations.
- **Java I/O** classes (`File`, `BufferedReader`, etc.) for reading/writing files.
- **Java Collections** (`HashMap`, `Map`) to store mappings.

---

### **2. File Paths**
```java
String inputFilePath = "C:/Users/punabadi/Apps/reports/2025-03-13/6.28 and.html"; 
String outputFilePath = "C:/Users/punabadi/Apps/reports/2023-08-16/Batch 1 combined.txt";
String excelFilePath = "C:/Users/punabadi/git/MobileAPPQA/diy-app/MobileAppFlexAutomationSuite/input/Run Manager Latest.xls"; 
```
These define:
- `inputFilePath`: Path to the HTML report (which contains test results).
- `outputFilePath`: Path to save extracted test case names.
- `excelFilePath`: Path to the Excel sheet for scenario-to-test-case mapping.

---

### **3. Extracting Test Case Names from HTML**
#### **Reading the HTML File**
```java
File inputHtmlFile = new File(inputFilePath);
Document doc = Jsoup.parse(inputHtmlFile, "UTF-8");
```
- Reads the HTML file using **JSoup** and loads it into a `Document` object.

#### **Extracting "Passed" Test Cases**
```java
Elements passingTestElements = doc.select(".test.pass");
combinedTestCases.append("PASSED TEST CASES:\n");
for (Element passingTestElement : passingTestElements) {
    Element testNameElement = passingTestElement.selectFirst(".test-name");
    if (testNameElement != null) {
        String testCaseName = testNameElement.text();
        combinedTestCases.append(testCaseName).append("\n");
    }
}
```
- Selects all elements with class **`test pass`** (which indicates passed test cases).
- Extracts test names and appends them to `combinedTestCases`.

#### **Extracting "Warning" Test Cases**
```java
Elements warningTestElements = doc.select(".test.warning");
combinedTestCases.append("\nWARNING TEST CASES:\n");
for (Element warningTestElement : warningTestElements) {
    Element testNameElement = warningTestElement.selectFirst(".test-name");
    if (testNameElement != null) {
        String testCaseName = testNameElement.text();
        combinedTestCases.append(testCaseName).append("\n");
    }
}
```
- Selects elements with class **`test warning`** (which indicates test cases with warnings).
- Extracts names and appends them to `combinedTestCases`.

#### **Saving Extracted Data to a File**
```java
File outputFile = new File(outputFilePath);
FileUtils.writeStringToFile(outputFile, combinedTestCases.toString(), "UTF-8");
```
- Writes extracted test cases to the `outputFilePath`.

---

### **4. Loading Scenario to Test Case Mapping from Excel**
#### **Reading the Excel File**
```java
Map<String, String> scenarioToTestCaseMap = new HashMap<>();
try (FileInputStream fis = new FileInputStream(new File(excelFilePath))) {
    Workbook workbook;
    if (excelFilePath.endsWith(".xls")) {
        workbook = new HSSFWorkbook(fis); 
    } else if (excelFilePath.endsWith(".xlsx")) {
        workbook = new XSSFWorkbook(fis);
    } else {
        throw new IllegalArgumentException("The specified file is not an Excel file");
    }
```
- Uses **Apache POI** to open the Excel file.
- Supports both `.xls` (HSSFWorkbook) and `.xlsx` (XSSFWorkbook).

#### **Extracting Scenario IDs and Test Case IDs**
```java
Sheet sheet = workbook.getSheetAt(0);
for (Row row : sheet) {
    String scenarioId = row.getCell(3).getStringCellValue();
    String testCaseIds = row.getCell(2).getStringCellValue();
    scenarioToTestCaseMap.put(scenarioId, testCaseIds);
}
```
- Reads the first sheet (`getSheetAt(0)`).
- Extracts:
  - **Column 3 (Scenario ID)**
  - **Column 2 (Test Case IDs)**
- Stores them in `scenarioToTestCaseMap`.

---

### **5. Mapping Extracted Test Cases to Test Case IDs**
#### **Reading the Extracted Test Cases File**
```java
try (BufferedReader br = new BufferedReader(new FileReader(outputFilePath))) {
    String line;
    StringBuilder ticketsFormat = new StringBuilder();
```
- Reads the extracted test cases from `outputFilePath`.

#### **Matching with Excel Data**
```java
while ((line = br.readLine()) != null) {
    int colonIndex = line.indexOf(':');

    if (colonIndex != -1) {
        String scenarioId = line.substring(0, colonIndex);
        String testCaseIds = scenarioToTestCaseMap.get(scenarioId);
        if (testCaseIds != null) {
            String[] ids = testCaseIds.split(",");
            for (String id : ids) {
                ticketsFormat.append("\"").append(id.trim()).append("\",");
            }
        }
    }
}
```
- **Extracts the scenario ID** (before the `:` character).
- **Looks up test case IDs** in `scenarioToTestCaseMap`.
- **Formats test case IDs** into `"TestID1","TestID2",...`.

#### **Removing the Trailing Comma**
```java
if (ticketsFormat.length() > 0) {
    ticketsFormat.setLength(ticketsFormat.length() - 1);
}
```
- Ensures the last `,` is removed.

#### **Printing the Final Output**
```java
System.out.println("Test Case IDs in Tickets Format:");
System.out.println(ticketsFormat.toString());
```
- Displays test case IDs in a ticket-compatible format.

---

## **Summary of What the Code Does**
1. **Reads an HTML report** and extracts test cases that passed or have warnings.
2. **Saves extracted test cases** into a `.txt` file.
3. **Reads an Excel sheet** to get a mapping of scenario IDs to test case IDs.
4. **Matches extracted test cases with scenario IDs** and converts them to test case IDs.
5. **Formats test case IDs** into a comma-separated list wrapped in quotes.
6. **Prints the formatted test case IDs**.
