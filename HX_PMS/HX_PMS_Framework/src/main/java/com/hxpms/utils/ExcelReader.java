package com.hxpms.utils;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ExcelReader {
    private Workbook workbook;

    public ExcelReader(String filePath) throws IOException {
        FileInputStream fis = new FileInputStream(filePath);
        workbook = new XSSFWorkbook(fis);
    }

    public List<Map<String, String>> getData(String sheetName) {
        List<Map<String, String>> data = new ArrayList<>();
        Sheet sheet = workbook.getSheet(sheetName);
        if (sheet == null) return data;

        Row headerRow = sheet.getRow(0);
        if (headerRow == null) return data;

        for (int i = 1; i <= sheet.getLastRowNum(); i++) {
            Row row = sheet.getRow(i);
            if (row == null) continue;

            Map<String, String> rowData = new HashMap<>();
            for (int j = 0; j < headerRow.getLastCellNum(); j++) {
                Cell headerCell = headerRow.getCell(j);
                Cell dataCell = row.getCell(j);
                String header = headerCell != null ? headerCell.getStringCellValue() : "";
                String value = dataCell != null ? dataCell.toString() : "";
                rowData.put(header, value);
            }
            data.add(rowData);
        }
        return data;
    }

    public void close() throws IOException {
        workbook.close();
    }
}