package com.tuyensinh.dto;

import com.tuyensinh.annotation.ExcelColumn;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class DiemThiImportDTO {

    @ExcelColumn(value = "CCCD", aliases = {"Căn cước công dân", "So CCCD"})
    private String cccd;

    @ExcelColumn(value = "SBD", aliases = {"Số báo danh", "So bao danh"})
    private String soBaoDanh;

    @ExcelColumn(value = "Phương thức", aliases = {"d_phuongthuc", "PTXT", "Phuong thuc"})
    private String phuongThuc;

    @ExcelColumn(value = "TO", aliases = {"Toan"})
    private BigDecimal to;

    @ExcelColumn(value = "LI", aliases = {"Ly", "Vat ly"})
    private BigDecimal li;

    @ExcelColumn(value = "HO", aliases = {"Hoa"})
    private BigDecimal ho;

    @ExcelColumn(value = "SI", aliases = {"Sinh"})
    private BigDecimal si;

    @ExcelColumn(value = "SU", aliases = {"Su", "Lich su"})
    private BigDecimal su;

    @ExcelColumn(value = "DI", aliases = {"Dia", "Dia ly"})
    private BigDecimal di;

    @ExcelColumn(value = "VA", aliases = {"Van", "Ngu van"})
    private BigDecimal va;

    @ExcelColumn(value = "N1_THI", aliases = {"N1", "NN", "TA_THI", "Anh thi", "Ngoai ngu", "Ngoại ngữ"})
    private BigDecimal n1Thi;

    @ExcelColumn(value = "N1_CC", aliases = {"TA_CC", "Anh CC"})
    private BigDecimal n1Cc;

    @ExcelColumn(value = "CNCN")
    private BigDecimal cncn;

    @ExcelColumn(value = "CNNN")
    private BigDecimal cnnn;

    @ExcelColumn(value = "TI", aliases = {"Tin"})
    private BigDecimal ti;

    @ExcelColumn(value = "KTPL", aliases = {"GDCD"})
    private BigDecimal ktpl;

    @ExcelColumn(value = "NL1", aliases = {"DGNL"})
    private BigDecimal nl1;

    @ExcelColumn(value = "NK1")
    private BigDecimal nk1;

    @ExcelColumn(value = "NK2")
    private BigDecimal nk2;
}
