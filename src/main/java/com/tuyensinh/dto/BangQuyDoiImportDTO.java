package com.tuyensinh.dto;

import com.tuyensinh.annotation.ExcelColumn;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class BangQuyDoiImportDTO {

    @ExcelColumn(value = "Phương thức", aliases = {"Phuong thuc", "PT"}, required = true)
    private String phuongthuc;

    @ExcelColumn(value = "Tổ hợp", aliases = {"To hop", "Tohop", "TH"}, required = true)
    private String tohop;

    @ExcelColumn(value = "Môn", aliases = {"Mon", "Môn học", "Mon hoc"}, required = true)
    private String mon;

    @ExcelColumn(value = "Điểm A", aliases = {"Diem A", "DiemA", "A"})
    private BigDecimal diemA;

    @ExcelColumn(value = "Điểm B", aliases = {"Diem B", "DiemB", "B"})
    private BigDecimal diemB;

    @ExcelColumn(value = "Điểm C", aliases = {"Diem C", "DiemC", "C"})
    private BigDecimal diemC;

    @ExcelColumn(value = "Điểm D", aliases = {"Diem D", "DiemD", "D"})
    private BigDecimal diemD;

    @ExcelColumn(value = "Mã quy đổi", aliases = {"Ma quy doi", "Maquydoi", "Ma QD"})
    private String maquydoi;

    @ExcelColumn(value = "Phân vị", aliases = {"Phan vi", "Phanvi", "PV"})
    private String phanvi;
}
