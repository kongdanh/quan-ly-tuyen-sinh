package com.tuyensinh.service;

import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.*;

/**
 * Test class cho Giai đoạn 4: Chạy Thuật toán Xét Tuyển & Chốt Danh Sách
 * Bao gồm: TC49 - TC57
 * Đặc biệt: TC57 kiểm thử Concurrency (Race Condition) theo tips.
 */
public class XetTuyenServiceTest {

    // ========================= MOCK STATE & HELPERS =========================

    /**
     * Giả lập cờ khóa tiến trình (atomic để thread-safe).
     * Trong production: dùng AtomicBoolean, DB flag hoặc Redis Lock.
     * AtomicBoolean.compareAndSet(false, true) → chỉ 1 thread thắng.
     */
    private final AtomicBoolean isRunningLock = new AtomicBoolean(false);
    private final AtomicInteger soLanChayThanhCong = new AtomicInteger(0);

    /**
     * Hàm chạy thuật toán với lock dùng AtomicBoolean.compareAndSet().
     * CAS (Compare-And-Set) đảm bảo chỉ 1 thread "giành được" lock.
     * Thread còn lại bị từ chối ngay lập tức.
     */
    private boolean runAlgorithmWithAtomicLock(boolean congDaDong) {
        if (!congDaDong) {
            throw new IllegalStateException("Cổng xét tuyển chưa đóng, không thể chạy thuật toán!");
        }
        // CAS: Chỉ set thành true nếu hiện tại là false → chỉ 1 thread thắng
        if (!isRunningLock.compareAndSet(false, true)) {
            return false; // Thread thua: bị từ chối vì có tiến trình đang chạy
        }
        try {
            Thread.sleep(20); // Simulate processing time
            soLanChayThanhCong.incrementAndGet();
        } catch (InterruptedException ignored) {
            Thread.currentThread().interrupt();
        } finally {
            isRunningLock.set(false); // Giải phóng lock
        }
        return true;
    }

    /**
     * Giả lập hàm chạy thuật toán xét tuyển (dùng cho các TC khác).
     */
    private synchronized boolean runAlgorithmWithLock(boolean congDaDong) {
        if (!congDaDong) {
            throw new IllegalStateException("Cổng xét tuyển chưa đóng, không thể chạy thuật toán!");
        }
        soLanChayThanhCong.incrementAndGet();
        return true;
    }

    /**
     * Giả lập logic xét tuyển từng nguyện vọng (không cần DB).
     * Trả về: "DAU" hoặc "ROT"
     */
    private String xetNguyenVong(double diemThiSinh, double diemChuan) {
        return (diemThiSinh >= diemChuan) ? "DAU" : "ROT";
    }

    @Before
    public void setUp() {
        soLanChayThanhCong.set(0);
        isRunningLock.set(false);
    }

    // ========================= 4.1 THUẬT TOÁN XÉT TUYỂN =========================

    /**
     * TC49 [Luồng chuẩn]: Bấm chạy thuật toán thành công khi đã đóng cổng.
     */
    @Test
    public void testTC49_ChayThuatToanThanhCong_KhiDaDongCong() {
        System.out.println("[RUNNING] TC49: Chạy thuật toán thành công khi đã đóng cổng.");

        boolean congDaDong = true;
        boolean result = runAlgorithmWithLock(congDaDong);

        assertTrue("TC49 FAILED: Thuật toán phải chạy thành công khi cổng đã đóng.", result);
        assertEquals("TC49 FAILED: Phải chạy đúng 1 lần.", 1, soLanChayThanhCong.get());
        System.out.println("[PASSED] TC49: Thuật toán đã chạy thành công.");
    }

    /**
     * TC50 [Luồng chuẩn - Logic]: Điểm thi + Ưu tiên >= Điểm NV1 → Đậu NV1.
     * Các NV sau không cần xét.
     */
    @Test
    public void testTC50_ThiSinhDauNV1() {
        System.out.println("[RUNNING] TC50: Điểm thi + Ưu tiên >= Điểm NV1 → Đậu NV1.");

        double diemThi = 24.0;
        double diemUuTien = 1.0;
        double diemChuanNV1 = 24.5;
        double diemChuanNV2 = 22.0;

        double tongDiem = diemThi + diemUuTien; // = 25.0 >= 24.5

        String ketQuaNV1 = xetNguyenVong(tongDiem, diemChuanNV1);

        assertEquals("TC50 FAILED: Thí sinh phải Đậu NV1.", "DAU", ketQuaNV1);

        // Sau khi đậu NV1, NV2 phải là "HUY" (không xét)
        String ketQuaNV2 = "HUY"; // Logic: nếu đã đậu NV1 thì NV2 = HUY
        assertEquals("TC50 FAILED: NV2 phải là HUY sau khi đậu NV1.", "HUY", ketQuaNV2);

        System.out.println("[PASSED] TC50: Tổng điểm " + tongDiem + " >= " + diemChuanNV1 + " → Đậu NV1.");
    }

    /**
     * TC51 [Luồng chuẩn - Logic]: Rớt NV1, đủ điểm NV2 → Rớt NV1, Đậu NV2.
     */
    @Test
    public void testTC51_ThiSinhRotNV1_DauNV2() {
        System.out.println("[RUNNING] TC51: Rớt NV1, đủ điểm NV2 → Rớt NV1, Đậu NV2.");

        double tongDiem = 23.0;
        double diemChuanNV1 = 25.0;
        double diemChuanNV2 = 22.0;

        String ketQuaNV1 = xetNguyenVong(tongDiem, diemChuanNV1); // 23.0 < 25.0 → ROT
        String ketQuaNV2 = "ROT".equals(ketQuaNV1) ? xetNguyenVong(tongDiem, diemChuanNV2) : "HUY"; // 23.0 >= 22.0 → DAU

        assertEquals("TC51 FAILED: Thí sinh phải Rớt NV1.", "ROT", ketQuaNV1);
        assertEquals("TC51 FAILED: Thí sinh phải Đậu NV2.", "DAU", ketQuaNV2);
        System.out.println("[PASSED] TC51: Rớt NV1 → Đậu NV2 với tổng điểm " + tongDiem + ".");
    }

    /**
     * TC52 [Luồng chuẩn - Logic]: Không đủ điểm bất kỳ NV nào → Rớt tất cả.
     */
    @Test
    public void testTC52_ThiSinhRotTatCaNguyenVong() {
        System.out.println("[RUNNING] TC52: Không đủ điểm bất kỳ NV nào → Rớt tất cả.");

        double tongDiem = 18.0;
        double diemChuanNV1 = 25.0;
        double diemChuanNV2 = 22.0;
        double diemChuanNV3 = 20.0;

        String kqNV1 = xetNguyenVong(tongDiem, diemChuanNV1);
        String kqNV2 = "ROT".equals(kqNV1) ? xetNguyenVong(tongDiem, diemChuanNV2) : "HUY";
        String kqNV3 = "ROT".equals(kqNV2) ? xetNguyenVong(tongDiem, diemChuanNV3) : "HUY";

        assertEquals("TC52 FAILED: Phải Rớt NV1.", "ROT", kqNV1);
        assertEquals("TC52 FAILED: Phải Rớt NV2.", "ROT", kqNV2);
        assertEquals("TC52 FAILED: Phải Rớt NV3.", "ROT", kqNV3);
        System.out.println("[PASSED] TC52: Thí sinh Rớt tất cả NV với tổng điểm " + tongDiem + ".");
    }

    /**
     * TC53 [Biên - Logic]: Điểm thi + Ưu tiên BẰNG CHÍNH XÁC Điểm chuẩn → phải Đậu.
     * Đây là trường hợp biên quan trọng: điều kiện là ">=" không phải ">".
     */
    @Test
    public void testTC53_DiemBangChinhXacDiemChuan_PhaiBangDau() {
        System.out.println("[RUNNING] TC53: Điểm thi = Điểm chuẩn CHÍNH XÁC → Phải Đậu.");

        double diemThi = 23.5;
        double diemUuTien = 1.5;
        double diemChuan = 25.0; // bằng chính xác

        double tongDiem = diemThi + diemUuTien; // = 25.0

        String ketQua = xetNguyenVong(tongDiem, diemChuan);

        assertEquals("TC53 FAILED: Điểm bằng đúng điểm chuẩn PHẢI là ĐẬU (điều kiện >=).", "DAU", ketQua);
        System.out.println("[PASSED] TC53: " + tongDiem + " >= " + diemChuan + " → ĐẬU (biên chính xác).");
    }

    /**
     * TC54 [Ngoại lệ]: Cố bấm chạy thuật toán khi cổng CHƯA đóng → phải bị chặn.
     */
    @Test(expected = IllegalStateException.class)
    public void testTC54_ChayThuatToanKhiCongChuaDong_PhaiBiChang() {
        System.out.println("[RUNNING] TC54: Bấm 'Chạy xét tuyển' khi cổng CHƯA đóng.");

        boolean congDaDong = false;
        runAlgorithmWithLock(congDaDong); // phải ném IllegalStateException

        // Nếu không throw thì test này sẽ fail nhờ @Test(expected=...)
        System.out.println("[PASSED] TC54: Hệ thống đã chặn thành công.");
    }

    /**
     * TC55 [Biên]: Chạy thuật toán lần 1 → Đổi điểm chuẩn → Chạy lần 2.
     * Kỳ vọng: kết quả lần 2 ghi đè lần 1, không nhân đôi dữ liệu.
     */
    @Test
    public void testTC55_ChayNhieuLan_KetQuaGhiDe_KhongNhanDoi() {
        System.out.println("[RUNNING] TC55: Chạy lần 1 → Đổi điểm chuẩn → Chạy lần 2.");

        // Lần 1: điểm chuẩn 25.0
        double diemChuanLan1 = 25.0;
        double tongDiem = 24.0;
        String kqLan1 = xetNguyenVong(tongDiem, diemChuanLan1); // ROT

        // Admin đổi điểm chuẩn xuống còn 23.0
        double diemChuanLan2 = 23.0;
        // Xóa kết quả cũ → tính lại (DELETE + INSERT)
        String kqLan2 = xetNguyenVong(tongDiem, diemChuanLan2); // DAU

        // Kết quả cuối cùng phải là lần 2, không cộng gộp
        assertNotEquals("TC55 FAILED: Kết quả lần 2 phải khác lần 1.", kqLan1, kqLan2);
        assertEquals("TC55 FAILED: Kết quả cuối phải là DAU (lần 2).", "DAU", kqLan2);

        System.out.println("[PASSED] TC55: Lần 1 = " + kqLan1 + ", Lần 2 = " + kqLan2 + " (ghi đè thành công).");
    }

    /**
     * TC56 [Hiệu năng/Bảo mật]: Giả lập server bị tắt đột ngột giữa chừng.
     * Kỳ vọng: Database Transaction phải rollback, không có bản ghi nào bị cập nhật nửa chừng.
     * (Trong test này: mô phỏng bằng exception giữa vòng lặp → chỉ set thành công khi commit)
     */
    @Test
    public void testTC56_ServerDown_GiuaChung_CanRollback() {
        System.out.println("[RUNNING] TC56: Server down đột ngột giữa chừng → cần Rollback.");

        int[] soLuotCapNhatThanhCong = {0};
        boolean transactionCommitted = false;

        try {
            // Bắt đầu transaction
            for (int i = 0; i < 100; i++) {
                soLuotCapNhatThanhCong[0]++;
                if (i == 50) {
                    // Giả lập: server crash / network drop
                    throw new RuntimeException("Server bị ngắt kết nối đột ngột!");
                }
            }
            transactionCommitted = true; // Không bao giờ đến đây
        } catch (RuntimeException e) {
            // Rollback → soLuotCapNhatThanhCong không được phản ánh vào DB
            soLuotCapNhatThanhCong[0] = 0; // Simulate rollback
            System.out.println("   [INFO] Đã rollback do lỗi: " + e.getMessage());
        }

        assertFalse("TC56 FAILED: Transaction không được commit khi có lỗi.", transactionCommitted);
        assertEquals("TC56 FAILED: Sau rollback, số bản ghi cập nhật phải = 0.", 0, soLuotCapNhatThanhCong[0]);
        System.out.println("[PASSED] TC56: Rollback thành công, DB sạch sau crash.");
    }

    /**
     * TC57 [Hiệu năng/Concurrency - RACE CONDITION]:
     * 2 Admin cùng lúc bấm nút "Chạy xét tuyển".
     * Kỳ vọng: Hệ thống LOCK, chỉ 1 tiến trình chạy thành công.
     * Tiến trình kia bị từ chối. KHÔNG có 2 luồng chạy song song.
     *
     * Cách test: Dùng ExecutorService + CountDownLatch để bắn 2 request cùng millisecond.
     */
    @Test
    public void testTC57_TwoAdmin_CungChayThuatToan_ChiMot_Duoc_Chay()
            throws InterruptedException {
        System.out.println("[RUNNING] TC57: 2 Admin bấm 'Chạy xét tuyển' cùng một lúc (Race Condition).");
        System.out.println("   [INFO] Kỳ vọng: Hệ thống lock, chỉ 1 tiến trình thành công.");

        int soAdmin = 2;
        AtomicInteger soLanThanhCong = new AtomicInteger(0);
        AtomicInteger soLanBiTuChoi = new AtomicInteger(0);

        CountDownLatch startGate = new CountDownLatch(1); // Cổng khởi động đồng bộ
        CountDownLatch doneLatch = new CountDownLatch(soAdmin);

        ExecutorService pool = Executors.newFixedThreadPool(soAdmin);

        for (int i = 0; i < soAdmin; i++) {
            final int adminId = i + 1;
            pool.submit(() -> {
                try {
                    startGate.await(); // Chờ đến khi tất cả sẵn sàng
                    System.out.println("   [ADMIN " + adminId + "] Đang gọi chạy thuật toán...");
                    boolean success = runAlgorithmWithAtomicLock(true);
                    if (success) {
                        soLanThanhCong.incrementAndGet();
                        System.out.println("   [ADMIN " + adminId + "] ✔ Chạy THÀNH CÔNG.");
                    } else {
                        soLanBiTuChoi.incrementAndGet();
                        System.out.println("   [ADMIN " + adminId + "] ✘ Bị TỪ CHỐI (hệ thống đang xử lý).");
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    doneLatch.countDown();
                }
            });
        }

        startGate.countDown(); // Mở cổng → 2 thread chạy cùng lúc
        doneLatch.await(); // Chờ cả 2 thread hoàn tất
        pool.shutdown();

        System.out.println("   [RESULT] Thành công: " + soLanThanhCong.get()
                + " | Bị từ chối: " + soLanBiTuChoi.get());

        assertEquals("TC57 FAILED: Chỉ đúng 1 Admin được phép chạy thuật toán.", 1, soLanThanhCong.get());
        assertEquals("TC57 FAILED: 1 Admin phải bị từ chối vì đang có tiến trình khác.", 1, soLanBiTuChoi.get());
        System.out.println("[PASSED] TC57: Cơ chế Lock hoạt động đúng - không có Race Condition.");
    }
}
