package com.armaninvestment.parsparandreporterapplication.utils;

import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;

public class RowStyle {
    public static void setRowStyle(Row row, CellStyle cellStyle) {
        for (int i = 0; i < row.getLastCellNum(); i++) {
            row.getCell(i).setCellStyle(cellStyle);
        }
    }
}