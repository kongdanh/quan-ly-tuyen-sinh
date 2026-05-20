package com.tuyensinh.service;

import com.tuyensinh.util.HibernateUtil;
import org.hibernate.Session;

import java.io.File;
import java.math.BigDecimal;
import java.util.List;

/**
 * Test thủ công: chạy processDgnlVsatImport với test.xlsx
 * rồi kiểm tra kết quả ở 2 bảng xt_diemthixettuyen và xt_diemcongxetuyen.
 *
 * <p>Thí sinh kiểm tra: TS_0001 — DGNL 815 / 1200.
 *
 * <p>Chạy từ thư mục core-lib:
 * <pre>
 *   mvn compile exec:java -Dexec.mainClass=com.tuyensinh.service.Test
 * </pre>
 */
public class Test {

    // ── CCCD thí sinh cần kiểm tra ───────────────────────────────────────────
    private static final String CCCD_TEST = "TS_0001";

    public static void main(String[] args) {

        // ── Xác định đường dẫn file test.xlsx ─────────────────────────────────
        // Chạy từ core-lib/ → file ở gốc thư mục đó
        File fileTest = new File("test.xlsx");
        if (!fileTest.exists()) {
            // Fallback: thử tìm theo đường dẫn tuyệt đối kề bên src/
            fileTest = new File("core-lib/test.xlsx");
        }
        System.out.println("=".repeat(70));
        System.out.println("FILE TEST: " + fileTest.getAbsolutePath()
                + " (exists=" + fileTest.exists() + ")");
        System.out.println("=".repeat(70));

        // ── Snapshot TRƯỚC khi import ─────────────────────────────────────────
        System.out.println("\n[BEFORE] Trạng thái TRƯỚC khi import:");
        printDiemThi(CCCD_TEST);
        printDiemCong(CCCD_TEST);

        // ── Chạy import ─────────────────────────────────────────────────
        // ── DEBUG: kiểm tra dữ liệu tiền quyết trước khi import ──────────────
        debugBangQuyDoi();
        debugNguyenVong(CCCD_TEST);

        System.out.println("");
        XetTuyenService service = new XetTuyenService();
        System.out.println("\n--- ĐANG CHẠY processDgnlVsatImport ---");
        List<String> errors = service.processDgnlVsatImport(fileTest);

        if (errors.isEmpty()) {
            System.out.println("→ Import hoàn tất KHÔNG có lỗi.");
        } else {
            System.out.println("→ Import xong — CÓ " + errors.size() + " cảnh báo/lỗi:");
            errors.forEach(e -> System.err.println("  [ERR] " + e));
        }

        // ── Snapshot SAU khi import ───────────────────────────────────────────
        System.out.println("\n[AFTER] Trạng thái SAU khi import:");
        printDiemThi(CCCD_TEST);
        printDiemCong(CCCD_TEST);

        // ── Kiểm tra kỳ vọng ─────────────────────────────────────────────────
        System.out.println("\n" + "=".repeat(70));
        System.out.println("KIỂM TRA KỲ VỌNG:");
        assertDiemThi(CCCD_TEST);
        assertDiemCong(CCCD_TEST);
        System.out.println("=".repeat(70));

        // Đóng SessionFactory để thoát process sạch
        HibernateUtil.getSessionFactory().close();
    }

    // ──────────────────────────────────────────────────────────────────────
    //  DEBUG HELPERS
    // ──────────────────────────────────────────────────────────────────────

    /** Kiểm tra bảng xt_bangquydoi có dữ liệu DGNL không. */
    private static void debugBangQuyDoi() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            List<?> rows = session.createNativeQuery(
                    "SELECT d_phuongthuc, d_mon, d_diema, d_diemb, d_diemc, d_diemd " +
                    "FROM xt_bangquydoi WHERE d_phuongthuc = 'DGNL' LIMIT 10", Object[].class)
                    .list();
            System.out.println("\n  [DEBUG] xt_bangquydoi (d_phuongthuc='DGNL'): " + rows.size() + " dòng");
            if (rows.isEmpty()) {
                System.out.println("    ⚠ Bảng quy đổi chưa có dữ liệu DGNL!");
                System.out.println("    ⚠ Diễm DGNL sẽ không được nội suy và không có DiemCong.");
            } else {
                System.out.printf("    %-12s %-10s %-8s %-8s %-8s %-8s%n",
                        "phuongthuc","mon","diema","diemb","diemc","diemd");
                for (Object r : rows) {
                    Object[] row = (Object[]) r;
                    System.out.printf("    %-12s %-10s %-8s %-8s %-8s %-8s%n",
                            row[0], row[1], row[2], row[3], row[4], row[5]);
                }
            }
        } catch (Exception e) {
            System.err.println("  [ERROR] debugBangQuyDoi: " + e.getMessage());
        }
    }

    /** Kiểm tra nguyện vọng của thí sinh. */
    private static void debugNguyenVong(String cccd) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            List<?> rows = session.createNativeQuery(
                    "SELECT nv.nn_cccd, nv.nv_manganh, nth.matohop " +
                    "FROM xt_nguyenvongxettuyen nv " +
                    "JOIN xt_nganh_tohop nth ON nth.manganh = nv.nv_manganh " +
                    "WHERE nv.nn_cccd = :cccd", Object[].class)
                    .setParameter("cccd", cccd)
                    .list();
            System.out.println("\n  [DEBUG] Nguyện vọng của " + cccd + ": " + rows.size() + " tổ hợp");
            if (rows.isEmpty()) {
                System.out.println("    ⚠ Thí sinh không có nguyện vọng nào trong DB!");
            } else {
                System.out.printf("    %-12s %-12s %-10s%n", "cccd", "manganh", "matohop");
                for (Object r : rows) {
                    Object[] row = (Object[]) r;
                    System.out.printf("    %-12s %-12s %-10s%n", row[0], row[1], row[2]);
                }
                // Kiểm tra có bangquydoi tương ứng không
                for (Object r : rows) {
                    Object[] row = (Object[]) r;
                    String matohop = String.valueOf(row[2]);
                    Long cnt = (Long) session.createNativeQuery(
                            "SELECT COUNT(*) FROM xt_bangquydoi " +
                            "WHERE d_phuongthuc='DGNL' AND d_mon=:mon", Long.class)
                            .setParameter("mon", matohop)
                            .uniqueResult();
                    System.out.println("    → bangquydoi DGNL cho matohop='" + matohop + "': " + cnt + " dòng");
                }
            }
        } catch (Exception e) {
            System.err.println("  [ERROR] debugNguyenVong: " + e.getMessage());
        }
    }

    // ──────────────────────────────────────────────────────────────────────────
    //  In dữ liệu bảng xt_diemthixettuyen
    // ──────────────────────────────────────────────────────────────────────────

    private static void printDiemThi(String cccd) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            List<?> rows = session.createNativeQuery(
                    "SELECT cccd, NL1, N1_THI, N1_CC, `TO`, LI, VA, HO, SI, SU, DI " +
                    "FROM xt_diemthixettuyen WHERE cccd = :cccd", Object[].class)
                    .setParameter("cccd", cccd)
                    .list();

            System.out.println("\n  xt_diemthixettuyen (cccd=" + cccd + "):");
            if (rows.isEmpty()) {
                System.out.println("    → Không có dòng nào!");
            } else {
                System.out.printf("    %-12s %-8s %-8s %-8s %-6s %-6s %-6s %-6s %-6s %-6s %-6s%n",
                        "cccd","NL1","N1_THI","N1_CC","TO","LI","VA","HO","SI","SU","DI");
                for (Object r : rows) {
                    Object[] row = (Object[]) r;
                    System.out.printf("    %-12s %-8s %-8s %-8s %-6s %-6s %-6s %-6s %-6s %-6s %-6s%n",
                            row[0], row[1], row[2], row[3],
                            row[4], row[5], row[6], row[7], row[8], row[9], row[10]);
                }
            }
        } catch (Exception e) {
            System.err.println("  [ERROR] printDiemThi: " + e.getMessage());
        }
    }

    // ──────────────────────────────────────────────────────────────────────────
    //  In dữ liệu bảng xt_diemcongxetuyen
    // ──────────────────────────────────────────────────────────────────────────

    private static void printDiemCong(String cccd) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            List<?> rows = session.createNativeQuery(
                    "SELECT dc.dc_keys, dc.manganh, dc.matohop, dc.diemCC " +
                    "FROM xt_diemcongxetuyen dc WHERE dc.ts_cccd = :cccd " +
                    "ORDER BY dc.manganh, dc.matohop", Object[].class)
                    .setParameter("cccd", cccd)
                    .list();

            System.out.println("\n  xt_diemcongxetuyen (cccd=" + cccd + "):");
            if (rows.isEmpty()) {
                System.out.println("    → Không có dòng nào.");
            } else {
                System.out.printf("    %-40s %-12s %-10s %-10s%n",
                        "dc_keys", "manganh", "matohop", "diemCC");
                for (Object r : rows) {
                    Object[] row = (Object[]) r;
                    System.out.printf("    %-40s %-12s %-10s %-10s%n",
                            row[0], row[1], row[2], row[3]);
                }
            }
        } catch (Exception e) {
            System.err.println("  [ERROR] printDiemCong: " + e.getMessage());
        }
    }

    // ──────────────────────────────────────────────────────────────────────────
    //  Assertion: xt_diemthixettuyen — cột NL1 phải được cập nhật
    // ──────────────────────────────────────────────────────────────────────────

    private static void assertDiemThi(String cccd) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            BigDecimal nl1 = (BigDecimal) session.createNativeQuery(
                    "SELECT NL1 FROM xt_diemthixettuyen WHERE cccd = :cccd", BigDecimal.class)
                    .setParameter("cccd", cccd)
                    .uniqueResult();

            if (nl1 == null) {
                fail("xt_diemthixettuyen.NL1 = NULL — chưa được ghi!");
            } else if (nl1.compareTo(BigDecimal.ZERO) > 0) {
                pass("xt_diemthixettuyen.NL1 = " + nl1 + " (điểm gốc DGNL đã lưu)");
            } else {
                fail("xt_diemthixettuyen.NL1 = " + nl1 + " — không hợp lệ (≤ 0)");
            }
        } catch (Exception e) {
            fail("assertDiemThi exception: " + e.getMessage());
        }
    }

    // ──────────────────────────────────────────────────────────────────────────
    //  Assertion: xt_diemcongxetuyen — phải có ít nhất 1 dòng với diemCC > 0
    // ──────────────────────────────────────────────────────────────────────────

    private static void assertDiemCong(String cccd) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Long count = (Long) session.createNativeQuery(
                    "SELECT COUNT(*) FROM xt_diemcongxetuyen WHERE ts_cccd = :cccd AND diemCC > 0",
                    Long.class)
                    .setParameter("cccd", cccd)
                    .uniqueResult();

            if (count == null || count == 0) {
                fail("xt_diemcongxetuyen: không có dòng nào có diemCC > 0 cho CCCD=" + cccd);
            } else {
                pass("xt_diemcongxetuyen: " + count + " dòng diemCC > 0 (điểm DGNL per-tổ-hợp đã UPSERT)");
            }

            // Kiểm tra dc_keys không bị trùng (UNIQUE constraint)
            Long dupCount = (Long) session.createNativeQuery(
                    "SELECT COUNT(*) - COUNT(DISTINCT dc_keys) " +
                    "FROM xt_diemcongxetuyen WHERE ts_cccd = :cccd",
                    Long.class)
                    .setParameter("cccd", cccd)
                    .uniqueResult();

            if (dupCount != null && dupCount > 0) {
                fail("xt_diemcongxetuyen: phát hiện " + dupCount + " dc_keys bị trùng!");
            } else {
                pass("xt_diemcongxetuyen: dc_keys đều duy nhất — UPSERT idempotent OK");
            }
        } catch (Exception e) {
            fail("assertDiemCong exception: " + e.getMessage());
        }
    }

    // ──────────────────────────────────────────────────────────────────────────
    //  Helpers log
    // ──────────────────────────────────────────────────────────────────────────

    private static void pass(String msg) {
        System.out.println("  ✓ PASS: " + msg);
    }

    private static void fail(String msg) {
        System.out.println("  ✗ FAIL: " + msg);
    }
}
