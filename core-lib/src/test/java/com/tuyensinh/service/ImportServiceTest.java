package com.tuyensinh.service;

import com.tuyensinh.dto.DiemThiImportDTO;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.Method;
import java.math.BigDecimal;

/**
 * Unit test cho ImportService – kiểm tra logic validate điểm thi.
 *
 * Nhóm test:
 *  TC-IMP-03 đến TC-IMP-05B : validateMethodRules – THPT
 *  TC-IMP-06 đến TC-IMP-07B : validateMethodRules – DGNL
 *  TC-IMP-08 đến TC-IMP-08C : validateMethodRules – VSAT
 *  TC-IMP-09 đến TC-IMP-15  : validateScoreRange (khoảng điểm)
 *  TC-IMP-16                 : validateMethodRules – method null
 */
public class ImportServiceTest {

    private ImportService importService;
    private Method validateMethodRulesMethod;
    private Method validateScoreRangeMethod;

    @Before
    public void setUp() throws Exception {
        importService = new ImportService();

        validateMethodRulesMethod = ImportService.class.getDeclaredMethod(
                "validateMethodRules", DiemThiImportDTO.class, String.class);
        validateMethodRulesMethod.setAccessible(true);

        validateScoreRangeMethod = ImportService.class.getDeclaredMethod(
                "validateScoreRange", DiemThiImportDTO.class);
        validateScoreRangeMethod.setAccessible(true);
    }

    private String invokeValidateMethodRules(DiemThiImportDTO dto, String method) throws Exception {
        return (String) validateMethodRulesMethod.invoke(importService, dto, method);
    }

    private String invokeValidateScoreRange(DiemThiImportDTO dto) throws Exception {
        return (String) validateScoreRangeMethod.invoke(importService, dto);
    }

    // ===============================================================
    // THPT
    // ===============================================================

    /** TC-IMP-03: THPT với đủ 3 môn hợp lệ → không lỗi */
    @Test
    public void testTC_IMP_03_THPTHopLe() throws Exception {
        DiemThiImportDTO dto = new DiemThiImportDTO();
        dto.setTo(new BigDecimal("8.0"));
        dto.setLi(new BigDecimal("7.5"));
        dto.setHo(new BigDecimal("9.0"));

        String error = invokeValidateMethodRules(dto, "THPT");
        Assert.assertNull("TC-IMP-03: THPT với 3 môn hợp lệ không được báo lỗi", error);
    }

    /** TC-IMP-04: THPT có thêm điểm NL1 (file tổng hợp) → vẫn hợp lệ */
    @Test
    public void testTC_IMP_04_THPTChoPhepMonKhac() throws Exception {
        DiemThiImportDTO dto = new DiemThiImportDTO();
        dto.setTo(new BigDecimal("8.0"));
        dto.setLi(new BigDecimal("7.5"));
        dto.setHo(new BigDecimal("9.0"));
        dto.setNl1(new BigDecimal("800"));

        String error = invokeValidateMethodRules(dto, "THPT");
        Assert.assertNull("TC-IMP-04: THPT có điểm NL1 (file tổng) vẫn hợp lệ", error);
    }

    /** TC-IMP-05: THPT chỉ có 2 môn → vẫn hợp lệ do nới lỏng validation */
    @Test
    public void testTC_IMP_05_THPTChoPhepThieuMon() throws Exception {
        DiemThiImportDTO dto = new DiemThiImportDTO();
        dto.setTo(new BigDecimal("8.0"));
        dto.setLi(new BigDecimal("7.5"));

        String error = invokeValidateMethodRules(dto, "THPT");
        Assert.assertNull("TC-IMP-05: THPT < 3 môn vẫn hợp lệ do nới lỏng validation", error);
    }

    /** TC-IMP-05B: THPT không có môn nào → vẫn hợp lệ (file tổng rỗng điểm) */
    @Test
    public void testTC_IMP_05B_THPTKhongCoMon() throws Exception {
        DiemThiImportDTO dto = new DiemThiImportDTO();

        String error = invokeValidateMethodRules(dto, "THPT");
        Assert.assertNull("TC-IMP-05B: THPT không có môn nào vẫn hợp lệ", error);
    }

    // ===============================================================
    // DGNL
    // ===============================================================

    /** TC-IMP-06: DGNL có điểm NL1 hợp lệ → không lỗi */
    @Test
    public void testTC_IMP_06_DGNLHopLe() throws Exception {
        DiemThiImportDTO dto = new DiemThiImportDTO();
        dto.setNl1(new BigDecimal("850"));

        String error = invokeValidateMethodRules(dto, "DGNL");
        Assert.assertNull("TC-IMP-06: DGNL hợp lệ không được báo lỗi", error);
    }

    /** TC-IMP-07: DGNL thiếu điểm NL1 → phải báo lỗi */
    @Test
    public void testTC_IMP_07_DGNLThieuNL1() throws Exception {
        DiemThiImportDTO dto = new DiemThiImportDTO();
        dto.setTi(new BigDecimal("8.0"));

        String error = invokeValidateMethodRules(dto, "DGNL");
        Assert.assertNotNull("TC-IMP-07: DGNL không có NL1 phải báo lỗi", error);
        Assert.assertTrue("TC-IMP-07: Thông báo phải đề cập NL1", error.contains("NL1"));
    }

    /** TC-IMP-07B: DGNL DTO rỗng hoàn toàn → phải báo lỗi */
    @Test
    public void testTC_IMP_07B_DGNLDTORong() throws Exception {
        DiemThiImportDTO dto = new DiemThiImportDTO();

        String error = invokeValidateMethodRules(dto, "DGNL");
        Assert.assertNotNull("TC-IMP-07B: DGNL không môn nào phải báo lỗi", error);
    }

    // ===============================================================
    // VSAT
    // ===============================================================

    /** TC-IMP-08: VSAT có đủ NK1 và NK2 → không lỗi */
    @Test
    public void testTC_IMP_08_VSATHopLe() throws Exception {
        DiemThiImportDTO dto = new DiemThiImportDTO();
        dto.setNk1(new BigDecimal("8.0"));
        dto.setNk2(new BigDecimal("9.0"));

        String error = invokeValidateMethodRules(dto, "VSAT");
        Assert.assertNull("TC-IMP-08: VSAT hợp lệ không được báo lỗi", error);
    }

    /** TC-IMP-08B: VSAT chỉ có NK1, thiếu NK2 → phải báo lỗi */
    @Test
    public void testTC_IMP_08B_VSATThieuNK2() throws Exception {
        DiemThiImportDTO dto = new DiemThiImportDTO();
        dto.setNk1(new BigDecimal("8.0"));

        String error = invokeValidateMethodRules(dto, "VSAT");
        Assert.assertNotNull("TC-IMP-08B: VSAT thiếu NK2 phải báo lỗi", error);
        Assert.assertTrue("TC-IMP-08B: Thông báo phải đề cập Năng khiếu", error.contains("Năng khiếu"));
    }

    /** TC-IMP-08C: VSAT không có cả NK1 lẫn NK2 → phải báo lỗi */
    @Test
    public void testTC_IMP_08C_VSATKhongCoNangKhieu() throws Exception {
        DiemThiImportDTO dto = new DiemThiImportDTO();

        String error = invokeValidateMethodRules(dto, "VSAT");
        Assert.assertNotNull("TC-IMP-08C: VSAT không có NK1/NK2 phải báo lỗi", error);
    }

    // ===============================================================
    // Method null
    // ===============================================================

    /** TC-IMP-16: method = null → không lỗi (bỏ qua validation) */
    @Test
    public void testTC_IMP_16_MethodNull() throws Exception {
        DiemThiImportDTO dto = new DiemThiImportDTO();
        dto.setTo(new BigDecimal("7.0"));

        String error = invokeValidateMethodRules(dto, null);
        Assert.assertNull("TC-IMP-16: Method null không được báo lỗi", error);
    }

    // ===============================================================
    // validateScoreRange – khoảng điểm
    // ===============================================================

    /** TC-IMP-09: Điểm vượt giới hạn (Toán = 11, NL1 = 1300) → báo lỗi */
    @Test
    public void testTC_IMP_09_SaiKhoangDiem() throws Exception {
        DiemThiImportDTO dto1 = new DiemThiImportDTO();
        dto1.setTo(new BigDecimal("11"));
        String error1 = invokeValidateScoreRange(dto1);
        Assert.assertNotNull("TC-IMP-09: Điểm Toán = 11 phải báo lỗi", error1);

        DiemThiImportDTO dto2 = new DiemThiImportDTO();
        dto2.setNl1(new BigDecimal("1300"));
        String error2 = invokeValidateScoreRange(dto2);
        Assert.assertNotNull("TC-IMP-09: Điểm NL1 = 1300 phải báo lỗi", error2);
    }

    /** TC-IMP-10: Điểm âm (Toán = -1) → phải báo lỗi */
    @Test
    public void testTC_IMP_10_DiemAm() throws Exception {
        DiemThiImportDTO dto = new DiemThiImportDTO();
        dto.setTo(new BigDecimal("-1"));

        String error = invokeValidateScoreRange(dto);
        Assert.assertNotNull("TC-IMP-10: Điểm âm phải báo lỗi", error);
    }

    /** TC-IMP-11: Biên dưới (Toán = 0) → hợp lệ */
    @Test
    public void testTC_IMP_11_DiemBienDuoi() throws Exception {
        DiemThiImportDTO dto = new DiemThiImportDTO();
        dto.setTo(new BigDecimal("0"));

        String error = invokeValidateScoreRange(dto);
        Assert.assertNull("TC-IMP-11: Điểm Toán = 0 (biên dưới) hợp lệ", error);
    }

    /** TC-IMP-12: Biên trên thông thường (Toán = 10) → hợp lệ */
    @Test
    public void testTC_IMP_12_DiemBienTren() throws Exception {
        DiemThiImportDTO dto = new DiemThiImportDTO();
        dto.setTo(new BigDecimal("10"));

        String error = invokeValidateScoreRange(dto);
        Assert.assertNull("TC-IMP-12: Điểm Toán = 10 (biên trên) hợp lệ", error);
    }

    /** TC-IMP-13: NL1 biên trên (1200) → hợp lệ */
    @Test
    public void testTC_IMP_13_NL1BienTren() throws Exception {
        DiemThiImportDTO dto = new DiemThiImportDTO();
        dto.setNl1(new BigDecimal("1200"));

        String error = invokeValidateScoreRange(dto);
        Assert.assertNull("TC-IMP-13: Điểm NL1 = 1200 (biên trên) hợp lệ", error);
    }

    /** TC-IMP-14: NL1 biên dưới (0) → hợp lệ */
    @Test
    public void testTC_IMP_14_NL1BienDuoi() throws Exception {
        DiemThiImportDTO dto = new DiemThiImportDTO();
        dto.setNl1(new BigDecimal("0"));

        String error = invokeValidateScoreRange(dto);
        Assert.assertNull("TC-IMP-14: Điểm NL1 = 0 (biên dưới) hợp lệ", error);
    }

    /** TC-IMP-15: Tất cả điểm null → không lỗi khoảng */
    @Test
    public void testTC_IMP_15_TatCaNull() throws Exception {
        DiemThiImportDTO dto = new DiemThiImportDTO();

        String error = invokeValidateScoreRange(dto);
        Assert.assertNull("TC-IMP-15: DTO rỗng (tất cả null) không báo lỗi khoảng điểm", error);
    }
}
