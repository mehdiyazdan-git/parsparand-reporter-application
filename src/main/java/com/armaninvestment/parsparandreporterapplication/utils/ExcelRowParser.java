package com.armaninvestment.parsparandreporterapplication.utils;

import com.github.eloyzone.jalalicalendar.DateConverter;
import com.github.eloyzone.jalalicalendar.JalaliDate;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.springframework.util.StringUtils;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.time.LocalDate;

public class ExcelRowParser {

    public static <T> T parseRowToDto(Row row, Class<T> dtoClass, int rowNum) {
        try {
            Constructor<T> constructor = dtoClass.getDeclaredConstructor();
            T dtoInstance = constructor.newInstance();
            Field[] fields = dtoClass.getDeclaredFields();

            for (int colNum = 0; colNum < fields.length; colNum++) {
                Field field = fields[colNum];
                field.setAccessible(true);
                Object value = getCellValue(row.getCell(colNum), field.getType());
                field.set(dtoInstance, value);
            }
            return dtoInstance;
        } catch (Exception e) {
            throw new RuntimeException("Error creating DTO instance: " + e.getMessage(), e);
        }
    }

    private static Object getCellValue(Cell cell, Class<?> targetType) {
        if (cell == null || cell.getCellType() == CellType.BLANK) {
            return null;
        }

        if (targetType == String.class) {
            return cell.getStringCellValue();
        }else if (targetType == Long.class) {
            return convertCellToLong(cell);
        }else if (targetType == LocalDate.class) {
            return convertCellToDate(cell);
        } else if (targetType == Boolean.class) {
            return convertCellToBoolean(cell);
        } else if (targetType.isEnum()) {
            return convertCellToEnum(cell, (Class<Enum>) targetType);
        }else if (targetType == Double.class) {
            return convertCellToDouble(cell);
        }else if (targetType == Integer.class) {
            return convertCellToInteger(cell);
        }else if (targetType == Float.class) {
            return convertCellToFloat(cell);
        }else if (targetType == BigDecimal.class) {
            return convertCellToBigDecimal(cell);
        }

        throw new IllegalArgumentException("Unsupported target type: " + targetType);
    }

    private static Long convertCellToLong(Cell cell) {
        if (cell.getCellType() == CellType.NUMERIC) {
            return (long) cell.getNumericCellValue();
        } else if (cell.getCellType() == CellType.STRING) {
            String cellValue = cell.getStringCellValue();
            return cellValue.isEmpty() ? null : Long.parseLong(cellValue);
        } else {
            throw new IllegalArgumentException("Cell contains non-numeric data");
        }
    }

    private static Double convertCellToDouble(Cell cell) {
        if (cell.getCellType() == CellType.NUMERIC) {
            return cell.getNumericCellValue();
        } else if (cell.getCellType() == CellType.STRING) {
            String cellValue = cell.getStringCellValue();
            return cellValue.isEmpty() ? null : Double.parseDouble(cellValue);
        } else {
            throw new IllegalArgumentException("Cell contains non-numeric data");
        }
    }

    private static Integer convertCellToInteger(Cell cell) {
        if (cell.getCellType() == CellType.NUMERIC) {
            return (int) cell.getNumericCellValue();
        } else if (cell.getCellType() == CellType.STRING) {
            String cellValue = cell.getStringCellValue();
            return cellValue.isEmpty() ? null : Integer.parseInt(cellValue);
        } else {
            throw new IllegalArgumentException("Cell contains non-numeric data");
        }
    }

    private static BigDecimal convertCellToBigDecimal(Cell cell){
        if (cell.getCellType() == CellType.NUMERIC) {
            return BigDecimal.valueOf(cell.getNumericCellValue());
        } else if (cell.getCellType() == CellType.STRING) {
            String cellValue = cell.getStringCellValue();
            return StringUtils.hasText(cellValue) ? new BigDecimal(cellValue) : null;
        } else {
            throw new IllegalArgumentException("Cell contains non-numeric data");
        }
    }
    //convert cell to float
    private static Float convertCellToFloat(Cell cell) {
        if (cell.getCellType() == CellType.NUMERIC) {
            return (float) cell.getNumericCellValue();
        } else if (cell.getCellType() == CellType.STRING) {
            String cellValue = cell.getStringCellValue();
            return cellValue.isEmpty() ? null : Float.parseFloat(cellValue);
        } else {
            throw new IllegalArgumentException("Cell contains non-numeric data");
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
            throw new IllegalArgumentException("Cell contains non-boolean data");
        }
    }

    private static <E extends Enum<E>> E convertCellToEnum(Cell cell, Class<E> enumType) {
        if (cell.getCellType() == CellType.STRING) {
            String cellValue = cell.getStringCellValue().trim();
            try {
                return Enum.valueOf(enumType, cellValue);
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Invalid enum value: " + cellValue + " for enum class " + enumType.getName(), e);
            }
        } else {
            throw new IllegalArgumentException("Cell must contain string data for enum conversion");
        }
    }

}
