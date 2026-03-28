package com.jd.apvd.util;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;

public final class ExcelImportUtils {

    private ExcelImportUtils() {
    }

    public static boolean isRowEmpty(Row row) {
        if (row == null) {
            return true;
        }

        for (int i = row.getFirstCellNum(); i < row.getLastCellNum(); i++) {
            Cell cell = row.getCell(i);
            if (cell != null && cell.getCellType() != CellType.BLANK) {
                String value = getCellString(row, i);
                if (!value.isBlank()) {
                    return false;
                }
            }
        }
        return true;
    }

    public static String getCellString(Row row, int index) {
        if (row == null) {
            return "";
        }

        Cell cell = row.getCell(index);
        if (cell == null) {
            return "";
        }

        return switch (cell.getCellType()) {
            case STRING -> cell.getStringCellValue().trim();
            case NUMERIC -> {
                double numericValue = cell.getNumericCellValue();
                if (numericValue == Math.floor(numericValue)) {
                    yield String.valueOf((long) numericValue);
                }
                yield String.valueOf(numericValue);
            }
            case BOOLEAN -> String.valueOf(cell.getBooleanCellValue());
            case FORMULA -> {
                try {
                    yield cell.getStringCellValue().trim();
                } catch (IllegalStateException ex) {
                    double numericValue = cell.getNumericCellValue();
                    if (numericValue == Math.floor(numericValue)) {
                        yield String.valueOf((long) numericValue);
                    }
                    yield String.valueOf(numericValue);
                }
            }
            default -> "";
        };
    }

    public static Integer getCellInteger(Row row, int index) {
        String value = getCellString(row, index);
        if (value.isBlank()) {
            return null;
        }
        return Integer.parseInt(value);
    }
}
