package com.tuyensinh.dto;

import com.tuyensinh.annotation.ExcelColumn;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class DiemChuanImportDTO {
    
    @ExcelColumn(value = "Mã Ngành", aliases = {"Mã ngành", "Mã", "Ngành"})
    private String maNganh;
    
    @ExcelColumn(value = "Tổ Hợp", aliases = {"Tổ hợp", "Mã Tổ hợp", "To Hop"})
    private String maToHop;
    
    @ExcelColumn(value = "Điểm Chuẩn", aliases = {"Điểm chuẩn", "Điểm", "Diem chuan"})
    private BigDecimal diemChuan;

}
