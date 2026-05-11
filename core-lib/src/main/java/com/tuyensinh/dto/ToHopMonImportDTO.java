package com.tuyensinh.dto;

import com.tuyensinh.annotation.ExcelColumn;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ToHopMonImportDTO {
    @ExcelColumn(value = "Mã tổ hợp", aliases = {"matohop"})
    private String matohop;

    @ExcelColumn(value = "Môn 1", aliases = {"mon1"})
    private String mon1;

    @ExcelColumn(value = "Môn 2", aliases = {"mon2"})
    private String mon2;

    @ExcelColumn(value = "Môn 3", aliases = {"mon3"})
    private String mon3;

    @ExcelColumn(value = "Tên tổ hợp", aliases = {"tentohop"})
    private String tentohop;
}
