package com.tuyensinh.service;

import com.tuyensinh.util.ExcelReaderUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

public class BaseImportService<D, E> {

    /**
     * Import du lieu tu file Excel theo co che ALL-OR-NOTHING.
     * Tat ca dong phai hop le moi luu vao database, neu co bat ky loi nao thi khong luu dong nao.
     */
    public List<String> importFromExcel(File file, Class<D> dtoClass,
                                        Function<D, E> mapper,
                                        Consumer<List<E>> saveConsumer,
                                        Function<D, String> validateFunction) {
        List<String> errors = new ArrayList<>();

        try {
            List<D> dtoList = ExcelReaderUtil.readExcel(file, dtoClass);

            int rowNumber = 2;
            List<E> entities = new ArrayList<>();

            for (D dto : dtoList) {
                try {
                    String validationError = validateFunction.apply(dto);
                    if (validationError != null) {
                        errors.add("Dong " + rowNumber + ": " + validationError);
                    } else {
                        E entity = mapper.apply(dto);
                        entities.add(entity);
                    }
                } catch (Exception e) {
                    errors.add("Dong " + rowNumber + ": Loi dinh dang du lieu - " + e.getMessage());
                }
                rowNumber++;
            }

            if (!errors.isEmpty()) {
                return errors;
            }

            saveConsumer.accept(entities);

        } catch (Exception e) {
            errors.add("Loi he thong: " + e.getMessage());
        }

        return errors;
    }
}