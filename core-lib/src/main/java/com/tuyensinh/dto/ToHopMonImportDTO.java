package com.tuyensinh.dto;

import com.tuyensinh.annotation.ExcelColumn;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class ToHopMonImportDTO {

    @ExcelColumn(value = "Mã tổ hợp", aliases = {"Ma to hop", "Matohop", "Ma TH"}, required = true)
    private String matohop;

    @ExcelColumn(value = "Môn 1", aliases = {"Mon 1", "Mon1"}, required = true)
    private String mon1;

    @ExcelColumn(value = "Môn 2", aliases = {"Mon 2", "Mon2"}, required = true)
    private String mon2;

    @ExcelColumn(value = "Môn 3", aliases = {"Mon 3", "Mon3"}, required = true)
    private String mon3;

    @ExcelColumn(value = "Tên tổ hợp", aliases = {"Ten to hop", "Tentohop", "Ten TH"})
    private String tentohop;
}
