package com.armaninvestment.parsparandreporterapplication.utils;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;

public class ExcelTemplateGenerator {

    public static <T> byte[] generateTemplateExcel(Class<T> dtoClass) throws IOException {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Template");
            Row headerRow = sheet.createRow(0);

            Field[] fields = dtoClass.getDeclaredFields();
            CellStyle headerStyle = createHeaderCellStyle(workbook);

            // Create headers based on the fields of the DTO class
            for (int i = 0; i < fields.length; i++) {
                Field field = fields[i];
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(field.getName());
                cell.setCellStyle(headerStyle);
                sheet.autoSizeColumn(i);
            }

            try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
                workbook.write(outputStream);
                return outputStream.toByteArray();
            }
        }
    }

    private static CellStyle createHeaderCellStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();

        // Create a font and set it to bold
        Font font = workbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 11); // Set font size if necessary
        style.setFont(font);

        // Set the fill pattern and foreground color for cell background
        style.setFillForegroundColor(IndexedColors.SKY_BLUE.getIndex()); // Assuming light blue background
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);

        // Set cell text to be centered horizontally and vertically
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);

        // Set borders
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);

        // Set border color if necessary (e.g., black)
        style.setTopBorderColor(IndexedColors.BLACK.getIndex());
        style.setBottomBorderColor(IndexedColors.BLACK.getIndex());
        style.setLeftBorderColor(IndexedColors.BLACK.getIndex());
        style.setRightBorderColor(IndexedColors.BLACK.getIndex());

        // Set wrap text
        style.setWrapText(true);

        return style;
    }

}
