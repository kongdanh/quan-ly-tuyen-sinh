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
public class DiemCongImportDTO {

    @ExcelColumn(value = "CCCD thí sinh", aliases = {"CCCD", "Cccd", "So CCCD"}, required = true)
    private String tsCccd;

    @ExcelColumn(value = "Mã ngành", aliases = {"Ma nganh", "Manganh"}, required = true)
    private String manganh;

    @ExcelColumn(value = "Mã tổ hợp", aliases = {"Ma to hop", "Matohop"}, required = true)
    private String matohop;

    @ExcelColumn(value = "Phương thức", aliases = {"Phuong thuc", "Phuongthuc", "PT"}, required = true)
    private String phuongthuc;

    @ExcelColumn(value = "Điểm cộng chứng chỉ", aliases = {"Diem CC", "DiemCC", "Diem cong CC"})
    private BigDecimal diemCC;

    @ExcelColumn(value = "Điểm ưu tiên xét tuyển", aliases = {"Diem Ut xt", "DiemUtxt", "Diem uu tien"})
    private BigDecimal diemUtxt;

    @ExcelColumn(value = "Điểm tổng", aliases = {"Diem tong", "DiemTong", "Tong diem"})
    private BigDecimal diemTong;

    @ExcelColumn(value = "Ghi chú", aliases = {"Ghi chu", "Ghichu", "Note"})
    private String ghichu;

    @ExcelColumn(value = "Mã khóa", aliases = {"Ma khoa", "DC Keys", "Key"})
    private String dcKeys;
}
