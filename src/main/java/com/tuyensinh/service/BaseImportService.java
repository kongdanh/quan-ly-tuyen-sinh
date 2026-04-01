package com.tuyensinh.service;

import com.tuyensinh.dao.BangQuyDoiDAO;
import com.tuyensinh.dto.BangQuyDoiImportDTO;
import com.tuyensinh.mapper.BangQuyDoiMapper;
import com.tuyensinh.model.BangQuyDoi;
import com.tuyensinh.util.ExcelReaderUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

public class BaseImportService<D,E> {

    /**
     * Import dữ liệu từ file Excel
     */
    public List<String> importFromExcel(File file, Class<D> dtoClass,
                                        Function<D, E> mapper,
                                        Consumer<List<E>> saveConsumer,
                                        Function<D, String> validateFunction) {
        List<String> errors = new ArrayList<>();

        try {
            // Đọc file Excel
            List<D> dtoList = ExcelReaderUtil.readExcel(file, dtoClass);

            // Validate và lưu
            int rowNumber = 2; // bắt đầu từ row 2 (row 1 là header)
            List<E> entities = new ArrayList<>();
            for (D dto : dtoList) {
                try {
                    // Validate
                    String validationError = validateFunction.apply(dto);
                    if (validationError != null) {
                        errors.add("Dòng " + rowNumber + ": " + validationError);
                        rowNumber++;
                        continue;
                    }

                    // Convert sang entity
                    E entity = mapper.apply(dto);

                    entities.add(entity);
                } catch (Exception e) {
                    errors.add("Dòng " + rowNumber + ": " + e.getMessage());
                }
                rowNumber++;
            }
            // Lưu vào DB
            saveConsumer.accept(entities);
        } catch (Exception e) {
            errors.add("Lỗi đọc file: " + e.getMessage());
        }

        return errors;
    }

    public static void main(String[] args) {
        System.out.println("Current Folder: " + new File(".").getAbsolutePath());
        BaseImportService<BangQuyDoiImportDTO, BangQuyDoi> service = new BaseImportService<>();
        BangQuyDoiDAO dao = new BangQuyDoiDAO();

        long startTime = System.currentTimeMillis();
        List<String> errors = service.importFromExcel(
                new File("Book.xlsx"),
                BangQuyDoiImportDTO.class,
                dto -> BangQuyDoiMapper.toEntity(dto),
                entities -> dao.saveOrUpdateAll(entities),
                dto -> {
                    if (dto.getPhuongthuc() == null) return "Trống phương thức";
                    if (dto.getTohop() == null) return "Trống tổ hợp";
                    return null;
                    }
                );
        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;
        System.out.println("Tổng thời gian thực thi: " + duration + " ms");
        System.out.println("Trung bình: " + (duration / 3000.0) + " ms/dòng");
        if (errors.isEmpty()) {
            System.out.println("Import thành công!");
        } else {
            System.out.println("Có lỗi:");
            errors.forEach(System.out::println);
        }
    }
}
