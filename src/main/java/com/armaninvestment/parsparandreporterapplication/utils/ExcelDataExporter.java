package com.armaninvestment.parsparandreporterapplication.utils;

import com.github.eloyzone.jalalicalendar.DateConverter;
import com.github.eloyzone.jalalicalendar.JalaliDate;
import com.github.eloyzone.jalalicalendar.JalaliDateFormatter;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.time.LocalDate;
import java.util.List;

public class ExcelDataExporter {

    public static <T> byte[] exportData(List<T> data, Class<T> dtoClass) throws IOException {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Data");

            // Create header style
            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);
            headerStyle.setAlignment(HorizontalAlignment.CENTER);
            headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            setBorders(headerStyle);

            // Create body style
            CellStyle bodyStyle = workbook.createCellStyle();
            setBorders(bodyStyle);

            // Create header row with field names
            Row headerRow = sheet.createRow(0);
            Field[] fields = dtoClass.getDeclaredFields();
            for (int i = 0; i < fields.length; i++) {
                Cell headerCell = headerRow.createCell(i);
                headerCell.setCellValue(fields[i].getName());
                headerCell.setCellStyle(headerStyle);
                sheet.autoSizeColumn(i);
            }


            int rowNum = 1;
            for (T item : data) {
                Row dataRow = sheet.createRow(rowNum++);
                for (int i = 0; i < fields.length; i++) {
                    fields[i].setAccessible(true);
                    Cell cell = dataRow.createCell(i);
                    cell.setCellStyle(bodyStyle);
                    try {
                        Object value = fields[i].get(item);
                        if (value != null) {
                            if (value instanceof Number) {
                                cell.setCellValue(((Number) value).doubleValue());
                            } else if (value instanceof Boolean) {
                                cell.setCellValue((Boolean) value);
                            }else if (value instanceof LocalDate) {
                                cell.setCellValue(convertDateToJalali((LocalDate) value));
                            } else {
                                cell.setCellValue(value.toString());
                            }
                        }
                    } catch (IllegalAccessException e) {
                        throw new RuntimeException("Error accessing field value", e);
                    }
                }
            }

            // Write the output to a byte array
            try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
                workbook.write(outputStream);
                return outputStream.toByteArray();
            }
        }
    }

    private static void setBorders(CellStyle style) {
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setTopBorderColor(IndexedColors.BLACK.getIndex());
        style.setBottomBorderColor(IndexedColors.BLACK.getIndex());
        style.setRightBorderColor(IndexedColors.BLACK.getIndex());
        style.setLeftBorderColor(IndexedColors.BLACK.getIndex());
    }

    private static String convertDateToJalali(LocalDate localDate) {
        if (localDate == null) {
            return null;
        }
        DateConverter dateConverter = new DateConverter();
        int gregorianYear = localDate.getYear();
        int gregorianMonth = localDate.getMonthValue();
        int gregorianDay = localDate.getDayOfMonth();
        JalaliDate jalaliDate = dateConverter.gregorianToJalali(gregorianYear, gregorianMonth, gregorianDay);

        if (jalaliDate != null) {
            return jalaliDate.format(new JalaliDateFormatter("yyyy/mm/dd"));
        }
        return null;
    }
}
