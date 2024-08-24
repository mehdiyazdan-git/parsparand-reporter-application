package com.armaninvestment.parsparandreporterapplication.utils;

import org.apache.poi.ss.usermodel.*;

public class CellStyleHelper {
    public CellStyle getCellStyle(Workbook workbook) {
        return initiaTeCellStyle(workbook);
    }

    public CellStyle getHeaderCellStyle(Workbook workbook) {
        CellStyle cellStyle = workbook.createCellStyle();
        cellStyle.setAlignment(HorizontalAlignment.CENTER);
        cellStyle.setVerticalAlignment(VerticalAlignment.CENTER);
        cellStyle.setFont(getHeaderFont(workbook));
        cellStyle.setFillForegroundColor(IndexedColors.LIGHT_BLUE.getIndex());
        cellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        cellStyle.setBorderTop(BorderStyle.THIN);
        cellStyle.setBorderBottom(BorderStyle.THIN);
        cellStyle.setBorderLeft(BorderStyle.THIN);
        cellStyle.setBorderRight(BorderStyle.THIN);

        return cellStyle;
    }

    public CellStyle getFooterCellStyle(Workbook workbook) {
        CellStyle cellStyle = initiaTeCellStyle(workbook);

        DataFormat dataFormat = workbook.createDataFormat();
        cellStyle.setDataFormat(dataFormat.getFormat("#,##0"));

        return cellStyle;
    }

    public CellStyle getMonatoryCellStyle(Workbook workbook) {

        CellStyle cellStyle = initiaTeCellStyle(workbook);
        DataFormat dataFormat = workbook.createDataFormat();
        cellStyle.setDataFormat(dataFormat.getFormat("#,##0"));

        return cellStyle;
    }

    public Font getFont(Workbook workbook) {
        Font font = workbook.createFont();
        font.setFontHeightInPoints((short) 11);
        font.setFontName("B Nazanin");
        return font;
    }

    public Font getHeaderFont(Workbook workbook) {
        Font font = workbook.createFont();
        font.setFontHeightInPoints((short) 11);
        font.setFontName("B Nazanin");
        font.setBold(true);
        return font;
    }

    private CellStyle initiaTeCellStyle(Workbook workbook) {
        CellStyle cellStyle = workbook.createCellStyle();
        cellStyle.setAlignment(HorizontalAlignment.CENTER);
        cellStyle.setVerticalAlignment(VerticalAlignment.CENTER);
        cellStyle.setFont(getFont(workbook));
        cellStyle.setBorderTop(BorderStyle.THIN);
        cellStyle.setBorderBottom(BorderStyle.THIN);
        cellStyle.setBorderLeft(BorderStyle.THIN);
        cellStyle.setBorderRight(BorderStyle.THIN);
        return cellStyle;
    }
}
