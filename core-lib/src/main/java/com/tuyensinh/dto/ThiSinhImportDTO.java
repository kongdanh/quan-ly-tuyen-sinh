package com.tuyensinh.dto;

import com.tuyensinh.annotation.ExcelColumn;
import lombok.Data;

@Data
public class ThiSinhImportDTO {

    @ExcelColumn(value = "CCCD", aliases = {"CMND", "Căn cước công dân"})
    private String cccd;

    @ExcelColumn(value = "sobaodanh", aliases = {"SBD", "Số báo danh", "So bao danh"})
    private String soBaoDanh;

    @ExcelColumn(value = "Họ", aliases = {"Ho", "Họ đệm"})
    private String ho;

    @ExcelColumn(value = "Tên", aliases = {"Ten"})
    private String ten;

    @ExcelColumn(value = "Họ Tên", aliases = {"Ho Ten", "Họ và Tên", "Ho va Ten", "Họ tên", "Ho ten"})
    private String hoTen;

    @ExcelColumn(value = "Ngày sinh", aliases = {"Ngay sinh", "Ngày Sinh"})
    private String ngaySinh;

    @ExcelColumn(value = "NGÀY SINH", aliases = {"NGAY SINH"})
    private String ngaySinhAlt;

    @ExcelColumn(value = "Điện Thoại", aliases = {"SĐT", "SDT"})
    private String dienThoai;

    @ExcelColumn(value = "Giới tính", aliases = {"Gioi tinh", "Giới Tính"})
    private String gioiTinh;

    @ExcelColumn(value = "GIỚI TÍNH", aliases = {"GIOI TINH"})
    private String gioiTinhAlt;

    @ExcelColumn(value = "Email", aliases = {"Hòm thư", "Thư điện tử"})
    private String email;

    @ExcelColumn(value = "ĐTƯT", aliases = {"DTUT", "Đối tượng ưu tiên", "Doi tuong uu tien"})
    private String doiTuongUuTien;

    @ExcelColumn(value = "KVƯT", aliases = {"KVUT", "Khu vực ưu tiên", "Khu vuc uu tien"})
    private String khuVucUuTien;

    @ExcelColumn(value = "NƠI SINH", aliases = {"NOI SINH", "Nơi sinh", "Noi sinh"})
    private String noiSinh;
}