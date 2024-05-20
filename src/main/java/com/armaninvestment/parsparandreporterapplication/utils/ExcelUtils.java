package com.armaninvestment.parsparandreporterapplication.utils;

import org.apache.poi.ss.usermodel.*;
import com.github.eloyzone.jalalicalendar.DateConverter;
import com.github.eloyzone.jalalicalendar.JalaliDate;

import java.time.LocalDate;

public class ExcelUtils {

    public static Long getCellLongValue(Row row, int cellIndex, int rowNum) {
        try {
            Cell cell = row.getCell(cellIndex);
            if (cell.getCellType() == CellType.NUMERIC) {
                return (long) cell.getNumericCellValue();
            } else if (cell.getCellType() == CellType.STRING) {
                return Long.parseLong(cell.getStringCellValue());
            } else {
                throw new IllegalArgumentException("نوع سلول نامعتبر است");
            }
        } catch (Exception e) {
            throw new RuntimeException("خطا در ردیف " + rowNum + "، ستون " + (cellIndex + 1) + ": " + e.getMessage(), e);
        }
    }

    public static Integer getCellIntValue(Row row, int cellIndex, int rowNum) {
        try {
            Cell cell = row.getCell(cellIndex);
            if (cell.getCellType() == CellType.NUMERIC) {
                return (int) cell.getNumericCellValue();
            } else if (cell.getCellType() == CellType.STRING) {
                return Integer.parseInt(cell.getStringCellValue());
            } else {
                throw new IllegalArgumentException("نوع سلول نامعتبر است");
            }
        } catch (Exception e) {
            throw new RuntimeException("خطا در ردیف " + rowNum + "، ستون " + (cellIndex + 1) + ": " + e.getMessage(), e);
        }
    }

    public static String getCellStringValue(Row row, int cellIndex, int rowNum) {
        try {
            Cell cell = row.getCell(cellIndex);
            if (cell.getCellType() == CellType.STRING) {
                return cell.getStringCellValue();
            } else if (cell.getCellType() == CellType.NUMERIC) {
                return String.valueOf((long) cell.getNumericCellValue());
            } else {
                throw new IllegalArgumentException("نوع سلول نامعتبر است");
            }
        } catch (Exception e) {
            throw new RuntimeException("خطا در ردیف " + rowNum + "، ستون " + (cellIndex + 1) + ": " + e.getMessage(), e);
        }
    }

    public static LocalDate convertToDate(String dateStr) {

        DateConverter dateConverter = new DateConverter();

        String[] parts = dateStr.split("/");

        int jalaliYear = Integer.parseInt(parts[0]);
        int jalaliMonth = Integer.parseInt(parts[1]);
        int jalaliDay = Integer.parseInt(parts[2]);

        return dateConverter.jalaliToGregorian(jalaliYear, jalaliMonth, jalaliDay);
    }
}
