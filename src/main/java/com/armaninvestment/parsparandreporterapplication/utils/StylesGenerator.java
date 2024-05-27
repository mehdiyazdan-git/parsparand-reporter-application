package com.armaninvestment.parsparandreporterapplication.utils;



import com.armaninvestment.parsparandreporterapplication.enums.CustomCellStyle;
import org.apache.poi.ss.usermodel.*;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.Map;

@Component
public class StylesGenerator {

    public Map<CustomCellStyle, CellStyle> prepareStyles(Workbook wb) {
        Font boldArial = createBoldArialFont(wb);
        Font redBoldArial = createRedBoldArialFont(wb);

        CellStyle rightAlignedStyle = createRightAlignedStyle(wb);
        CellStyle greyCenteredBoldArialWithBorderStyle = createGreyCenteredBoldArialWithBorderStyle(wb, boldArial);
        CellStyle redBoldArialWithBorderStyle = createRedBoldArialWithBorderStyle(wb, redBoldArial);
        CellStyle rightAlignedDateFormatStyle = createRightAlignedDateFormatStyle(wb);

        Map<CustomCellStyle, CellStyle> styles = new EnumMap<>(CustomCellStyle.class);
        styles.put(CustomCellStyle.RIGHT_ALIGNED, rightAlignedStyle);
        styles.put(CustomCellStyle.GREY_CENTERED_BOLD_ARIAL_WITH_BORDER, greyCenteredBoldArialWithBorderStyle);
        styles.put(CustomCellStyle.RED_BOLD_ARIAL_WITH_BORDER, redBoldArialWithBorderStyle);
        styles.put(CustomCellStyle.RIGHT_ALIGNED_DATE_FORMAT, rightAlignedDateFormatStyle);

        return styles;
    }

    private Font createBoldArialFont(Workbook wb) {
        Font font = wb.createFont();
        font.setFontName("Arial");
        font.setBold(true);
        return font;
    }

    private Font createRedBoldArialFont(Workbook wb) {
        Font font = wb.createFont();
        font.setFontName("Arial");
        font.setBold(true);
        font.setColor(IndexedColors.RED.getIndex());
        return font;
    }

    private CellStyle createRightAlignedStyle(Workbook wb) {
        CellStyle style = wb.createCellStyle();
        style.setAlignment(HorizontalAlignment.RIGHT);
        return style;
    }

    private CellStyle createGreyCenteredBoldArialWithBorderStyle(Workbook wb, Font boldArial) {
        CellStyle style = createBorderedStyle(wb);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setFont(boldArial);
        style.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        return style;
    }

    private CellStyle createRedBoldArialWithBorderStyle(Workbook wb, Font redBoldArial) {
        CellStyle style = createBorderedStyle(wb);
        style.setFont(redBoldArial);
        return style;
    }

    private CellStyle createRightAlignedDateFormatStyle(Workbook wb) {
        CellStyle style = wb.createCellStyle();
        style.setAlignment(HorizontalAlignment.RIGHT);
        style.setDataFormat((short) 14);
        return style;
    }

    private CellStyle createBorderedStyle(Workbook wb) {
        BorderStyle thin = BorderStyle.THIN;
        short black = IndexedColors.BLACK.getIndex();
        CellStyle style = wb.createCellStyle();
        style.setBorderRight(thin);
        style.setRightBorderColor(black);
        style.setBorderBottom(thin);
        style.setBottomBorderColor(black);
        style.setBorderLeft(thin);
        style.setLeftBorderColor(black);
        style.setBorderTop(thin);
        style.setTopBorderColor(black);
        return style;
    }
}
