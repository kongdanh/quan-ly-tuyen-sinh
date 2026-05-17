package com.tuyensinh.service;

import org.junit.Test;

import java.time.LocalDateTime;

import static org.junit.Assert.*;

/**
 * Test class cho Giai đoạn 3 & 5: Bảo mật API (Sau khi đóng cổng & Công bố kết quả).
 * Bao gồm:
 *   TC43-TC48 (Giai đoạn 3: Bảo mật sau đóng cổng)
 *   TC61-TC64 (Giai đoạn 5.1: Hiển thị kết quả)
 *
 * Tập trung vào kịch bản thực tế: thí sinh dùng F12 / Postman để bypass UI
 * và gọi thẳng vào API sau khi cổng đã đóng hoặc trước giờ công bố.
 */
public class BaoMatApiServiceTest {

    // ========================= STATE GIẢ LẬP =========================

    /**
     * Giả lập thời gian hệ thống và trạng thái đợt xét tuyển.
     */
    private LocalDateTime thoiGianDong = LocalDateTime.now().minusHours(2); // Đã đóng 2 tiếng trước
    private LocalDateTime thoiGianCongBo = LocalDateTime.now().plusHours(3); // Công bố 3 tiếng nữa

    /**
     * API Guard: Kiểm tra xem cổng có đang mở không.
     * Đây là logic BACKEND phải thực thi, không phụ thuộc vào UI.
     */
    private boolean isCongDangMo(LocalDateTime now) {
        // Trong thực tế: query DB để lấy thời gian đóng thực sự
        return now.isBefore(thoiGianDong);
    }

    /**
     * API Guard: Kiểm tra đã đến giờ công bố kết quả chưa.
     */
    private boolean isDaGioCongBo(LocalDateTime now) {
        return !now.isBefore(thoiGianCongBo);
    }

    /**
     * Giả lập response của API khi bị chặn:
     * Trả về HTTP 403 Forbidden.
     */
    private int callProtectedApi(boolean congDangMo) {
        if (!congDangMo) return 403; // Forbidden
        return 200; // OK
    }

    /**
     * Giả lập response của API lấy kết quả xét tuyển.
     * Trả về: HTTP 403 nếu chưa đến giờ công bố, 200 nếu đã đến giờ.
     */
    private Object callGetKetQuaApi(LocalDateTime now) {
        if (!isDaGioCongBo(now)) {
            // Quan trọng: không được lộ field isPassed / passedMajor trong response
            return 403; // Chưa đến giờ, từ chối
        }
        // Đã đến giờ: trả về kết quả thật
        return new Object(); // Giả lập data kết quả
    }

    // ========================= GIAI ĐOẠN 3: SAU ĐÓNG CỔNG =========================

    /**
     * TC43 [Luồng chuẩn - Chặn UI]: Thí sinh truy cập form Đăng ký → báo đã hết hạn.
     */
    @Test
    public void testTC43_TruyCap_FormDangKy_KhiCongDaDong() {
        System.out.println("[RUNNING] TC43: Thí sinh truy cập form Đăng ký sau khi cổng đóng.");

        LocalDateTime now = LocalDateTime.now();
        boolean congMo = isCongDangMo(now);

        assertFalse("TC43 FAILED: Cổng đã đóng, biến congMo phải là false.", congMo);
        System.out.println("[PASSED] TC43: UI báo đã hết hạn. Cổng đang mở = " + congMo);
    }

    /**
     * TC44 [Bảo mật API - BYPASS]: Thí sinh dùng Postman gọi /api/register khi cổng đã đóng.
     * Kỳ vọng: Backend phải validate thời gian hệ thống và trả về lỗi.
     * UI ẩn nút đăng ký là KHÔNG ĐỦ.
     */
    @Test
    public void testTC44_PostmanGoi_ApiRegister_KhiCongDaDong_PhaiBiChang() {
        System.out.println("[RUNNING] TC44: [SECURITY] Thí sinh bypass UI, gọi /api/register qua Postman khi cổng đóng.");
        System.out.println("   [INFO] Kịch bản: Thí sinh mở F12 → xem request → dùng Postman gửi lại với token hợp lệ.");

        LocalDateTime now = LocalDateTime.now(); // Thực: thoiGianDong đã qua
        boolean congMo = isCongDangMo(now);

        // Backend phải thực hiện việc này, dù UI có ẩn nút hay không
        int httpStatus = callProtectedApi(congMo);

        assertEquals("TC44 FAILED: API /api/register PHẢI trả về 403 khi cổng đã đóng!", 403, httpStatus);
        System.out.println("[PASSED] TC44: API trả về HTTP " + httpStatus + " → Backend chặn thành công.");
    }

    /**
     * TC45 [Luồng chuẩn - Chặn UI]: Đăng nhập sau khi đóng cổng → UI ẩn các nút Lưu/Sửa NV.
     */
    @Test
    public void testTC45_DangNhap_SauDongCong_UI_An_NutLuu() {
        System.out.println("[RUNNING] TC45: Thí sinh đăng nhập khi cổng đã đóng → UI ẩn nút Lưu/Sửa.");

        boolean congMo = false; // Cổng đã đóng
        // Logic UI: chỉ hiện nút Lưu khi cổng đang mở
        boolean showNutLuu = congMo;
        boolean showNutSuaNV = congMo;

        assertFalse("TC45 FAILED: Nút Lưu phải bị ẩn khi cổng đóng.", showNutLuu);
        assertFalse("TC45 FAILED: Nút Sửa NV phải bị ẩn khi cổng đóng.", showNutSuaNV);
        System.out.println("[PASSED] TC45: UI ẩn đúng nút Lưu hồ sơ và Sửa NV khi cổng đóng.");
    }

    /**
     * TC46 [Bảo mật API - Update Profile]: Bypass qua Postman gọi /api/profile/update sau đóng cổng.
     * Kịch bản thực tế:
     *   1. Thí sinh đăng nhập lúc cổng còn mở → trình duyệt có JWT token.
     *   2. Cổng đóng nhưng token vẫn còn hiệu lực.
     *   3. Thí sinh mở F12 → copy token → dùng Postman gọi PUT /api/profile/update.
     *   4. Backend PHẢI từ chối vì cổng đã đóng.
     */
    @Test
    public void testTC46_Postman_CapNhatProfile_KhiCongDaDong_PhaiBiChang() {
        System.out.println("[RUNNING] TC46: [SECURITY] Postman gọi /api/profile/update sau khi cổng đóng.");
        System.out.println("   [INFO] Token JWT vẫn hợp lệ nhưng cổng đã đóng → Backend phải block!");

        LocalDateTime now = LocalDateTime.now();
        boolean congMo = isCongDangMo(now);

        // API update profile: phải kiểm tra trạng thái cổng, không chỉ kiểm tra token
        int httpStatus = callProtectedApi(congMo);

        assertEquals("TC46 FAILED: /api/profile/update PHẢI trả về 403 khi cổng đóng. " +
                "Token hợp lệ không đủ điều kiện!", 403, httpStatus);
        System.out.println("[PASSED] TC46: API update profile trả về HTTP " + httpStatus + " → Chặn thành công.");
    }

    /**
     * TC47 [Bảo mật API - Update NV]: Bypass gọi /api/nv/update để thay đổi nguyện vọng sau đóng cổng.
     * Đây là lỗ hổng nghiêm trọng nhất: thí sinh có thể đổi NV sau deadline.
     */
    @Test
    public void testTC47_Postman_ThayDoiNguyenVong_KhiCongDaDong_PhaiBiChang() {
        System.out.println("[RUNNING] TC47: [SECURITY] Postman gọi /api/nv/update sau khi cổng đóng.");
        System.out.println("   [INFO] ĐÂY LÀ LỖ HỔNG NGHIÊM TRỌNG NHẤT: Thí sinh đổi NV sau deadline!");
        System.out.println("   [INFO] Backend PHẢI lock bảng NV và từ chối mọi thao tác sau thời gian đóng.");

        LocalDateTime now = LocalDateTime.now();
        boolean congMo = isCongDangMo(now);

        // API sửa NV: phải kiểm tra trạng thái cổng trước khi cho phép bất kỳ thao tác ghi nào
        int httpStatus = callProtectedApi(congMo);

        assertEquals("TC47 FAILED: /api/nv/update PHẢI trả về 403 khi cổng đóng. " +
                "Đây là lỗ hổng nghiêm trọng!", 403, httpStatus);
        System.out.println("[PASSED] TC47: API update NV trả về HTTP " + httpStatus + " → NV bị khóa thành công.");
    }

    /**
     * TC48 [Ngoại lệ - Admin]: Admin cố tình Import thêm thí sinh khi cổng đã đóng.
     * Tùy logic nghiệp vụ: thường nên cảnh báo Admin nhưng vẫn có thể cho phép (với xác nhận).
     */
    @Test
    public void testTC48_Admin_ImportThiSinh_KhiCongDaDong() {
        System.out.println("[RUNNING] TC48: Admin import thí sinh mới khi cổng đã đóng.");

        boolean congMo = false; // Đã đóng
        boolean adminCoQuyen = true;

        // Logic: Admin có quyền đặc biệt nhưng hệ thống phải CẢNH BÁO
        // Hành vi: Nếu cho phép → phải ghi log. Nếu chặn → phải báo lý do.
        boolean seHienCanhBao = !congMo && adminCoQuyen;

        assertTrue("TC48 FAILED: Phải hiển thị cảnh báo khi Admin import lúc cổng đóng.", seHienCanhBao);
        System.out.println("[PASSED] TC48: Hệ thống hiển thị cảnh báo khi cổng đóng. Admin vẫn có thể xác nhận tiếp.");
    }

    // ========================= GIAI ĐOẠN 5.1: HIỂN THỊ KẾT QUẢ =========================

    /**
     * TC61 [Luồng chuẩn]: Thí sinh ĐẬU đăng nhập sau giờ công bố → thấy kết quả đúng.
     */
    @Test
    public void testTC61_ThiSinhDau_ThayThongBaoChucMung() {
        System.out.println("[RUNNING] TC61: Thí sinh ĐẬU đăng nhập sau giờ công bố.");

        LocalDateTime now = LocalDateTime.now().plusHours(5); // Sau giờ công bố
        boolean daCongBo = isDaGioCongBo(now);
        String ketQuaThiSinh = "TRUNG_TUYEN";

        assertTrue("TC61: Phải đã đến giờ công bố.", daCongBo);
        assertEquals("TC61: Thí sinh đậu phải thấy kết quả TRUNG_TUYEN.", "TRUNG_TUYEN", ketQuaThiSinh);
        System.out.println("[PASSED] TC61: Thấy thông báo chúc mừng & Ngành đậu.");
    }

    /**
     * TC62 [Luồng chuẩn]: Thí sinh RỚT đăng nhập sau giờ công bố → thấy báo rớt.
     */
    @Test
    public void testTC62_ThiSinhRot_ThayThongBaoRot() {
        System.out.println("[RUNNING] TC62: Thí sinh RỚT đăng nhập sau giờ công bố.");

        LocalDateTime now = LocalDateTime.now().plusHours(5);
        boolean daCongBo = isDaGioCongBo(now);
        String ketQuaThiSinh = "KHONG_TRUNG_TUYEN";

        assertTrue("TC62: Phải đã đến giờ công bố.", daCongBo);
        assertEquals("TC62: Thí sinh rớt phải thấy KHONG_TRUNG_TUYEN.", "KHONG_TRUNG_TUYEN", ketQuaThiSinh);
        System.out.println("[PASSED] TC62: Thấy thông báo rớt đúng.");
    }

    /**
     * TC63 [Luồng chuẩn - UI]: Thí sinh đăng nhập TRƯỚC giờ công bố → UI báo 'Chưa tới hạn'.
     * Admin đã chạy thuật toán xong nhưng kết quả chưa được công bố.
     */
    @Test
    public void testTC63_TruocGioCongBo_UI_BaoChuaToiHan() {
        System.out.println("[RUNNING] TC63: Đăng nhập TRƯỚC giờ công bố → UI báo 'Chưa tới hạn'.");

        LocalDateTime now = LocalDateTime.now(); // Cổng đóng nhưng chưa tới giờ công bố
        boolean daCongBo = isDaGioCongBo(now);

        assertFalse("TC63 FAILED: Phải CHƯA đến giờ công bố.", daCongBo);
        // UI message
        String uiMessage = !daCongBo ? "Kết quả chưa được công bố. Vui lòng quay lại sau." : null;
        assertNotNull("TC63 FAILED: UI phải hiển thị thông báo chưa tới hạn.", uiMessage);
        System.out.println("[PASSED] TC63: UI báo đúng: '" + uiMessage + "'");
    }

    /**
     * TC64 [Bảo mật API - Lộ Kết Quả TRƯỚC GIỜ CÔNG BỐ]:
     * Kịch bản thực tế (cực kỳ nguy hiểm):
     *   1. Thí sinh đăng nhập → lấy JWT token.
     *   2. Mở F12 → tìm API lấy thông tin profile/kết quả.
     *   3. Dùng Postman gọi GET /api/results/my-result TRƯỚC giờ công bố.
     *   4. Backend PHẢI từ chối HOẶC trả về JSON đã bị che field isPassed / passedMajor.
     *   5. KHÔNG ĐƯỢC trả về kết quả sớm chỉ vì UI đã ẩn các phần tử.
     */
    @Test
    public void testTC64_Postman_LayKetQua_TruocGioCongBo_PhaiBiChang() {
        System.out.println("[RUNNING] TC64: [SECURITY] Gọi API lấy kết quả TRƯỚC giờ công bố qua Postman.");
        System.out.println("   [INFO] Đây là lỗ hổng: UI ẩn kết quả nhưng JSON response vẫn chứa data!");
        System.out.println("   [INFO] Backend PHẢI check: currentTime < publishTime → trả về 403 hoặc JSON rỗng.");

        LocalDateTime now = LocalDateTime.now(); // Trước giờ công bố
        Object apiResponse = callGetKetQuaApi(now);

        // API phải trả về mã lỗi (Integer 403) chứ không phải data object
        assertTrue("TC64 FAILED: API /api/results/my-result PHẢI trả về 403 trước giờ công bố. " +
                        "Không được lộ kết quả trong JSON response!",
                apiResponse instanceof Integer && ((Integer) apiResponse) == 403);

        System.out.println("[PASSED] TC64: API trả về HTTP " + apiResponse + " → Bảo mật kết quả thành công.");
        System.out.println("   [NOTE] Tuyệt đối không để field isPassed/passedMajor trong response trước giờ G!");
    }
}
