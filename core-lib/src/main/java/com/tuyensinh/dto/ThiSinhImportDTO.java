package com.tuyensinh.dto;

import com.tuyensinh.annotation.ExcelColumn;
import lombok.Data;

@Data
public class ThiSinhImportDTO {

    @ExcelColumn(value = "CCCD", aliases = {"CMND", "Căn cước công dân"})
    private String cccd;

    @ExcelColumn(value = "SBD", aliases = {"Số báo danh", "So bao danh"})
    private String soBaoDanh;

    @ExcelColumn(value = "Họ", aliases = {"Ho", "Họ đệm"})
    private String ho;

    @ExcelColumn(value = "Tên", aliases = {"Ten"})
    private String ten;

    @ExcelColumn(value = "Họ Tên", aliases = {"Ho Ten", "Họ và Tên", "Ho va Ten", "Họ tên"})
    private String hoTen;

    @ExcelColumn(value = "Ngày Sinh", aliases = {"Ngay sinh"})
    private String ngaySinh;

    @ExcelColumn(value = "Điện Thoại", aliases = {"SĐT", "SDT"})
    private String dienThoai;

    @ExcelColumn(value = "Giới Tính", aliases = {"Gioi tinh"})
    private String gioiTinh;

    @ExcelColumn(value = "Email", aliases = {"Hòm thư", "Thư điện tử"})
    private String email;
}