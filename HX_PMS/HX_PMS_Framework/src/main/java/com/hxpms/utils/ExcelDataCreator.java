package com.hxpms.utils;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.*;

import java.io.File;
import java.io.FileOutputStream;

public class ExcelDataCreator {

    public static void main(String[] args) throws Exception {
        createGuestDataExcel();
    }

    public static void createGuestDataExcel() throws Exception {
        XSSFWorkbook workbook = new XSSFWorkbook();

        // ── Styles ────────────────────────────────────────────────────────────
        XSSFCellStyle headerStyle = workbook.createCellStyle();
        XSSFFont headerFont = workbook.createFont();
        headerFont.setBold(true);
        headerFont.setColor(IndexedColors.WHITE.getIndex());
        headerFont.setFontHeightInPoints((short) 11);
        headerStyle.setFont(headerFont);
        headerStyle.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
        headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        headerStyle.setAlignment(HorizontalAlignment.CENTER);
        headerStyle.setBorderBottom(BorderStyle.THIN);
        headerStyle.setBorderTop(BorderStyle.THIN);
        headerStyle.setBorderLeft(BorderStyle.THIN);
        headerStyle.setBorderRight(BorderStyle.THIN);

        XSSFCellStyle dataStyle = workbook.createCellStyle();
        dataStyle.setBorderBottom(BorderStyle.THIN);
        dataStyle.setBorderTop(BorderStyle.THIN);
        dataStyle.setBorderLeft(BorderStyle.THIN);
        dataStyle.setBorderRight(BorderStyle.THIN);

        XSSFCellStyle altStyle = workbook.createCellStyle();
        altStyle.cloneStyleFrom(dataStyle);
        altStyle.setFillForegroundColor(IndexedColors.LIGHT_CORNFLOWER_BLUE.getIndex());
        altStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

        // ── Sheet 1: WalkIn_Guest ─────────────────────────────────────────────
        createSheet(workbook, "WalkIn_Guest", headerStyle, dataStyle, altStyle,
            new String[]{"scenario", "booking_type", "rooms", "nights",
                         "first_name", "last_name", "email", "mobile",
                         "address", "zip", "adults", "kids"},
            new String[][]{
                {"Walk-In Booking", "Walk In", "1", "1",
                 "Jully", "Williams", "jully.williams@example.com", "5551234567",
                 "123 Main Street", "10001", "1", "0"}
            }
        );

        // ── Sheet 2: Reservation_Guest ────────────────────────────────────────
        createSheet(workbook, "Reservation_Guest", headerStyle, dataStyle, altStyle,
            new String[]{"scenario", "booking_type", "rooms", "nights",
                         "first_name", "last_name", "email", "mobile",
                         "address", "zip", "adults", "kids"},
            new String[][]{
                {"Reservation Booking", "Reservation", "5", "6",
                 "Kelvin", "Disuza", "kelvin.disuza@example.com", "5559876543",
                 "456 Ocean Avenue", "90210", "5", "0"}
            }
        );

        // ── Sheet 3: WalkIn_NSDB_Guest ────────────────────────────────────────
        createSheet(workbook, "WalkIn_NSDB_Guest", headerStyle, dataStyle, altStyle,
            new String[]{"scenario", "booking_type", "rooms", "nights", "room_type",
                         "first_name", "last_name", "email", "mobile",
                         "address", "zip", "adults", "kids"},
            new String[][]{
                {"Walk-In NSDB Booking", "Walk In", "1", "1", "Non Smoking Double Beds",
                 "Joyvita", "Williams", "joyvita.williams@example.com", "5553219870",
                 "88 Lakeview Drive", "30301", "2", "0"}
            }
        );

        // ── Sheet 4: Reservation_NSDB_Guest ──────────────────────────────────
        createSheet(workbook, "Reservation_NSDB_Guest", headerStyle, dataStyle, altStyle,
            new String[]{"scenario", "booking_type", "rooms", "nights", "room_type",
                         "first_name", "last_name", "email", "mobile",
                         "address", "zip", "adults", "kids"},
            new String[][]{
                {"Reservation NSDB Booking", "Reservation", "5", "6", "Non Smoking Double Beds",
                 "Joyvita", "Williams", "joyvita.williams2@example.com", "5557654321",
                 "22 Riverside Road", "77001", "5", "1"}
            }
        );

        // ── Save file ─────────────────────────────────────────────────────────
        File dir = new File("src/test/resources/testdata");
        dir.mkdirs();
        File file = new File(dir, "GuestData.xlsx");
        try (FileOutputStream fos = new FileOutputStream(file)) {
            workbook.write(fos);
        }
        workbook.close();
        System.out.println("✓ Excel file created: " + file.getAbsolutePath());
    }

    private static void createSheet(XSSFWorkbook wb, String sheetName,
            XSSFCellStyle headerStyle, XSSFCellStyle dataStyle, XSSFCellStyle altStyle,
            String[] headers, String[][] rows) {

        XSSFSheet sheet = wb.createSheet(sheetName);

        // Header row
        Row headerRow = sheet.createRow(0);
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i].toUpperCase().replace("_", " "));
            cell.setCellStyle(headerStyle);
            sheet.setColumnWidth(i, 22 * 256);
        }

        // Data rows
        for (int r = 0; r < rows.length; r++) {
            Row row = sheet.createRow(r + 1);
            XSSFCellStyle style = (r % 2 == 0) ? dataStyle : altStyle;
            for (int c = 0; c < rows[r].length; c++) {
                Cell cell = row.createCell(c);
                cell.setCellValue(rows[r][c]);
                cell.setCellStyle(style);
            }
        }
    }
}
