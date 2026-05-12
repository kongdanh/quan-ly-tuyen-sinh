package com.tuyensinh.util;

import com.tuyensinh.model.KetQuaXetTuyen;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileOutputStream;
import java.util.List;

public class ExcelExportUtil {

    public static void exportDanhSachTrungTuyen(File file, List<KetQuaXetTuyen> listData) throws Exception {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Danh_Sach_Trung_Tuyen");

            // Tạo style cho Header (In đậm, nền xám)
            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);
            headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

            // Dòng Header
            String[] columns = {"STT", "Mã Hồ Sơ", "CCCD", "Họ Tên", "Ngành Trúng Tuyển", "Điểm XT", "Trạng Thái"};
            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < columns.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(columns[i]);
                cell.setCellStyle(headerStyle);
            }

            // Đổ dữ liệu
            int rowNum = 1;
            for (KetQuaXetTuyen kq : listData) {
                // Chỉ xuất những bạn ĐẬU (TRUNG_TUYEN)
                if (!"TRUNG_TUYEN".equals(kq.getTrangThai())) continue;

                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(rowNum - 1);
                row.createCell(1).setCellValue(kq.getHoSo().getMaHoSo());
                row.createCell(2).setCellValue(kq.getHoSo().getThiSinh().getCccd());
                row.createCell(3).setCellValue(kq.getHoSo().getThiSinh().getHo() + " " + kq.getHoSo().getThiSinh().getTen());
                row.createCell(4).setCellValue(kq.getNganh().getTennganh());
                row.createCell(5).setCellValue(kq.getDiemXetTuyen().doubleValue());
                row.createCell(6).setCellValue(kq.getTrangThai());
            }

            // Auto-size các cột
            for (int i = 0; i < columns.length; i++) {
                sheet.autoSizeColumn(i);
            }

            // Ghi ra file
            try (FileOutputStream fos = new FileOutputStream(file)) {
                workbook.write(fos);
            }
        }
    }
}