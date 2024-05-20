package com.armaninvestment.parsparandreporterapplication.utils;

import com.armaninvestment.parsparandreporterapplication.enums.ProductType;
import com.github.eloyzone.jalalicalendar.DateConverter;
import com.github.eloyzone.jalalicalendar.JalaliDate;
import org.apache.poi.ss.usermodel.*;
import org.springframework.util.StringUtils;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

public class ExcelRowParser {

    public static <T> T parseRowToDto(Row row, Class<T> dtoClass, int rowNum) {
        try {
            Constructor<T> constructor = dtoClass.getDeclaredConstructor();
            T dtoInstance = constructor.newInstance();
            Field[] fields = dtoClass.getDeclaredFields();
            Map<Integer, String> columnNames = getColumnNames(fields);

            for (int colNum = 0; colNum < fields.length; colNum++) {
                Field field = fields[colNum];
                field.setAccessible(true);
                try {
                    Object value = getCellValue(row.getCell(colNum), field.getType(), columnNames, colNum, rowNum);
                    field.set(dtoInstance, value);
                } catch ( NumberFormatException e) {
                    String columnName = columnNames.getOrDefault(colNum, "ستون ناشناخته");
                    String errorMsg = "خطا در ردیف " + rowNum + "، ستون " + columnName + ": " + e.getMessage();
                    throw new RuntimeException(errorMsg, e);
                }
            }
            return dtoInstance;
        } catch (Exception e) {
            throw new RuntimeException("خطا در ایجاد نمونه DTO: " + e.getMessage(), e);
        }
    }

    private static Map<Integer, String> getColumnNames(Field[] fields) {
        Map<Integer, String> columnNames = new HashMap<>();
        for (int i = 0; i < fields.length; i++) {
            columnNames.put(i, fields[i].getName());
        }
        return columnNames;
    }

    private static Object getCellValue(Cell cell, Class<?> targetType, Map<Integer, String> columnNames, int colNum, int rowNum) {
        if (cell == null || cell.getCellType() == CellType.BLANK) {
            return null;
        }

        try {
            if (targetType == String.class) {
                return cell.getCellType() == CellType.NUMERIC ? String.valueOf((long) cell.getNumericCellValue()) : cell.getStringCellValue();
            } else if (targetType == Long.class) {
                return convertCellToLong(cell);
            } else if (targetType == LocalDate.class) {
                return convertCellToDate(cell);
            } else if (targetType == Boolean.class) {
                return convertCellToBoolean(cell);
            } else if (targetType.isEnum()) {
                return convertCellToEnum(cell, (Class<Enum>) targetType);
            } else if (targetType == Double.class) {
                return convertCellToDouble(cell);
            } else if (targetType == Integer.class) {
                return convertCellToInteger(cell);
            } else if (targetType == Float.class) {
                return convertCellToFloat(cell);
            } else if (targetType == BigDecimal.class) {
                return convertCellToBigDecimal(cell);
            }
        } catch (IllegalArgumentException e) {
            String columnName = columnNames.getOrDefault(colNum, "ستون ناشناخته");
            String errorMsg = "خطا در ردیف " + rowNum + "، ستون " + columnName + ": " + e.getMessage();
            throw new IllegalArgumentException(errorMsg, e);
        }

        throw new IllegalArgumentException("نوع هدف پشتیبانی نمی‌شود: " + targetType);
    }

    private static Long convertCellToLong(Cell cell) {
        if (cell.getCellType() == CellType.NUMERIC) {
            return (long) cell.getNumericCellValue();
        } else if (cell.getCellType() == CellType.STRING) {
            String cellValue = cell.getStringCellValue();
            return cellValue.isEmpty() ? null : Long.parseLong(cellValue);
        } else {
            throw new IllegalArgumentException("سلول حاوی داده‌های غیر عددی است");
        }
    }

    private static Double convertCellToDouble(Cell cell) {
        if (cell.getCellType() == CellType.NUMERIC) {
            return cell.getNumericCellValue();
        } else if (cell.getCellType() == CellType.STRING) {
            String cellValue = cell.getStringCellValue();
            return cellValue.isEmpty() ? null : Double.parseDouble(cellValue);
        } else {
            throw new IllegalArgumentException("سلول حاوی داده‌های غیر عددی است");
        }
    }

    private static Integer convertCellToInteger(Cell cell) {
        if (cell.getCellType() == CellType.NUMERIC) {
            return (int) cell.getNumericCellValue();
        } else if (cell.getCellType() == CellType.STRING) {
            String cellValue = cell.getStringCellValue();
            return cellValue.isEmpty() ? null : Integer.parseInt(cellValue);
        } else {
            throw new IllegalArgumentException("سلول حاوی داده‌های غیر عددی است");
        }
    }

    private static BigDecimal convertCellToBigDecimal(Cell cell) {
        if (cell.getCellType() == CellType.NUMERIC) {
            return BigDecimal.valueOf(cell.getNumericCellValue());
        } else if (cell.getCellType() == CellType.STRING) {
            String cellValue = cell.getStringCellValue();
            return StringUtils.hasText(cellValue) ? new BigDecimal(cellValue) : null;
        } else {
            throw new IllegalArgumentException("سلول حاوی داده‌های غیر عددی است");
        }
    }

    private static Float convertCellToFloat(Cell cell) {
        if (cell.getCellType() == CellType.NUMERIC) {
            return (float) cell.getNumericCellValue();
        } else if (cell.getCellType() == CellType.STRING) {
            String cellValue = cell.getStringCellValue();
            return cellValue.isEmpty() ? null : Float.parseFloat(cellValue);
        } else {
            throw new IllegalArgumentException("سلول حاوی داده‌های غیر عددی است");
        }
    }

    private static LocalDate convertCellToDate(Cell cell) {
        DateConverter dateConverter = new DateConverter();
        String[] parts = cell.getStringCellValue().split("-");
        int jalaliYear = Integer.parseInt(parts[0]);
        int jalaliMonth = Integer.parseInt(parts[1]);
        int jalaliDay = Integer.parseInt(parts[2]);
        JalaliDate jalaliDate = dateConverter.gregorianToJalali(jalaliYear, jalaliMonth, jalaliDay);
        if (jalaliDate != null) {
            return LocalDate.of(jalaliDate.getYear(), jalaliDate.getMonthPersian().getValue(), jalaliDate.getDay());
        }
        return null;
    }

    private static Boolean convertCellToBoolean(Cell cell) {
        if (cell.getCellType() == CellType.BOOLEAN) {
            return cell.getBooleanCellValue();
        } else if (cell.getCellType() == CellType.STRING) {
            String cellValue = cell.getStringCellValue();
            return cellValue.isEmpty() ? null : Boolean.parseBoolean(cellValue);
        } else {
            throw new IllegalArgumentException("سلول حاوی داده‌های غیر منطقی است");
        }
    }

    private static <E extends Enum<E>> E convertCellToEnum(Cell cell, Class<E> enumType) {
        if (cell.getCellType() == CellType.STRING) {
            String cellValue = cell.getStringCellValue().trim();
            try {
                return Enum.valueOf(enumType, cellValue);
            } catch (IllegalArgumentException e) {
                if (enumType == ProductType.class) {
                    return (E) ProductType.fromCaption(cellValue);
                }
                throw new IllegalArgumentException("مقدار نادرست enum: " + cellValue + " برای کلاس " + enumType.getName(), e);
            }
        } else {
            throw new IllegalArgumentException("سلول باید حاوی داده‌های متنی برای تبدیل به enum باشد");
        }
    }
}
