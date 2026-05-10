package com.tuyensinh.service;

import com.tuyensinh.dao.*;
import com.tuyensinh.model.*;
import com.tuyensinh.util.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.*;

/**
 * Service xử lý nghiệp vụ đăng ký nguyện vọng xét tuyển.
 *
 * <p><b>Hai nhóm API:</b>
 * <ul>
 *   <li>{@link #dangKyNguyenVong} — Đăng ký nguyện vọng với đầy đủ validation,
 *       business rule và Transaction atomic.</li>
 *   <li>{@link #saveWish}, {@link #deleteWish}, {@link #reorderByIds} — API
 *       tương thích ngược với UI panel Swing cũ.</li>
 * </ul>
 */
public class NguyenVongService {

    private static final Logger log = LoggerFactory.getLogger(NguyenVongService.class);

    private static final int MAX_NGUYEN_VONG = 3;

    // ── DAO dependencies ──────────────────────────────────────────────────────
    private final NguyenVongDAO  nvDAO          = new NguyenVongDAO();
    private final NganhToHopDAO  ntDAO          = new NganhToHopDAO();
    private final NganhDAO       nganhDAO       = new NganhDAO();
    private final ThiSinhService thiSinhService = new ThiSinhService();
    private final DiemService    diemService    = new DiemService();

    // ══════════════════════════════════════════════════════════════════════════
    //  1. ĐĂNG KÝ NGUYỆN VỌNG — API chính (atomic, validated)
    // ══════════════════════════════════════════════════════════════════════════

    /**
     * Đăng ký một nguyện vọng xét tuyển cho thí sinh với đầy đủ validation.
     *
     * <p><b>Luồng xử lý:</b>
     * <ol>
     *   <li><b>Validation</b> — Kiểm tra thí sinh, ngành, tổ hợp môn hợp lệ.</li>
     *   <li><b>Business Rule</b> — Thứ tự nguyện vọng phải duy nhất.</li>
     *   <li><b>Persist</b> — Lưu {@code NguyenVong} và tăng {@code sl_dadangky}.</li>
     * </ol>
     *
     * <p>Bước 2 và 3 nằm trong một {@code Transaction} để đảm bảo tính Atomic.
     *
     * @param cccd       Số CCCD của thí sinh.
     * @param manganh    Mã ngành đăng ký.
     * @param matohop    Mã tổ hợp môn thí sinh chọn.
     * @param thuTu      Thứ tự nguyện vọng (duy nhất trong danh sách của thí sinh).
     * @param phuongThuc Phương thức xét tuyển (ví dụ: "DGNL", "THPT", "VSAT").
     * @param thm        Tổ hợp môn thi phụ (nullable).
     * @return {@link KetQuaDangKy} chứa trạng thái và thông điệp kết quả.
     */
    public KetQuaDangKy dangKyNguyenVong(String cccd,
                                         String manganh,
                                         String matohop,
                                         int    thuTu,
                                         String phuongThuc,
                                         String thm) {

        log.info("[DangKy] Bắt đầu: CCCD={} ngành={} tổhợp={} thuTu={} pt={}",
                cccd, manganh, matohop, thuTu, phuongThuc);

        // ── Bước 1: Validation (ngoài transaction — tránh giữ lock DB lâu) ───
        try {
            String validErr = validate(cccd, manganh, matohop);
            if (validErr != null) {
                log.warn("[DangKy] Validation thất bại: {}", validErr);
                return KetQuaDangKy.thatBai(validErr);
            }
        } catch (Exception e) {
            log.error("[DangKy] Lỗi khi validation CCCD={}: {}", cccd, e.getMessage(), e);
            return KetQuaDangKy.thatBai("Lỗi hệ thống khi kiểm tra dữ liệu: " + e.getMessage());
        }

        // ── Bước 2 & 3: Business rule + persist (trong một Transaction) ───────
        Session     session = null;
        Transaction tx      = null;

        try {
            session = HibernateUtil.getSessionFactory().openSession();
            tx      = session.beginTransaction();

            // ── 2a. Kiểm tra thứ tự nguyện vọng không trùng ─────────────────
            if (isThuTuDaTonTai(session, cccd, thuTu)) {
                tx.rollback();
                String msg = "Thứ tự nguyện vọng " + thuTu + " đã tồn tại cho CCCD=" + cccd;
                log.warn("[DangKy] {}", msg);
                return KetQuaDangKy.thatBai(msg);
            }

            // ── 2b. Kiểm tra số lượng nguyện vọng chưa vượt giới hạn ────────
            long soNvHienTai = countNguyenVong(session, cccd);
            if (soNvHienTai >= MAX_NGUYEN_VONG) {
                tx.rollback();
                String msg = "Thí sinh CCCD=" + cccd + " đã đăng ký tối đa "
                        + MAX_NGUYEN_VONG + " nguyện vọng.";
                log.warn("[DangKy] {}", msg);
                return KetQuaDangKy.thatBai(msg);
            }

            // ── 2c. Load entity ThiSinh và Nganh trong cùng session ──────────
            ThiSinh thiSinh = findThiSinh(session, cccd);
            Nganh   nganh   = findNganh(session, manganh);

            if (thiSinh == null || nganh == null) {
                tx.rollback();
                return KetQuaDangKy.thatBai("Không tải được entity ThiSinh hoặc Nganh.");
            }

            // ── 3a. Tạo entity NguyenVong ─────────────────────────────────────
            // nv_keys = {cccd}_{manganh}_{thuTu}_{phuongThuc}
            String nvKeys = cccd + "_" + manganh + "_" + thuTu + "_" + phuongThuc;

            NguyenVong nv = new NguyenVong();
            nv.setThiSinh(thiSinh);
            nv.setNganh(nganh);
            nv.setNvTt(thuTu);
            nv.setTtPhuongthuc(phuongThuc);
            nv.setTtThm(thm);
            nv.setNvKetqua("CHUA_XET");
            nv.setNvKeys(nvKeys);

            session.persist(nv);
            log.info("[DangKy] Đã tạo NguyenVong id={} CCCD={} ngành={} tổhợp={} thuTu={}",
                    nv.getId(), cccd, manganh, matohop, thuTu);

            // ── 3b. Tăng sl_dadangky — atomic với persist nt trên ────────────
            int updatedRows = session.createNativeMutationQuery(
                    "UPDATE xt_nganh SET sl_dadangky = sl_dadangky + 1 " +
                    "WHERE manganh = :manganh")
                    .setParameter("manganh", manganh)
                    .executeUpdate();

            if (updatedRows == 0) {
                // Guard: ngành bị xoá giữa chừng (race condition cực hiếm)
                tx.rollback();
                log.error("[DangKy] Không cập nhật sl_dadangky cho ngành {}", manganh);
                return KetQuaDangKy.thatBai("Không cập nhật được sl_dadangky cho ngành " + manganh);
            }

            tx.commit();
            log.info("[DangKy] Thành công: CCCD={} ngành={} thuTu={} sl_dadangky+1",
                    cccd, manganh, thuTu);
            return KetQuaDangKy.thanhCong(
                    "Đăng ký nguyện vọng thành công: ngành=" + manganh + ", thứ tự=" + thuTu);

        } catch (Exception e) {
            if (tx != null && tx.isActive()) {
                try {
                    tx.rollback();
                    log.warn("[DangKy] Đã rollback transaction: {}", e.getMessage());
                } catch (Exception rb) {
                    log.error("[DangKy] Rollback thất bại: {}", rb.getMessage(), rb);
                }
            }
            log.error("[DangKy] Lỗi khi đăng ký CCCD={}: {}", cccd, e.getMessage(), e);
            return KetQuaDangKy.thatBai("Lỗi hệ thống: " + e.getMessage());

        } finally {
            if (session != null && session.isOpen()) session.close();
        }
    }

    /**
     * Huỷ một nguyện vọng đã đăng ký và giảm {@code sl_dadangky} của ngành.
     *
     * @param nvId ID của NguyenVong cần huỷ.
     * @return {@link KetQuaDangKy} kết quả huỷ.
     */
    public KetQuaDangKy huyNguyenVong(int nvId) {
        log.info("[Huy] Bắt đầu huỷ nguyện vọng id={}", nvId);

        Session     session = null;
        Transaction tx      = null;

        try {
            session = HibernateUtil.getSessionFactory().openSession();
            tx      = session.beginTransaction();

            NguyenVong nv = session.get(NguyenVong.class, nvId);
            if (nv == null) {
                tx.rollback();
                return KetQuaDangKy.thatBai("Không tìm thấy nguyện vọng id=" + nvId);
            }

            String manganh = nv.getNganh().getManganh();
            String cccd    = nv.getThiSinh().getCccd();

            session.remove(nv);

            // Dùng GREATEST để sl_dadangky không xuống âm
            session.createNativeMutationQuery(
                    "UPDATE xt_nganh SET sl_dadangky = GREATEST(sl_dadangky - 1, 0) " +
                    "WHERE manganh = :manganh")
                    .setParameter("manganh", manganh)
                    .executeUpdate();

            tx.commit();
            log.info("[Huy] Thành công: id={} CCCD={} ngành={}", nvId, cccd, manganh);
            return KetQuaDangKy.thanhCong("Huỷ nguyện vọng thành công.");

        } catch (Exception e) {
            if (tx != null && tx.isActive()) {
                try { tx.rollback(); } catch (Exception rb) {
                    log.error("[Huy] Rollback thất bại: {}", rb.getMessage());
                }
            }
            log.error("[Huy] Lỗi khi huỷ nguyện vọng id={}: {}", nvId, e.getMessage(), e);
            return KetQuaDangKy.thatBai("Lỗi hệ thống: " + e.getMessage());

        } finally {
            if (session != null && session.isOpen()) session.close();
        }
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  2. API TƯƠNG THÍCH NGƯỢC (cho UI Swing panel)
    // ══════════════════════════════════════════════════════════════════════════

    /**
     * Thêm mới hoặc chỉnh sửa nguyện vọng — API cũ dành cho UI Swing panel.
     *
     * <p>Tự động tìm tổ hợp mang lại điểm xét cao nhất cho ngành đã chọn.
     *
     * @param cccd    CCCD thí sinh đang đăng nhập.
     * @param manganh Mã ngành muốn đăng ký.
     * @param idnv    {@code null} = thêm mới; có giá trị = sửa nguyện vọng đó.
     * @param diem    Điểm thi của thí sinh.
     * @return {@link SaveResult}: OK, NOT_QUALIFIED, MAX_REACHED, FORBIDDEN.
     */
    public SaveResult saveWish(String cccd, String manganh, Integer idnv, DiemThiXetTuyen diem) {
        Map<String, Double> scoreMap = diemService.buildScoreMap(diem);
        List<NganhToHop>    listChoPhep = ntDAO.findByMaNganh(manganh);

        NganhToHop toHopToiUu = null;
        double diemMax = -1.0;
        for (NganhToHop th : listChoPhep) {
            double d = diemService.tinhDiemXet(scoreMap, th);
            if (d > diemMax) { diemMax = d; toHopToiUu = th; }
        }

        if (toHopToiUu == null) return SaveResult.NOT_QUALIFIED;

        Nganh  nganh = nganhDAO.findByMaNganh(manganh).orElse(null);
        String ttThm = toHopToiUu.getThMon1() + "-"
                     + toHopToiUu.getThMon2() + "-"
                     + toHopToiUu.getThMon3();

        if (idnv != null) {
            return updateWish(cccd, idnv, nganh, manganh, diemMax, ttThm);
        } else {
            return insertWish(cccd, nganh, manganh, diemMax, ttThm, diem);
        }
    }

    /**
     * Xóa nguyện vọng và tự sắp xếp lại thứ tự còn lại.
     *
     * <p>Kiểm tra quyền sở hữu: chỉ xóa được NV của chính mình.
     *
     * @return {@code true} nếu xóa thành công.
     */
    public boolean deleteWish(int idnv, String cccd) {
        NguyenVong nv = nvDAO.findById(idnv);
        if (nv == null || !nv.getThiSinh().getCccd().equals(cccd)) {
            log.warn("[DeleteWish] Không tìm thấy hoặc không có quyền: idnv={} cccd={}", idnv, cccd);
            return false;
        }
        nvDAO.delete(nv);
        reorder(cccd);
        log.info("[DeleteWish] Đã xoá nguyện vọng id={} CCCD={}", idnv, cccd);
        return true;
    }

    /**
     * Cập nhật lại thứ tự nguyện vọng sau khi kéo thả (drag-and-drop).
     *
     * <p>Kiểm tra quyền: chỉ reorder các ID thuộc về {@code cccd}.
     *
     * @param ids  Mảng ID theo thứ tự mới.
     * @param cccd CCCD thí sinh.
     */
    public void reorderByIds(String[] ids, String cccd) {
        Set<Integer> validIds = new HashSet<>();
        for (NguyenVong v : nvDAO.findByCccd(cccd)) validIds.add(v.getId());

        for (int i = 0; i < ids.length; i++) {
            try {
                int id = Integer.parseInt(ids[i].trim());
                if (!validIds.contains(id)) continue;
                NguyenVong nv = nvDAO.findById(id);
                if (nv == null) continue;
                nv.setNvTt(i + 1);
                nv.setNvKeys(buildKey(cccd, nv.getNganh().getManganh(), i + 1));
                nvDAO.update(nv);
            } catch (NumberFormatException ignored) {
                log.warn("[Reorder] ID không hợp lệ: {}", ids[i]);
            }
        }
        log.info("[Reorder] Đã sắp xếp lại {} nguyện vọng của CCCD={}", ids.length, cccd);
    }

    /** Lấy danh sách nguyện vọng theo CCCD. */
    public List<NguyenVong> findByCccd(String cccd) {
        if (cccd == null || cccd.isBlank()) return Collections.emptyList();
        return nvDAO.findByCccd(cccd);
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  3. VALIDATION — chạy ngoài transaction
    // ══════════════════════════════════════════════════════════════════════════

    /**
     * Kiểm tra tính hợp lệ của bộ ba (cccd, manganh, matohop).
     *
     * @return {@code null} nếu hợp lệ; chuỗi mô tả lỗi nếu không.
     */
    private String validate(String cccd, String manganh, String matohop) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {

            // V1. Thí sinh phải tồn tại
            ThiSinh thiSinh = findThiSinh(session, cccd);
            if (thiSinh == null) {
                return "Thí sinh không tồn tại: CCCD=" + cccd;
            }

            // V2. Ngành phải tồn tại
            Nganh nganh = findNganh(session, manganh);
            if (nganh == null) {
                return "Ngành không tồn tại: manganh=" + manganh;
            }

            // V3. Tổ hợp môn phải thuộc ngành đó (xt_nganh_tohop)
            if (!isTohopOfNganh(session, manganh, matohop)) {
                return "Tổ hợp '" + matohop + "' không thuộc ngành '" + manganh
                        + "'. Vui lòng chọn tổ hợp được ngành cho phép.";
            }

            return null; // Hợp lệ
        }
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  4. PRIVATE HELPERS — DAO / Business
    // ══════════════════════════════════════════════════════════════════════════

    /** Tìm ThiSinh theo CCCD trong session đã cho. */
    private ThiSinh findThiSinh(Session session, String cccd) {
        List<ThiSinh> result = session.createQuery(
                "FROM ThiSinh ts WHERE ts.cccd = :cccd", ThiSinh.class)
                .setParameter("cccd", cccd)
                .setMaxResults(1)
                .list();
        return result.isEmpty() ? null : result.get(0);
    }

    /** Tìm Nganh theo mã ngành trong session đã cho. */
    private Nganh findNganh(Session session, String manganh) {
        List<Nganh> result = session.createQuery(
                "FROM Nganh n WHERE n.manganh = :manganh", Nganh.class)
                .setParameter("manganh", manganh)
                .setMaxResults(1)
                .list();
        return result.isEmpty() ? null : result.get(0);
    }

    /**
     * Kiểm tra {@code matohop} có trong danh sách tổ hợp của {@code manganh} không
     * (bảng {@code xt_nganh_tohop}).
     */
    private boolean isTohopOfNganh(Session session, String manganh, String matohop) {
        Long count = session.createQuery(
                "SELECT COUNT(nth) FROM NganhToHop nth " +
                "WHERE nth.nganh.manganh = :manganh AND nth.toHopMon.matohop = :matohop",
                Long.class)
                .setParameter("manganh", manganh)
                .setParameter("matohop", matohop)
                .uniqueResult();
        return count != null && count > 0;
    }

    /**
     * Kiểm tra thứ tự nguyện vọng {@code thuTu} đã tồn tại cho {@code cccd} chưa.
     * Gọi <b>trong</b> transaction để đọc nhất quán.
     */
    private boolean isThuTuDaTonTai(Session session, String cccd, int thuTu) {
        Long count = session.createQuery(
                "SELECT COUNT(nv) FROM NguyenVong nv " +
                "WHERE nv.thiSinh.cccd = :cccd AND nv.nvTt = :thutu",
                Long.class)
                .setParameter("cccd", cccd)
                .setParameter("thutu", thuTu)
                .uniqueResult();
        return count != null && count > 0;
    }

    /** Đếm số nguyện vọng hiện tại của thí sinh. */
    private long countNguyenVong(Session session, String cccd) {
        Long count = session.createQuery(
                "SELECT COUNT(nv) FROM NguyenVong nv WHERE nv.thiSinh.cccd = :cccd",
                Long.class)
                .setParameter("cccd", cccd)
                .uniqueResult();
        return count != null ? count : 0L;
    }

    // ── Helpers cho API cũ (saveWish / deleteWish) ────────────────────────────

    private SaveResult updateWish(String cccd, int idnv, Nganh nganh,
                                  String manganh, double diemMax, String ttThm) {
        NguyenVong nv = nvDAO.findById(idnv);
        if (nv == null || !nv.getThiSinh().getCccd().equals(cccd)) {
            return SaveResult.FORBIDDEN;
        }
        nv.setNganh(nganh);
        nv.setDiemXettuyen(BigDecimal.valueOf(diemMax));
        nv.setTtThm(ttThm);
        nv.setNvKeys(buildKey(cccd, manganh, nv.getNvTt()));
        nvDAO.update(nv);
        return SaveResult.OK;
    }

    private SaveResult insertWish(String cccd, Nganh nganh, String manganh,
                                  double diemMax, String ttThm, DiemThiXetTuyen diem) {
        List<NguyenVong> current = nvDAO.findByCccd(cccd);
        if (current.size() >= MAX_NGUYEN_VONG) return SaveResult.MAX_REACHED;

        int     thuTu = current.size() + 1;
        ThiSinh ts    = thiSinhService.findByCccd(cccd).orElse(null);

        NguyenVong nv = new NguyenVong();
        nv.setThiSinh(ts);
        nv.setNganh(nganh);
        nv.setNvTt(thuTu);
        nv.setDiemXettuyen(BigDecimal.valueOf(diemMax));
        nv.setTtThm(ttThm);
        nv.setNvKetqua("CHO");
        nv.setTtPhuongthuc(diem.getDPhuongthuc());
        nv.setNvKeys(buildKey(cccd, manganh, thuTu));
        nvDAO.save(nv);
        return SaveResult.OK;
    }

    private void reorder(String cccd) {
        List<NguyenVong> list = nvDAO.findByCccd(cccd);
        for (int i = 0; i < list.size(); i++) {
            NguyenVong nv = list.get(i);
            nv.setNvTt(i + 1);
            nv.setNvKeys(buildKey(cccd, nv.getNganh().getManganh(), i + 1));
            nvDAO.update(nv);
        }
    }

    private String buildKey(String cccd, String manganh, int thuTu) {
        return cccd + "_" + manganh + "_" + thuTu;
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  5. VALUE OBJECTS
    // ══════════════════════════════════════════════════════════════════════════

    /**
     * Đóng gói kết quả của một lần đăng ký / huỷ nguyện vọng.
     *
     * <p>Pattern <em>Result Object</em>: caller không cần bắt exception
     * và có thể hiển thị thông điệp trực tiếp lên UI.
     */
    public static final class KetQuaDangKy {

        private final boolean thanhCong;
        private final String  thongDiep;

        private KetQuaDangKy(boolean thanhCong, String thongDiep) {
            this.thanhCong = thanhCong;
            this.thongDiep = thongDiep;
        }

        /** Factory: tạo kết quả thành công. */
        public static KetQuaDangKy thanhCong(String msg) {
            return new KetQuaDangKy(true,  msg);
        }

        /** Factory: tạo kết quả thất bại. */
        public static KetQuaDangKy thatBai(String msg) {
            return new KetQuaDangKy(false, msg);
        }

        /** @return {@code true} nếu thao tác thành công. */
        public boolean isThanhCong() { return thanhCong; }

        /** @return Thông điệp để hiển thị cho người dùng hoặc ghi log. */
        public String getThongDiep() { return thongDiep; }

        @Override
        public String toString() {
            return (thanhCong ? "[OK] " : "[FAIL] ") + thongDiep;
        }
    }

    /** Enum kết quả cho API cũ (saveWish). */
    public enum SaveResult {
        /** Lưu thành công. */
        OK,
        /** Thí sinh không đủ điều kiện (không có tổ hợp hợp lệ). */
        NOT_QUALIFIED,
        /** Đã đạt giới hạn số nguyện vọng tối đa. */
        MAX_REACHED,
        /** Không có quyền thao tác trên nguyện vọng này. */
        FORBIDDEN
    }
}