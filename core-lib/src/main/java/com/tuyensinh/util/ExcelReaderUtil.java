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
        try (FileInputStream fis = new FileInputStream(file);
             Workbook workbook = WorkbookFactory.create(fis)) {
            Sheet sheet = workbook.getSheetAt(0);
            return readSheetInternal(sheet, dtoClass);
        }
    }

    /**
     * Đọc 1 sheet cụ thể theo tên (case-insensitive).
     *
     * @param file      File Excel.
     * @param sheetName Tên sheet cần đọc (ví dụ: "DGNL", "VSAT").
     * @param dtoClass  Class DTO để ánh xạ.
     * @return Danh sách DTO từ sheet đó; rỗng nếu không tìm thấy sheet.
     * @throws Exception Nếu lỗi đọc file.
     */
    public static <T> List<T> readSheet(File file, String sheetName, Class<T> dtoClass) throws Exception {
        List<T> result = new ArrayList<>();

        try (FileInputStream fis = new FileInputStream(file);
             Workbook workbook = WorkbookFactory.create(fis)) {

            Sheet sheet = workbook.getSheet(sheetName);
            if (sheet == null) {
                // Thử tìm case-insensitive
                for (int i = 0; i < workbook.getNumberOfSheets(); i++) {
                    if (workbook.getSheetName(i).equalsIgnoreCase(sheetName)) {
                        sheet = workbook.getSheetAt(i);
                        break;
                    }
                }
            }
            if (sheet == null) {
                throw new IllegalArgumentException("Không tìm thấy sheet: " + sheetName);
            }

            result = readSheetInternal(sheet, dtoClass);
        }
        return result;
    }

    /**
     * Đọc tất cả sheet trong file Excel, trả về Map theo tên sheet.
     *
     * <p>Dùng cho import DGNL/VSAT khi file có nhiều sheet cần xử lý khác nhau.
     *
     * @param file     File Excel.
     * @param dtoClass Class DTO dùng chung cho tất cả sheet.
     * @return Map &lt;tênSheet, danhSáchDTO&gt; theo thứ tự sheet trong file.
     * @throws Exception Nếu lỗi đọc file.
     */
    public static <T> Map<String, List<T>> readAllSheets(File file, Class<T> dtoClass) throws Exception {
        // Dùng LinkedHashMap để giữ thứ tự sheet
        Map<String, List<T>> result = new LinkedHashMap<>();

        try (FileInputStream fis = new FileInputStream(file);
             Workbook workbook = WorkbookFactory.create(fis)) {

            int sheetCount = workbook.getNumberOfSheets();
            for (int i = 0; i < sheetCount; i++) {
                Sheet sheet = workbook.getSheetAt(i);
                String name  = workbook.getSheetName(i);
                try {
                    result.put(name, readSheetInternal(sheet, dtoClass));
                } catch (Exception e) {
                    // Sheet không có header hợp lệ → bỏ qua, ghi log
                    System.err.println("[ExcelReaderUtil] Bỏ qua sheet '" + name + "': " + e.getMessage());
                }
            }
        }
        return result;
    }

    /**
     * Phần lõi: đọc 1 {@link Sheet} đã mở vào danh sách DTO.
     * Được tách ra để tái sử dụng bởi tất cả các overload.
     */
    private static <T> List<T> readSheetInternal(Sheet sheet, Class<T> dtoClass) throws Exception {
        List<T> result = new ArrayList<>();
        Iterator<Row> rowIterator = sheet.iterator();

        if (!rowIterator.hasNext()) {
            return result; // Sheet rỗng
        }

        Row headerRow = rowIterator.next();
        Map<String, Integer> headerMap = buildHeaderMap(headerRow, dtoClass);

        while (rowIterator.hasNext()) {
            Row row = rowIterator.next();
            if (isEmptyRow(row)) continue;
            T dto = mapRowToDTO(row, headerMap, dtoClass);
            result.add(dto);
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