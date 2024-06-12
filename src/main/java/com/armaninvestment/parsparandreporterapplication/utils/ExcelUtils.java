package com.armaninvestment.parsparandreporterapplication.utils;

import com.armaninvestment.parsparandreporterapplication.exceptions.RowColumnException;
import org.apache.poi.ss.usermodel.*;
import com.github.eloyzone.jalalicalendar.DateConverter;

import java.time.LocalDate;

public class ExcelUtils {

    public static Long getCellLongValue(Row row, int cellIndex, int rowNum) {
        try {
            Cell cell = row.getCell(cellIndex);
            if (cell == null || cell.getCellType() == CellType.BLANK) {
                return null;
            }
            if (cell.getCellType() == CellType.NUMERIC) {
                return (long) cell.getNumericCellValue();
            } else if (cell.getCellType() == CellType.STRING) {
                return Long.parseLong(cell.getStringCellValue());
            } else {
                throw new IllegalArgumentException("نوع سلول نامعتبر است");
            }
        } catch (IllegalArgumentException e) {
            throw new RowColumnException(rowNum, cellIndex + 1, e.getMessage(), e);
        }
    }

    public static Integer getCellIntValue(Row row, int cellIndex, int rowNum) {
        try {
            Cell cell = row.getCell(cellIndex);
            if (cell == null || cell.getCellType() == CellType.BLANK) {
                return null;
            }
            if (cell.getCellType() == CellType.NUMERIC) {
                return (int) cell.getNumericCellValue();
            } else if (cell.getCellType() == CellType.STRING) {
                return Integer.parseInt(cell.getStringCellValue());
            } else {
                throw new IllegalArgumentException("نوع سلول نامعتبر است");
            }
        } catch (IllegalArgumentException e) {
            throw new RowColumnException(rowNum, cellIndex + 1, e.getMessage(), e);        }
    }

    public static Double getCellDoubleValue(Row row, int cellIndex, int rowNum) {
        try {
            Cell cell = row.getCell(cellIndex);
            if (cell == null || cell.getCellType() == CellType.BLANK) {
                return null;
            }
            if (cell.getCellType() == CellType.NUMERIC) {
                return cell.getNumericCellValue();
            } else if (cell.getCellType() == CellType.STRING) {
                return Double.parseDouble(cell.getStringCellValue());
            } else {
                throw new IllegalArgumentException("نوع سلول نامعتبر است");
            }
        } catch (IllegalArgumentException e) {
            throw new RowColumnException(rowNum, cellIndex + 1, e.getMessage(), e);        }
    }


    public static String getCellStringValue(Row row, int cellIndex, int rowNum) {
        try {
            Cell cell = row.getCell(cellIndex);
            if (cell == null || cell.getCellType() == CellType.BLANK) {
                return null;
            }
            if (cell.getCellType() == CellType.STRING) {
                return cell.getStringCellValue();
            } else if (cell.getCellType() == CellType.NUMERIC) {
                return String.valueOf((long) cell.getNumericCellValue());
            } else {
                throw new IllegalArgumentException("نوع سلول نامعتبر است");
            }
        } catch (IllegalArgumentException e) {
            throw new RowColumnException(rowNum, cellIndex + 1, e.getMessage(), e);        }
    }

    public static LocalDate convertToDate(String dateStr) {
        if (dateStr == null || dateStr.isEmpty()){
            return null;
        }

        DateConverter dateConverter = new DateConverter();

        String[] parts = dateStr.split("/");

        int jalaliYear = Integer.parseInt(parts[0]);
        int jalaliMonth = Integer.parseInt(parts[1]);
        int jalaliDay = Integer.parseInt(parts[2]);

        return dateConverter.jalaliToGregorian(jalaliYear, jalaliMonth, jalaliDay);
    }
}
