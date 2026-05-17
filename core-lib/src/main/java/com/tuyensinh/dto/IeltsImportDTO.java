package com.tuyensinh.dto;

import com.tuyensinh.annotation.ExcelColumn;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * DTO đại diện cho 1 dòng dữ liệu trong file Excel import IELTS / chứng chỉ ngoại ngữ.
 *
 * <p>Cấu trúc file Excel tối thiểu (header không phân biệt hoa/thường):
 * <pre>
 * | CCCD | Điểm quy đổi | Điểm cộng |
 * </pre>
 */
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class IeltsImportDTO {

    /**
     * Căn cước công dân (khóa chính định danh thí sinh).
     */
    @ExcelColumn(value = "CCCD", aliases = {"cccd", "So CCCD", "CCCD thi sinh"}, required = true)
    private String cccd;

    /**
     * Điểm quy đổi chứng chỉ ngoại ngữ (0–10 scale).
     * Dùng để so sánh / thay thế N1_THI khi có chứng chỉ tốt hơn.
     */
    @ExcelColumn(value = "Điểm quy đổi", aliases = {"diem_qd", "Diem QD", "DiemQD", "Diem quy doi"})
    private BigDecimal diemQd;

    /**
     * Điểm cộng (ưu tiên / khu vực / đối tượng...) áp dụng cho xét tuyển không có Ngoại ngữ.
     * Lưu vào cột {@code diemCC} của {@code xt_diemcongxetuyen}.
     */
    @ExcelColumn(value = "Điểm cộng", aliases = {"diem_cong", "Diem Cong", "DiemCong", "Diem cong"})
    private BigDecimal diemCong;
}
