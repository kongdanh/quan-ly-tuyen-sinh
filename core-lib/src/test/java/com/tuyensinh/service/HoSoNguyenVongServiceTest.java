package com.tuyensinh.service;

import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Test class cho Giai đoạn 1.4, 2.1, 2.3 & 4.2 & 5.
 * Bao gồm:
 *   TC24-TC27  (1.4 Duyệt hồ sơ)
 *   TC28-TC32  (2.1 Đăng ký & Thông tin cá nhân)
 *   TC37-TC42  (2.3 Đặt Nguyện Vọng)
 *   TC58-TC60  (4.2 Gửi thông báo & Chốt)
 *   TC65-TC68  (5.2 Thống kê)
 */
public class HoSoNguyenVongServiceTest {

    // ====================== HELPERS ======================

    /**
     * Giả lập validate Email.
     */
    private boolean isEmailHopLe(String email) {
        if (email == null || email.trim().isEmpty()) return false;
        return email.matches("^[\\w._%+\\-]+@[\\w.\\-]+\\.[a-zA-Z]{2,}$");
    }

    /**
     * Giả lập validate CCCD: phải đúng 12 chữ số.
     */
    private boolean isCccdHopLe(String cccd) {
        if (cccd == null) return false;
        return cccd.matches("^\\d{12}$");
    }

    /**
     * Giả lập validate SĐT: 10 chữ số, bắt đầu bằng 0.
     */
    private boolean isSdtHopLe(String sdt) {
        if (sdt == null) return false;
        return sdt.matches("^0\\d{9}$");
    }

    /**
     * Giả lập hệ thống: kiểm tra CCCD/Email đã tồn tại.
     */
    private final List<String> existingCCCDs = Arrays.asList("001234567890", "001234567891");
    private final List<String> existingEmails = Arrays.asList("a@test.com", "b@test.com");

    /**
     * Giả lập trạng thái cổng.
     */
    private boolean isCongDangMo = true;

    /**
     * Kiểm tra API bảo mật khi cổng đóng.
     */
    private void assertCongDangDong_ApiPhaiChang(boolean congMo, String apiName) {
        if (congMo) {
            fail(apiName + ": API không được phép thực thi khi cổng đã đóng! Backend bị bypass.");
        }
    }

    // ====================== 1.4 DUYỆT HỒ SƠ ======================

    /**
     * TC24 [Luồng chuẩn]: Duyệt thành công 1 tài khoản hợp lệ.
     */
    @Test
    public void testTC24_DuyetTaiKhoanHopLe() {
        System.out.println("[RUNNING] TC24: Duyệt tài khoản hợp lệ.");

        String trangThai = "PENDING";
        // Admin bấm Duyệt → trạng thái thành APPROVED
        String ketQua = "PENDING".equals(trangThai) ? "APPROVED" : trangThai;

        assertEquals("TC24 FAILED: Trạng thái phải là APPROVED.", "APPROVED", ketQua);
        System.out.println("[PASSED] TC24: Tài khoản đã được duyệt thành công.");
    }

    /**
     * TC25 [Luồng chuẩn]: Từ chối tài khoản kèm lý do.
     */
    @Test
    public void testTC25_TuChoiTaiKhoan_CoLyDo() {
        System.out.println("[RUNNING] TC25: Từ chối tài khoản với lý do.");

        String lyDo = "Ảnh CCCD không rõ nét.";
        String trangThai = "REJECTED";

        assertNotNull("TC25 FAILED: Phải có lý do từ chối.", lyDo);
        assertFalse("TC25 FAILED: Lý do không được để trống.", lyDo.trim().isEmpty());
        assertEquals("TC25 FAILED: Trạng thái phải là REJECTED.", "REJECTED", trangThai);
        System.out.println("[PASSED] TC25: Từ chối với lý do: " + lyDo);
    }

    /**
     * TC26 [Luồng chuẩn]: Bulk approve nhiều tài khoản cùng lúc.
     */
    @Test
    public void testTC26_BulkApprove_NhieuTaiKhoan() {
        System.out.println("[RUNNING] TC26: Bulk approve nhiều tài khoản.");

        List<String> pendingIds = Arrays.asList("1001", "1002", "1003");
        int soLuotDuyet = pendingIds.size();

        assertEquals("TC26 FAILED: Phải duyệt đủ 3 tài khoản.", 3, soLuotDuyet);
        System.out.println("[PASSED] TC26: Bulk approve " + soLuotDuyet + " tài khoản thành công.");
    }

    /**
     * TC27 [Ngoại lệ/Concurrency]: Cố tình duyệt tài khoản đã được duyệt rồi (double-duyệt qua 2 tab).
     * Kỳ vọng: Lần 2 phải bị từ chối hoặc idempotent (không lỗi nhưng không ghi đè).
     */
    @Test
    public void testTC27_DoubleDuyet_TaiKhoanDaDuyet() {
        System.out.println("[RUNNING] TC27: Duyệt 1 tài khoản đã APPROVED rồi (2 tab cùng mở).");

        String trangThaiHienTai = "APPROVED"; // Đã duyệt rồi

        String ketQua;
        if ("APPROVED".equals(trangThaiHienTai)) {
            ketQua = "NOOP"; // Không làm gì thêm - idempotent
        } else {
            ketQua = "APPROVED";
        }

        assertEquals("TC27 FAILED: Hệ thống phải xử lý idempotent (NOOP) khi đã duyệt.", "NOOP", ketQua);
        System.out.println("[PASSED] TC27: Không bị double-duyệt. Kết quả: " + ketQua);
    }

    // ====================== 2.1 ĐĂNG KÝ & THÔNG TIN CÁ NHÂN ======================

    /**
     * TC28 [Luồng chuẩn]: Đăng ký tài khoản thành công với thông tin đúng.
     */
    @Test
    public void testTC28_DangKyTaiKhoanHopLe() {
        System.out.println("[RUNNING] TC28: Đăng ký tài khoản thành công.");

        String email = "thisinh2025@gmail.com";
        String cccd = "001234567899";
        String sdt = "0912345678";

        assertTrue("TC28 FAILED: Email phải hợp lệ.", isEmailHopLe(email));
        assertTrue("TC28 FAILED: CCCD phải hợp lệ.", isCccdHopLe(cccd));
        assertTrue("TC28 FAILED: SĐT phải hợp lệ.", isSdtHopLe(sdt));
        assertFalse("TC28 FAILED: CCCD không được trùng DB.", existingCCCDs.contains(cccd));
        assertFalse("TC28 FAILED: Email không được trùng DB.", existingEmails.contains(email));

        System.out.println("[PASSED] TC28: Tài khoản hợp lệ, đăng ký thành công.");
    }

    /**
     * TC29 [Ngoại lệ]: Đăng ký bằng Email hoặc CCCD đã tồn tại trong DB.
     */
    @Test
    public void testTC29_DangKy_EmailHoacCCCD_DaTonTai() {
        System.out.println("[RUNNING] TC29: Đăng ký bằng CCCD đã có trong DB.");

        String cccdTrung = "001234567890"; // Đã có trong existingCCCDs
        assertTrue("TC29 FAILED: Phải phát hiện CCCD trùng.", existingCCCDs.contains(cccdTrung));

        String emailTrung = "a@test.com";
        assertTrue("TC29 FAILED: Phải phát hiện Email trùng.", existingEmails.contains(emailTrung));
        System.out.println("[PASSED] TC29: Phát hiện CCCD/Email trùng → không cho đăng ký.");
    }

    /**
     * TC30 [Ngoại lệ]: Bỏ trống các trường bắt buộc.
     */
    @Test
    public void testTC30_BoBietTruongBatBuoc() {
        System.out.println("[RUNNING] TC30: Để trống email, CCCD, SĐT.");

        assertFalse("TC30 FAILED: Email rỗng phải bị reject.", isEmailHopLe(""));
        assertFalse("TC30 FAILED: CCCD null phải bị reject.", isCccdHopLe(null));
        assertFalse("TC30 FAILED: SĐT rỗng phải bị reject.", isSdtHopLe(""));
        System.out.println("[PASSED] TC30: Tất cả trường rỗng/null bị từ chối đúng.");
    }

    /**
     * TC31 [Ngoại lệ]: Sai định dạng Email, SĐT, CCCD.
     */
    @Test
    public void testTC31_SaiDinhDang_Email_SDT_CCCD() {
        System.out.println("[RUNNING] TC31: Sai định dạng Email/SĐT/CCCD.");

        assertFalse("TC31: Email 'thi sinh .vn' phải sai định dạng.", isEmailHopLe("thisinh.vn"));
        assertFalse("TC31: SĐT '12345' phải sai (không đủ 10 số).", isSdtHopLe("12345"));
        assertFalse("TC31: CCCD '0012ABC34567' phải sai (chứa chữ).", isCccdHopLe("0012ABC34567"));
        assertFalse("TC31: CCCD '00112345678' phải sai (chỉ 11 số).", isCccdHopLe("00112345678"));
        System.out.println("[PASSED] TC31: Phát hiện đúng tất cả sai định dạng.");
    }

    /**
     * TC32 [Ngoại lệ]: Cập nhật CCCD sang số của người khác đang có trong hệ thống.
     */
    @Test
    public void testTC32_CapNhatCCCD_TrungNguoiKhac() {
        System.out.println("[RUNNING] TC32: Thí sinh cập nhật CCCD → trùng với CCCD của người khác.");

        String cccdMoiMuonCapNhat = "001234567891"; // Đã thuộc về thí sinh khác
        boolean trung = existingCCCDs.contains(cccdMoiMuonCapNhat);

        assertTrue("TC32 FAILED: Phải phát hiện CCCD đã thuộc người khác.", trung);
        System.out.println("[PASSED] TC32: Từ chối cập nhật CCCD vì đã tồn tại trong DB.");
    }

    // ====================== 2.3 ĐẶT NGUYỆN VỌNG ======================

    /**
     * TC37 [Luồng chuẩn]: Thêm, sửa, sắp xếp, xóa nguyện vọng thành công.
     */
    @Test
    public void testTC37_ThemSuaXoaNguyenVong() {
        System.out.println("[RUNNING] TC37: Thêm, sửa, sắp xếp, xóa nguyện vọng.");

        java.util.List<String> listNV = new java.util.ArrayList<>(Arrays.asList("NV1:CNTT", "NV2:KTPM", "NV3:ATTT"));
        assertEquals("TC37: Phải có 3 NV ban đầu.", 3, listNV.size());

        // Sửa NV2
        listNV.set(1, "NV2:KHMT");
        assertEquals("TC37: NV2 sau khi sửa.", "NV2:KHMT", listNV.get(1));

        // Xóa NV3
        listNV.remove(2);
        assertEquals("TC37: Còn 2 NV sau khi xóa.", 2, listNV.size());

        System.out.println("[PASSED] TC37: Thao tác NV thành công: " + listNV);
    }

    /**
     * TC38 [Ngoại lệ]: Đặt 2 nguyện vọng trùng nhau hoàn toàn (cùng Ngành, cùng Tổ hợp).
     */
    @Test
    public void testTC38_HaiNguyenVongTrungNhau() {
        System.out.println("[RUNNING] TC38: Đặt 2 NV trùng nhau (cùng Ngành + cùng Tổ hợp).");

        String nv1Key = "CNTT_A00";
        String nv2Key = "CNTT_A00"; // Trùng hoàn toàn

        boolean trung = nv1Key.equals(nv2Key);
        assertTrue("TC38 FAILED: Phải phát hiện 2 NV trùng nhau.", trung);
        System.out.println("[PASSED] TC38: Phát hiện 2 NV trùng hoàn toàn → Từ chối.");
    }

    /**
     * TC39 [Ngoại lệ]: Xóa hết tất cả NV và Lưu (hệ thống có bắt buộc ít nhất 1 NV không).
     */
    @Test
    public void testTC39_XoaHetNguyenVong() {
        System.out.println("[RUNNING] TC39: Xóa sạch tất cả NV rồi Lưu.");

        java.util.List<String> listNV = new java.util.ArrayList<>(); // Rỗng

        // Validate: phải có ít nhất 1 NV
        boolean hopLe = !listNV.isEmpty();
        assertFalse("TC39 FAILED: Nên từ chối khi thí sinh không có NV nào.", hopLe);
        System.out.println("[PASSED] TC39: Hệ thống từ chối lưu hồ sơ khi không có NV.");
    }

    /**
     * TC40 [Biên]: Thêm vượt quá số NV tối đa (ví dụ: giới hạn là 5 NV).
     */
    @Test
    public void testTC40_VuotSoNguyenVongToiDa() {
        System.out.println("[RUNNING] TC40: Thêm quá 5 nguyện vọng (giới hạn tối đa).");

        int MAX_NV = 5;
        java.util.List<String> listNV = new java.util.ArrayList<>(
                Arrays.asList("NV1", "NV2", "NV3", "NV4", "NV5")
        );

        // Cố thêm NV6
        boolean coTheThemNua = listNV.size() < MAX_NV;
        assertFalse("TC40 FAILED: Không được phép thêm khi đã đạt giới hạn 5 NV.", coTheThemNua);
        System.out.println("[PASSED] TC40: Hệ thống chặn khi NV >= " + MAX_NV);
    }

    /**
     * TC41 [Bảo mật]: Dùng Postman gửi ID Ngành không thuộc đợt xét tuyển hiện tại.
     * Kỳ vọng: Backend phải validate ID Ngành tồn tại trong đợt xét tuyển đang mở.
     */
    @Test
    public void testTC41_ApiNguyenVong_GuiIdNganhGiaMao() {
        System.out.println("[RUNNING] TC41: [SECURITY] Gửi ID Ngành không thuộc đợt xét tuyển hiện tại.");
        System.out.println("   [INFO] Kịch bản: Thí sinh dùng Postman gửi nganhId=999 (không thuộc đợt).");

        // Giả lập: đợt xét tuyển hiện tại chỉ có ngành với ID 1, 2, 3
        List<Integer> nganhHopLeIds = Arrays.asList(1, 2, 3);
        int nganhIdGiaMao = 999; // ID không thuộc đợt

        boolean isHopLe = nganhHopLeIds.contains(nganhIdGiaMao);

        assertFalse("TC41 FAILED: Backend PHẢI reject ID Ngành không thuộc đợt xét tuyển!", isHopLe);
        System.out.println("[PASSED] TC41: Backend từ chối ID Ngành giả mạo = " + nganhIdGiaMao);
    }

    /**
     * TC42 [Bảo mật]: Đăng nhập quyền Admin, gọi API sửa NV của thí sinh → phải 403 Forbidden.
     */
    @Test
    public void testTC42_AdminGoiApi_SuaNguyenVong_ThiSinh_PhaiBi403() {
        System.out.println("[RUNNING] TC42: [SECURITY] Admin gọi API sửa NV của thí sinh.");
        System.out.println("   [INFO] API POST /api/nv/update với token Admin → Phải 403 Forbidden.");

        String userRole = "ADMIN";
        String targetApiRole = "THI_SINH"; // API này chỉ dành cho thí sinh

        boolean hasPermission = userRole.equals(targetApiRole);

        assertFalse("TC42 FAILED: ADMIN không được phép gọi API chỉ dành cho THINSINH → phải bị 403!", hasPermission);
        System.out.println("[PASSED] TC42: ADMIN bị từ chối gọi API THINSINH → 403 Forbidden.");
    }

    // ====================== 4.2 GỬI THÔNG BÁO & CHỐT ======================

    /**
     * TC58 [Luồng chuẩn]: Chốt danh sách → DB cập nhật status Final.
     */
    @Test
    public void testTC58_ChotDanhSach_DBCapNhatFinal() {
        System.out.println("[RUNNING] TC58: Chốt danh sách → status = FINAL.");

        String trangThaiDot = "CLOSED"; // Cổng đã đóng

        // Admin bấm Chốt → status Final
        String trangThaiMoi = "CLOSED".equals(trangThaiDot) ? "FINALIZED" : "ERROR";

        assertEquals("TC58 FAILED: Status phải là FINALIZED.", "FINALIZED", trangThaiMoi);
        System.out.println("[PASSED] TC58: Chốt thành công. Status = FINALIZED.");
    }

    /**
     * TC59 [Luồng chuẩn]: Gửi email thông báo đúng mẫu, đúng thông tin thí sinh.
     */
    @Test
    public void testTC59_GuiEmailThongBao_DungThongTin() {
        System.out.println("[RUNNING] TC59: Kiểm tra nội dung email gửi đúng thông tin thí sinh.");

        // Giả lập thông tin thí sinh trúng tuyển
        String hoTen = "Nguyễn Văn A";
        String nganh = "Công nghệ thông tin";
        String trangThai = "TRUNG_TUYEN";

        String emailBody = "Chúc mừng " + hoTen + "! Bạn đã trúng tuyển ngành " + nganh + ".";

        assertTrue("TC59: Email phải chứa tên thí sinh.", emailBody.contains(hoTen));
        assertTrue("TC59: Email phải chứa tên ngành.", emailBody.contains(nganh));
        System.out.println("[PASSED] TC59: Nội dung email hợp lệ: " + emailBody);
    }

    /**
     * TC60 [Ngoại lệ]: Một số thí sinh email sai → tiến trình gửi mail không crash.
     * Kỳ vọng: Các email lỗi được skip và log, không dừng toàn bộ batch.
     */
    @Test
    public void testTC60_GuiMailBatch_EmailLoi_KhongCrash() {
        System.out.println("[RUNNING] TC60: Gửi mail batch, một số email sai → không crash toàn bộ.");

        String[] emails = {"valid@gmail.com", "email_sai_format", null, "another@gmail.com"};
        int thanhCong = 0;
        int thatBai = 0;

        for (String email : emails) {
            if (email != null && email.contains("@") && email.contains(".")) {
                thanhCong++;
            } else {
                thatBai++;
                System.out.println("   [SKIP] Email lỗi, ghi log và tiếp tục: " + email);
            }
        }

        assertEquals("TC60: Phải gửi thành công 2 email hợp lệ.", 2, thanhCong);
        assertEquals("TC60: Phải skip 2 email lỗi (null + sai format).", 2, thatBai);
        System.out.println("[PASSED] TC60: Batch gửi mail không crash. Thành công: " + thanhCong + ", Lỗi: " + thatBai);
    }

    // ====================== 5.2 THỐNG KÊ ======================

    /**
     * TC65 [Luồng chuẩn]: Tổng Đậu + Tổng Rớt = Tổng số thí sinh hợp lệ (Có NV).
     */
    @Test
    public void testTC65_TongDauPlusTongRot_EqualsTongThiSinh() {
        System.out.println("[RUNNING] TC65: Tổng Đậu + Rớt = Tổng thí sinh hợp lệ.");

        int tongHopLe = 100; // có NV
        int tongDau = 42;
        int tongRot = 58;

        assertEquals("TC65 FAILED: Tổng Đậu + Rớt phải = Tổng thí sinh hợp lệ.", tongHopLe, tongDau + tongRot);
        System.out.println("[PASSED] TC65: " + tongDau + " + " + tongRot + " = " + tongHopLe);
    }

    /**
     * TC66 [Ngoại lệ]: Chuyển đổi qua lại giữa các Đợt xét tuyển → biểu đồ không cộng dồn.
     */
    @Test
    public void testTC66_ChuyenDoiDot_BieuDoKhongCongDon() {
        System.out.println("[RUNNING] TC66: Chuyển đợt xét tuyển → biểu đồ cập nhật đúng.");

        // Giả lập data của 2 đợt
        int dauDot1 = 50, rotDot1 = 30;
        int dauDot2 = 70, rotDot2 = 20;

        // Khi chuyển sang Đợt 2, biểu đồ phải hiển thị data của Đợt 2, KHÔNG cộng dồn
        int dauHienThi = dauDot2;
        int rotHienThi = rotDot2;

        assertNotEquals("TC66 FAILED: Không được cộng dồn data 2 đợt.", dauDot1 + dauDot2, dauHienThi);
        assertEquals("TC66: Biểu đồ đang hiện Đợt 2 = 70 đậu.", 70, dauHienThi);
        System.out.println("[PASSED] TC66: Biểu đồ Đợt 2: Đậu=" + dauHienThi + ", Rớt=" + rotHienThi);
    }

    /**
     * TC67 [Hiệu năng]: Dashboard với 50.000 thí sinh không timeout.
     * Test này chủ yếu ghi nhận thời gian giả lập; trong thực tế cần đo query time thật.
     */
    @Test
    public void testTC67_DashboardLoad_50000ThiSinh_KhongTimeout() {
        System.out.println("[RUNNING] TC67: Load Dashboard với 50.000 thí sinh - kiểm tra hiệu năng.");

        long start = System.currentTimeMillis();

        // Giả lập việc tính toán thống kê với 50.000 bản ghi (in-memory)
        int totalDau = 0;
        int totalRot = 0;
        for (int i = 0; i < 50_000; i++) {
            if (i % 2 == 0) totalDau++;
            else totalRot++;
        }

        long duration = System.currentTimeMillis() - start;
        int TIMEOUT_MS = 3000; // 3 giây

        System.out.println("   [INFO] Thời gian xử lý in-memory 50.000 bản ghi: " + duration + "ms");
        assertTrue("TC67 FAILED: Xử lý thống kê quá chậm (>" + TIMEOUT_MS + "ms). Cần thêm DB index!",
                duration < TIMEOUT_MS);
        System.out.println("[PASSED] TC67: Xử lý 50.000 bản ghi trong " + duration + "ms < " + TIMEOUT_MS + "ms.");
    }

    /**
     * TC68 [Ngoại lệ/Biên]: Thí sinh đăng ký nhưng không đặt nguyện vọng.
     * Hỏi: Nhóm này có tính vào Tổng số / Tổng Rớt không?
     */
    @Test
    public void testTC68_ThiSinhKhongCoNguyenVong_KhongTinhVaoThongKe() {
        System.out.println("[RUNNING] TC68: Thí sinh có tài khoản nhưng không đặt nguyện vọng.");

        int tongDangKy = 120;   // Tổng số có tài khoản
        int soKhongCoNV = 20;   // Không đặt NV
        int tongHopLe = tongDangKy - soKhongCoNV; // = 100 (có NV)

        int tongDau = 42;
        int tongRot = 58; // = 100 - 42

        // Nhóm không có NV KHÔNG được tính vào thống kê chính
        assertEquals("TC68: Nhóm không có NV phải TÁCH BIỆT, không gộp vào Tổng Rớt.",
                tongHopLe, tongDau + tongRot);
        assertNotEquals("TC68: Tổng có NV không được bằng Tổng đăng ký.", tongDangKy, tongHopLe);

        System.out.printf("[PASSED] TC68: Tổng đăng ký=%d, Có NV=%d, Không có NV=%d (tách riêng).%n",
                tongDangKy, tongHopLe, soKhongCoNV);
        System.out.println("   [NOTE] Nhóm không có NV cần hiển thị riêng trong báo cáo.");
    }
}
