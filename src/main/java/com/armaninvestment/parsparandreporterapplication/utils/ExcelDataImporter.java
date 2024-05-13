package com.armaninvestment.parsparandreporterapplication.utils;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class ExcelDataImporter {

    public static <T> List<T> importData(MultipartFile file, Class<T> dtoClass) throws IOException {
        if (file.isEmpty()) {
            throw new RuntimeException("The provided file is empty.");
        }

        List<T> dtos = new ArrayList<>();
        try (Workbook workbook = new XSSFWorkbook(file.getInputStream())) {
            Sheet sheet = workbook.getSheetAt(0);
            Iterator<Row> rows = sheet.rowIterator();

            // Skip the header row
            if (rows.hasNext()) {
                rows.next();
            }

            int rowNum = 1; // Start counting from 1 for the first data row
            while (rows.hasNext()) {
                Row currentRow = rows.next();
                try {
                    T dto = ExcelRowParser.parseRowToDto(currentRow, dtoClass, rowNum);
                    dtos.add(dto);
                } catch (RuntimeException e) {
                    System.out.println("Error at row " + rowNum + ": " + e.getMessage());
                    e.printStackTrace();
                    throw new RuntimeException("Error at row " + rowNum + ": " + e.getMessage(), e);
                }
                rowNum++;
            }
        }
        return dtos;
    }
}
