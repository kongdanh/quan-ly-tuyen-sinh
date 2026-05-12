package com.tuyensinh.util;

import com.tuyensinh.annotation.ExcelColumn;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.usermodel.DataFormatter;

import java.io.File;
import java.io.FileInputStream;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.text.Normalizer;
import java.util.*;

public class ExcelReaderUtil {

    // Khởi tạo DataFormatter dùng chung để đọc mọi định dạng (chống lỗi .0 và E10)
    private static final DataFormatter dataFormatter = new DataFormatter();

    public static <T> List<T> readExcel(File file, Class<T> dtoClass) throws Exception {
        List<T> result = new ArrayList<>();

        try (FileInputStream fis = new FileInputStream(file);
               Workbook workbook = WorkbookFactory.create(fis)) {

            Sheet sheet = workbook.getSheetAt(0);
            Iterator<Row> rowIterator = sheet.iterator();

            if (!rowIterator.hasNext()) {
                throw new IllegalArgumentException("File Excel rỗng");
            }

            // Đọc header
            Row headerRow = rowIterator.next();
            Map<String, Integer> headerMap = buildHeaderMap(headerRow, dtoClass);

            // Đọc data
            while (rowIterator.hasNext()) {
                Row row = rowIterator.next();
                if (isEmptyRow(row)) {
                    continue;
                }

                T dto = mapRowToDTO(row, headerMap, dtoClass);
                result.add(dto);
            }
        }
        return result;
    }

    private static <T> Map<String, Integer> buildHeaderMap(Row headerRow, Class<T> dtoClass) {
        Map<String, Integer> headerMap = new HashMap<>();

        for (Cell cell : headerRow) {
            String headerValue = normalize(getCellValue(cell));
            headerMap.put(headerValue, cell.getColumnIndex());
        }

        Map<String, Integer> fieldIndexMap = new HashMap<>();
        for (Field field : dtoClass.getDeclaredFields()) {
            if (field.isAnnotationPresent(ExcelColumn.class)) {
                ExcelColumn annotation = field.getAnnotation(ExcelColumn.class);

                String normalizedMain = normalize(annotation.value());
                if (headerMap.containsKey(normalizedMain)) {
                    fieldIndexMap.put(field.getName(), headerMap.get(normalizedMain));
                    continue;
                }

                for (String alias : annotation.aliases()) {
                    String normalizedAlias = normalize(alias);
                    if (headerMap.containsKey(normalizedAlias)) {
                        fieldIndexMap.put(field.getName(), headerMap.get(normalizedAlias));
                        break;
                    }
                }
            }
        }
        return fieldIndexMap;
    }

    private static <T> T mapRowToDTO(Row row, Map<String, Integer> headerMap, Class<T> dtoClass) throws Exception {
        T dto = dtoClass.getDeclaredConstructor().newInstance();

        for (Field field : dtoClass.getDeclaredFields()) {
            field.setAccessible(true);

            Integer colIndex = headerMap.get(field.getName());
            if (colIndex == null) {
                continue;
            }

            Cell cell = row.getCell(colIndex);
            Object value = getCellValueAsType(cell, field.getType());
            field.set(dto, value);
        }
        return dto;
    }

    /**
     * Lấy giá trị cell dưới dạng String
     */
    private static String getCellValue(Cell cell) {
        if (cell == null) {
            return "";
        }
        return dataFormatter.formatCellValue(cell).trim();
    }

    private static Object getCellValueAsType(Cell cell, Class<?> type) {
        if (cell == null) {
            return null;
        }

        String value = getCellValue(cell).trim();
        if (value.isEmpty()) {
            return null;
        }

        if (type == String.class) {
            return value;
        } else if (type == BigDecimal.class) {
            return new BigDecimal(value.replace(',', '.'));
        } else if (type == Integer.class || type == int.class) {
            return (int) Double.parseDouble(value);
        } else if (type == Double.class || type == double.class) {
            return Double.parseDouble(value);
        } else if (type == Boolean.class || type == boolean.class) {
            return Boolean.parseBoolean(value);
        }
        return value;
    }

    private static String normalize(String input) {
        if (input == null) {
            return "";
        }
        String normalized = Normalizer.normalize(input, Normalizer.Form.NFD);
        normalized = normalized.replaceAll("\\p{M}", ""); 
        normalized = normalized.toLowerCase().trim();
        normalized = normalized.replaceAll("\\s+", ""); 
        return normalized;
    }

    /**
     * Kiểm tra row có rỗng không
     */
    private static boolean isEmptyRow(Row row) {
        if (row == null) {
            return true;
        }
        for (Cell cell : row) {
            if (cell != null && cell.getCellType() != CellType.BLANK) {
                String value = getCellValue(cell).trim();
                if (!value.isEmpty()) {
                    return false;
                }
            }
        }
        return true;
    }
}