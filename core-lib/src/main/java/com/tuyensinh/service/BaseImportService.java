package com.tuyensinh.service;

import com.tuyensinh.util.ExcelReaderUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

public class BaseImportService<D, E> {

    /**
     * Import dữ liệu từ file Excel (ALL-OR-NOTHING)
     */
    public List<String> importFromExcel(File file, Class<D> dtoClass,
                                        Function<D, E> mapper,
                                        Consumer<List<E>> saveConsumer,
                                        Function<D, String> validateFunction) {
        List<String> errors = new ArrayList<>();

        try {
            // Đọc file Excel
            List<D> dtoList = ExcelReaderUtil.readExcel(file, dtoClass);

            int rowNumber = 2;
            List<E> entities = new ArrayList<>();

            for (D dto : dtoList) {
                try {
                    // Validate Logic
                    String validationError = validateFunction.apply(dto);
                    if (validationError != null) {
                        errors.add("Dòng " + rowNumber + ": " + validationError);
                    } else {
                        // Convert sang entity
                        E entity = mapper.apply(dto);
                        entities.add(entity);
                    }
                } catch (Exception e) {
                    errors.add("Dòng " + rowNumber + ": Lỗi định dạng dữ liệu - " + e.getMessage());
                }
                rowNumber++;
            }

            if (!errors.isEmpty()) {
                return errors;
            }

            saveConsumer.accept(entities);

        } catch (Exception e) {
            errors.add("Lỗi hệ thống: " + e.getMessage());
        }

        return errors;
    }
}