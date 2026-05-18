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
public class IeltsImportDTO {

    @ExcelColumn(value = "CCCD", aliases = {"cccd", "So CCCD", "CCCD thi sinh"}, required = true)
    private String cccd;

    /**
     * Điểm/ Bậc chứng chỉ gốc đọc trực tiếp từ Excel (ví dụ: 7.5, 6.0...).
     */
    @ExcelColumn(value = "Điểm/ Bậc chứng chỉ", aliases = {"diem_raw", "Diem IELTS", "Diem thot", "Diem thong so", "Diem/ Bac chung chi"})
    private BigDecimal diemIeltsRaw;

    @ExcelColumn(value = "Điểm quy đổi", aliases = {"diem_qd", "Diem QD", "DiemQD", "Diem quy doi"})
    private BigDecimal diemQd;

    @ExcelColumn(value = "Điểm cộng", aliases = {"diem_cong", "Diem Cong", "DiemCong", "Diem cong"})
    private BigDecimal diemCong;
}