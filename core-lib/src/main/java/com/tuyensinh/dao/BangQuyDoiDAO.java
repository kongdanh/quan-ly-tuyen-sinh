package com.tuyensinh.dao;

import com.tuyensinh.model.BangQuyDoi;
import com.tuyensinh.util.HibernateUtil;
import org.hibernate.Session;

import java.math.BigDecimal;
import java.util.*;

/**
 * DAO cho bảng {@code xt_bangquydoi} — bảng nội suy điểm quy đổi.
 *
 * <p>Mỗi dòng trong bảng đại diện cho 1 ngưỡng (breakpoint):
 * <pre>
 * d_mon      | d_diema (a) | d_diemb (b) | d_diemc (c) | d_diemd (d)
 * TO_VS      |   0.00      |   50.00     |   0.00      |   3.33
 * TO_VS      |  50.00      |  100.00     |   3.33      |   6.67
 * TO_VS      | 100.00      |  150.00     |   6.67      |  10.00
 * DGNL       |   0.00      |  400.00     |   0.00      |  10.00
 * ...
 * </pre>
 *
 * <p>Công thức nội suy tuyến tính:
 * <pre>
 *   y = c + (x - a) / (b - a) * (d - c)
 * </pre>
 * trong đó x là điểm thô cần quy đổi.
 */
public class BangQuyDoiDAO extends GenericDAO<BangQuyDoi> {

    public BangQuyDoiDAO() {
        super(BangQuyDoi.class);
    }

    /**
     * Tìm 2 ngưỡng liên tiếp [a,b,c,d] chứa điểm thô {@code diemTho}
     * cho mã môn {@code maMon}, dùng cho nội suy tuyến tính.
     *
     * <p><b>Chiến lược query một lần duy nhất:</b><br>
     * Lấy toàn bộ breakpoints của {@code maMon} sắp xếp tăng dần theo {@code d_diema}.
     * Tìm khoảng [a,b] thoả {@code a ≤ diemTho ≤ b} trong Java — tránh subquery phức tạp,
     * dễ debug và portable với mọi phiên bản MySQL/MariaDB.
     *
     * <p>Nếu {@code diemTho} vượt ngoài mọi khoảng (điểm max/min tuyệt đối),
     * kết quả trả về chứa ngưỡng ngoài cùng gần nhất (clamp).
     *
     * @param maMon    Mã môn thi trong bảng quy đổi (ví dụ: "TO_VS", "M1", "DGNL").
     * @param diemTho  Điểm thô cần quy đổi.
     * @return Mảng {@code [a, b, c, d]} dưới dạng {@link BigDecimal}[4],
     *         hoặc {@code null} nếu không tìm thấy cấu hình quy đổi cho môn này.
     */
    public BigDecimal[] findInterpolationRange(String maMon, BigDecimal diemTho) {
        if (maMon == null || diemTho == null) return null;

        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            // Lấy tất cả breakpoints của môn, sắp xếp theo d_diema tăng dần
            String hql = "FROM BangQuyDoi b WHERE b.dMon = :maMon ORDER BY b.dDiema ASC";
            List<BangQuyDoi> rows = session.createQuery(hql, BangQuyDoi.class)
                    .setParameter("maMon", maMon)
                    .list();

            if (rows == null || rows.isEmpty()) return null;

            // ── Tìm khoảng [a,b] chứa diemTho ────────────────────────────────
            for (BangQuyDoi row : rows) {
                BigDecimal a = row.getDDiema();
                BigDecimal b = row.getDDiemb();
                BigDecimal c = row.getDDiemc();
                BigDecimal d = row.getDDiemd();

                if (a == null || b == null || c == null || d == null) continue;

                // a ≤ diemTho ≤ b
                if (diemTho.compareTo(a) >= 0 && diemTho.compareTo(b) <= 0) {
                    return new BigDecimal[]{a, b, c, d};
                }
            }

            // ── Clamp: ngoài mọi khoảng → dùng ngưỡng ngoài cùng ─────────────
            BangQuyDoi first = rows.get(0);
            BangQuyDoi last  = rows.get(rows.size() - 1);

            if (diemTho.compareTo(first.getDDiema()) < 0) {
                // Dưới ngưỡng tối thiểu → kẹp về khoảng đầu tiên
                return new BigDecimal[]{
                        first.getDDiema(), first.getDDiemb(),
                        first.getDDiemc(), first.getDDiemd()};
            } else {
                // Vượt quá ngưỡng tối đa → kẹp về khoảng cuối cùng
                return new BigDecimal[]{
                        last.getDDiema(), last.getDDiemb(),
                        last.getDDiemc(), last.getDDiemd()};
            }

        } catch (Exception e) {
            System.err.println("[BangQuyDoiDAO] Lỗi findInterpolationRange maMon="
                    + maMon + " diem=" + diemTho + ": " + e.getMessage());
            return null;
        }
    }

    /**
     * Tải toàn bộ bảng quy đổi của 1 mã môn vào bộ nhớ (cache tại caller).
     *
     * <p>Dùng khi cần tra cứu nhiều lần cho cùng một mã môn — tránh mở nhiều session.
     * Caller có trách nhiệm giữ cache và truyền vào {@link #interpolateFromRows}.
     *
     * @param maMon Mã môn thi.
     * @return Danh sách breakpoints sắp xếp theo {@code d_diema} tăng dần; rỗng nếu không có.
     */
    public List<BangQuyDoi> findAllByMaMon(String maMon) {
        if (maMon == null) return Collections.emptyList();
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery(
                    "FROM BangQuyDoi b WHERE b.dMon = :maMon ORDER BY b.dDiema ASC",
                    BangQuyDoi.class)
                    .setParameter("maMon", maMon)
                    .list();
        } catch (Exception e) {
            System.err.println("[BangQuyDoiDAO] Lỗi findAllByMaMon maMon=" + maMon + ": " + e.getMessage());
            return Collections.emptyList();
        }
    }

    /**
     * Thực hiện nội suy tuyến tính từ danh sách breakpoints đã tải sẵn.
     *
     * <p>Dùng khi caller đã giữ cache ({@link #findAllByMaMon}) để tránh gọi DB lặp lại.
     *
     * @param rows     Danh sách breakpoints đã sắp xếp theo {@code d_diema} tăng dần.
     * @param diemTho  Điểm thô cần quy đổi.
     * @return Mảng {@code [a, b, c, d]}, hoặc {@code null} nếu {@code rows} rỗng.
     */
    public static BigDecimal[] interpolateFromRows(List<BangQuyDoi> rows, BigDecimal diemTho) {
        if (rows == null || rows.isEmpty() || diemTho == null) return null;

        for (BangQuyDoi row : rows) {
            BigDecimal a = row.getDDiema(), b = row.getDDiemb();
            BigDecimal c = row.getDDiemc(), d = row.getDDiemd();
            if (a == null || b == null || c == null || d == null) continue;
            if (diemTho.compareTo(a) >= 0 && diemTho.compareTo(b) <= 0) {
                return new BigDecimal[]{a, b, c, d};
            }
        }

        // Clamp
        BangQuyDoi first = rows.get(0);
        BangQuyDoi last  = rows.get(rows.size() - 1);
        if (diemTho.compareTo(first.getDDiema()) < 0)
            return new BigDecimal[]{first.getDDiema(), first.getDDiemb(),
                    first.getDDiemc(), first.getDDiemd()};
        return new BigDecimal[]{last.getDDiema(), last.getDDiemb(),
                last.getDDiemc(), last.getDDiemd()};
    }

    /**
     * Tải breakpoints lọc theo {@code d_phuongthuc} VÀ {@code d_mon}.
     *
     * <p>Dùng cho DGNL per-tổ-hợp: {@code d_phuongthuc='DGNL'}
     * và {@code d_mon = matohop} (ví dụ: 'A00', 'D01').
     *
     * <p>Cũng dùng cho IELTS: {@code d_phuongthuc='IELTS'} và mã chứng chỉ.
     *
     * @param phuongThuc Giá trị {@code d_phuongthuc} (ví dụ: "DGNL", "IELTS").
     * @param mon        Giá trị {@code d_mon} (ví dụ: "A00", "N1").
     * @return Danh sách breakpoints sắp xếp tăng dần theo {@code d_diema}.
     */
    public List<BangQuyDoi> findAllByPhuongThucAndMon(String phuongThuc, String mon) {
        if (phuongThuc == null || mon == null) return Collections.emptyList();
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery(
                    "FROM BangQuyDoi b " +
                    "WHERE b.dPhuongthuc = :pt AND b.dMon = :mon " +
                    "ORDER BY b.dDiema ASC",
                    BangQuyDoi.class)
                    .setParameter("pt",  phuongThuc)
                    .setParameter("mon", mon)
                    .list();
        } catch (Exception e) {
            System.err.println("[BangQuyDoiDAO] findAllByPhuongThucAndMon pt="
                    + phuongThuc + " mon=" + mon + ": " + e.getMessage());
            return Collections.emptyList();
        }
    }

    /**
     * Tra cứu {@code d_diemb} theo điểm thô — dùng cho IELTS (step-based, không nội suy).
     *
     * <p>Tìm breakpoint có {@code d_diema ≤ diemTho} cao nhất (floor lookup).
     * Kết quả là {@code d_diemb} của breakpoint đó.
     *
     * @param phuongThuc Phương thức (ví dụ: "IELTS").
     * @param mon        Mã môn / loại chứng chỉ.
     * @param diemTho    Điểm thô cần tra.
     * @return {@code d_diemb} tương ứng, hoặc {@code null} nếu không tìm thấy.
     */
    public BigDecimal lookupDiemb(String phuongThuc, String mon, BigDecimal diemTho) {
        List<BangQuyDoi> rows = findAllByPhuongThucAndMon(phuongThuc, mon);
        if (rows.isEmpty() || diemTho == null) return null;
        BigDecimal result = null;
        for (BangQuyDoi row : rows) {
            if (row.getDDiema() != null && diemTho.compareTo(row.getDDiema()) >= 0) {
                result = row.getDDiemb(); // floor: cập nhật khi tìm ngưỡng phù hợp
            } else {
                break; // Danh sách tăng dần → vượt qua → dừng
            }
        }
        return result;
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  NORMALIZE & IN-MEMORY CACHE
    // ══════════════════════════════════════════════════════════════════════════

    /**
     * Chuẩn hóa mã môn từ Excel về mã gốc lưu trong {@code xt_bangquydoi}.
     *
     * <p><b>Quy tắc cắt suffix (Regex):</b>
     * <pre>
     *   N1_VS   → N1
     *   LI_VS   → LI
     *   TO_VS   → TO
     *   VA_VS   → VA
     *   M1_DGNL → M1
     *   DGNL    → DGNL   (không có suffix → giữ nguyên)
     * </pre>
     *
     * <p>Pattern: cắt bỏ phần {@code _<SUFFIX>} với suffix là 2–5 ký tự IN HOA
     * ở cuối chuỗi (ví dụ: {@code _VS}, {@code _DGNL}, {@code _THPT}).
     * Nếu không khớp, trả về nguyên giá trị đầu vào.
     *
     * @param maMon Mã môn từ Excel (có thể có suffix, ví dụ "N1_VS").
     * @return Mã môn đã chuẩn hóa (ví dụ "N1"), hoặc nguyên giá trị nếu không có suffix.
     */
    public static String normalizeMaMon(String maMon) {
        if (maMon == null || maMon.isBlank()) return maMon;
        // Cắt suffix dạng _XX đến _XXXXX (2–5 ký tự IN HOA hoặc số) ở cuối chuỗi
        // Ví dụ: N1_VS → N1, LI_VS → LI, M1_DGNL → M1, DGNL → DGNL
        return maMon.trim().toUpperCase().replaceAll("_[A-Z0-9]{2,5}$", "");
    }

    /**
     * Tải toàn bộ bảng {@code xt_bangquydoi} vào bộ nhớ một lần duy nhất.
     *
     * <p><b>Mục đích:</b> Tránh hàng nghìn lệnh {@code SELECT} lẻ tẻ trong vòng lặp
     * xử lý Excel — thay vào đó load 1 lần rồi tra cứu bằng {@code Map}.
     *
     * <p><b>Cấu trúc Map trả về:</b>
     * <pre>
     *   Key   = {@link #normalizeMaMon(String)} của {@code d_mon}
     *           (ví dụ: "TO", "LI", "N1", "DGNL")
     *   Value = Danh sách breakpoints, sắp xếp tăng dần theo {@code d_diema}
     * </pre>
     *
     * <p><b>Lưu ý:</b> Đây là cache tại caller — mỗi import session gọi 1 lần,
     * không dùng lâu dài (bảng quy đổi có thể được admin cập nhật giữa các session).
     *
     * @return Map không null; rỗng nếu bảng trống hoặc có lỗi DB.
     */
    public Map<String, List<BangQuyDoi>> loadAllAsCache() {
        Map<String, List<BangQuyDoi>> cache = new LinkedHashMap<>();
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            List<BangQuyDoi> all = session.createQuery(
                    "FROM BangQuyDoi b ORDER BY b.dMon ASC, b.dDiema ASC",
                    BangQuyDoi.class)
                    .list();

            for (BangQuyDoi row : all) {
                // Lưu với cả key gốc (TO_VS) lẫn key chuẩn hóa (TO) để tăng tỷ lệ hit
                String rawKey  = row.getDMon() == null ? "" : row.getDMon().trim().toUpperCase();
                String normKey = normalizeMaMon(rawKey);

                // Thêm theo key gốc
                cache.computeIfAbsent(rawKey,  k -> new ArrayList<>()).add(row);
                // Thêm theo key chuẩn hóa (nếu khác key gốc → alias thêm)
                if (!normKey.equals(rawKey)) {
                    cache.computeIfAbsent(normKey, k -> new ArrayList<>()).add(row);
                }
            }
            System.out.println("[BangQuyDoiDAO] Loaded " + all.size()
                    + " breakpoints → " + cache.size() + " keys vào in-memory cache.");
        } catch (Exception e) {
            System.err.println("[BangQuyDoiDAO] Lỗi loadAllAsCache: " + e.getMessage());
        }
        return cache;
    }

    /**
     * Tra cứu breakpoints từ cache đã tải sẵn, tự động chuẩn hóa mã môn.
     *
     * <p>Ưu tiên tra theo {@code maMon} gốc trước,
     * nếu miss thì thử lại với {@link #normalizeMaMon(String) mã chuẩn hóa}.
     *
     * @param cache   Map đã được tải bởi {@link #loadAllAsCache()}.
     * @param maMon   Mã môn từ Excel (có thể có suffix như {@code _VS}).
     * @return Danh sách breakpoints, hoặc {@link Collections#emptyList()} nếu không có.
     */
    public static List<BangQuyDoi> lookupFromCache(
            Map<String, List<BangQuyDoi>> cache, String maMon) {
        if (cache == null || maMon == null) return Collections.emptyList();

        String upper = maMon.trim().toUpperCase();

        // Thử key gốc trước (hit khi DB lưu TO_VS)
        List<BangQuyDoi> direct = cache.get(upper);
        if (direct != null && !direct.isEmpty()) return direct;

        // Fallback: thử key chuẩn hóa (hit khi DB lưu TO nhưng Excel ghi TO_VS)
        String normalized = normalizeMaMon(upper);
        List<BangQuyDoi> norm = cache.get(normalized);
        return norm != null ? norm : Collections.emptyList();
    }
}
