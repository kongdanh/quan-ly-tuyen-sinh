package com.tuyensinh.dto;

import com.tuyensinh.annotation.ExcelColumn;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * DTO ánh xạ 1 dòng dữ liệu từ file Excel DGNL/VSAT.
 *
 * <p>Cả 2 sheet có cùng cấu trúc cột:
 * <pre>
 * STT | CMND | DOTTHI | MADOTTHI | NGAYTHI | NAMTHI | MAMONTHI | TENMONTHI | DIEM | THANGDIEM | MADVTCTDL | TENDVTCTDL
 * </pre>
 *
 * <ul>
 *   <li><b>Sheet DGNL</b>: MAMONTHI = "DGNL", DIEM nằm trong thang 1200 → ghi vào cột {@code NL1}.</li>
 *   <li><b>Sheet VSAT</b>: MAMONTHI ∈ {"TO_VS","LI_VS","VA_VS","N1_VS",...},
 *       DIEM nằm trong thang 150 → quy đổi về thang 10 rồi ghi
 *       vào cột tương ứng (TO/LI/VA/N1_THI) trong {@code xt_diemthixettuyen}.</li>
 * </ul>
 *
 * <p>Trường {@code sheetName} KHÔNG đọc từ Excel — được gán thủ công sau khi
 * {@link com.tuyensinh.util.ExcelReaderUtil} đọc xong từng sheet.
 */
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class DgnlVsatRowDTO {

    /**
     * Tên sheet nguồn. Không có trong file Excel;
     * được gán bởi {@code processDgnlVsatImport()} sau khi đọc sheet.
     */
    private String sheetName;

    /** Số thứ tự dòng trong file — dùng để log lỗi chính xác. */
    @ExcelColumn(value = "STT", aliases = {"Stt", "stt"})
    private String stt;

    /**
     * Mã số căn cước / CMND của thí sinh.
     * Cột trong Excel là "CMND" (tương ứng {@code cccd} trong DB).
     */
    @ExcelColumn(value = "CMND", aliases = {"Cmnd", "cccd", "CCCD", "So CCCD"}, required = true)
    private String cmnd;

    /** Số thứ tự đợt thi. */
    @ExcelColumn(value = "DOTTHI", aliases = {"Dot thi", "DotThi"})
    private String dotthi;

    /** Mã đợt thi (ví dụ: "CTU255", "256", "1"). */
    @ExcelColumn(value = "MADOTTHI", aliases = {"Ma dot thi", "MaDotThi"})
    private String madotthi;

    /** Ngày thi (chuỗi, không parse thành Date để tránh mất dữ liệu). */
    @ExcelColumn(value = "NGAYTHI", aliases = {"Ngay thi", "NgayThi"})
    private String ngaythi;

    /** Năm thi. */
    @ExcelColumn(value = "NAMTHI", aliases = {"Nam thi", "NamThi"})
    private String namthi;

    /**
     * Mã môn thi — cột phân loại chính.
     * <ul>
     *   <li>DGNL: "DGNL"</li>
     *   <li>VSAT: "TO_VS" | "LI_VS" | "VA_VS" | "N1_VS"</li>
     * </ul>
     */
    @ExcelColumn(value = "MAMONTHI", aliases = {"Ma mon thi", "MaMonThi", "ma_mon_thi"}, required = true)
    private String mamonthi;

    /** Tên môn thi (chỉ dùng để log). */
    @ExcelColumn(value = "TENMONTHI", aliases = {"Ten mon thi", "TenMonThi"})
    private String tenmonthi;

    /**
     * Điểm thi thô (theo thang của kỳ thi đó).
     * <ul>
     *   <li>DGNL: thang 1200</li>
     *   <li>VSAT: thang 150</li>
     * </ul>
     */
    @ExcelColumn(value = "DIEM", aliases = {"Diem", "diem", "Score"}, required = true)
    private BigDecimal diem;

    /**
     * Thang điểm tối đa của kỳ thi.
     * Dùng để quy đổi: {@code diemQuyDoi = diem / thangdiem * 10}.
     */
    @ExcelColumn(value = "THANGDIEM", aliases = {"Thang diem", "ThangDiem", "thang_diem"})
    private BigDecimal thangdiem;

    /** Mã đơn vị tổ chức thi (chỉ dùng để log). */
    @ExcelColumn(value = "MADVTCTDL", aliases = {"Ma DV", "MaDV"})
    private String madvtctdl;

    /** Tên đơn vị tổ chức thi (chỉ dùng để log). */
    @ExcelColumn(value = "TENDVTCTDL", aliases = {"Ten DV", "TenDV"})
    private String tendvtctdl;
}
