package com.tuyensinh.service;

import com.tuyensinh.dao.DiemCongDAO;
import com.tuyensinh.dao.DiemThiXetTuyenDAO;
import com.tuyensinh.dao.NguyenVongDAO;
import com.tuyensinh.dto.IeltsImportDTO;
import com.tuyensinh.model.DiemCong;
import com.tuyensinh.model.DiemThiXetTuyen;
import com.tuyensinh.model.NganhToHop;
import com.tuyensinh.model.ThiSinh;
import com.tuyensinh.util.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;

import java.io.File;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Service xử lý toàn bộ luồng nghiệp vụ Xét tuyển.
 *
 * <p>Điểm vào chính hiện tại: {@link #processIELTSImport(File)}.
 */
public class XetTuyenService {

    // ──────────────────────────────────────────────────────────────────────────
    //  Dependencies (tái sử dụng DAO/Service có sẵn trong core-lib)
    // ──────────────────────────────────────────────────────────────────────────

    private final DiemThiXetTuyenDAO diemThiDAO   = new DiemThiXetTuyenDAO();
    private final NguyenVongDAO      nguyenVongDAO = new NguyenVongDAO();
    private final DiemCongDAO        diemCongDAO   = new DiemCongDAO();
    private final BaseImportService<IeltsImportDTO, IeltsImportDTO> baseImport = new BaseImportService<>();

    /** Batch size khớp với cấu hình hibernate.jdbc.batch_size (thường 50-100). */
    private static final int BATCH_SIZE = 50;

    // ──────────────────────────────────────────────────────────────────────────
    //  PUBLIC API
    // ──────────────────────────────────────────────────────────────────────────

    /**
     * Xử lý import file Excel chứng chỉ IELTS / ngoại ngữ.
     *
     * <h3>Luồng xử lý (per row):</h3>
     * <ol>
     *   <li><b>Validation</b> – Kiểm tra {@code cccd} tồn tại trong cả
     *       {@code xt_diemthixettuyen} VÀ {@code xt_nguyenvongxettuyen}.
     *       Nếu không có trong bảng nào → log + skip dòng đó.</li>
     *   <li><b>JOIN Query</b> – Lấy danh sách ngành/tổ hợp đăng ký qua
     *       {@code NguyenVongDAO#findNganhToHopByCccd}.</li>
     *   <li><b>Subset Check</b> – Lọc các tổ hợp mà thí sinh có <em>đủ</em> điểm
     *       môn văn hóa trong "Tập hợp cha" ({@code xt_diemthixettuyen}).
     *       Điều kiện: nếu tổ hợp yêu cầu môn X ({@code nth.monX = true})
     *       thì thí sinh phải có điểm môn X ({@code dt.monX > 0}).</li>
     *   <li><b>Conditional Update/Insert</b>:
     *     <ul>
     *       <li>Tổ hợp <em>có</em> Ngoại ngữ ({@code nth.n1 = true}):
     *           UPDATE {@code N1_CC} = GREATEST(N1_THI, diemQd).</li>
     *       <li>Tổ hợp <em>không</em> Ngoại ngữ ({@code nth.n1 = false}):
     *           UPSERT vào {@code xt_diemcongxetuyen} với
     *           {@code diemCC = diemCong}, khóa duy nhất
     *           {@code dc_keys = {cccd}_{manganh}_{matohop}}.</li>
     *     </ul>
     *   </li>
     * </ol>
     *
     * <p>Toàn bộ ghi DB được thực hiện trong 2 phiên batch riêng biệt
     * (một cho UPDATE N1_CC, một cho UPSERT DiemCong) để tối ưu MySQL.
     *
     * @param file File Excel (*.xlsx / *.xls) có các cột: CCCD, Điểm quy đổi, Điểm cộng.
     * @return Danh sách chuỗi lỗi (rỗng nếu thành công hoàn toàn).
     */
    public List<String> processIELTSImport(File file) {

        // ── Bước 1: Đọc & validate file Excel ──────────────────────────────
        List<String> errors = new ArrayList<>();

        List<IeltsImportDTO> rows = new ArrayList<>();
        List<String> parseErrors = baseImport.importFromExcel(
                file,
                IeltsImportDTO.class,
                dto -> dto,             // identity mapper – validate first, process later
                parsed -> rows.addAll(parsed),
                dto -> validateRow(dto)
        );

        if (!parseErrors.isEmpty()) {
            return parseErrors;         // ALL-OR-NOTHING: dừng sớm nếu có lỗi format
        }

        // ── Bước 2: Chuẩn bị danh sách batch ──────────────────────────────
        /*
         * N1 Update batch:  (session, cccd, diemQd)
         * DiemCong Upsert batch: List<DiemCong> entities
         */
        List<N1UpdateCommand>  n1Updates    = new ArrayList<>();
        List<DiemCong>         dcUpserts    = new ArrayList<>();

        for (IeltsImportDTO dto : rows) {
            String cccd = dto.getCccd().trim();

            // ── Validation tập hợp cha ─────────────────────────────────────
            Optional<DiemThiXetTuyen> optDiem = diemThiDAO.findByCccd(cccd);
            if (optDiem.isEmpty()) {
                errors.add("[SKIP] CCCD=" + cccd + " không tồn tại trong xt_diemthixettuyen (tập hợp cha).");
                continue;
            }

            List<NganhToHop> toHopList = nguyenVongDAO.findNganhToHopByCccd(cccd);
            if (toHopList.isEmpty()) {
                errors.add("[SKIP] CCCD=" + cccd + " không có nguyện vọng trong xt_nguyenvongxettuyen.");
                continue;
            }

            DiemThiXetTuyen dt = optDiem.get();

            // ── Duyệt từng tổ hợp ─────────────────────────────────────────
            for (NganhToHop nth : toHopList) {

                // ── Subset Check: thí sinh phải có đủ điểm môn văn hóa ────
                if (!passesSubsetCheck(dt, nth)) {
                    continue;   // Thiếu điểm môn bắt buộc → bỏ qua tổ hợp này
                }

                String manganh  = nth.getNganh().getManganh();
                String matohop  = nth.getToHopMon().getMatohop();

                boolean coNgoaiNgu = Boolean.TRUE.equals(nth.getN1());

                if (coNgoaiNgu) {
                    // ── Case A: Tổ hợp CÓ Ngoại ngữ → ghi UPDATE N1_CC ───
                    BigDecimal diemQd = coalesceZero(dto.getDiemQd());
                    n1Updates.add(new N1UpdateCommand(cccd, diemQd));

                } else {
                    // ── Case B: Tổ hợp KHÔNG có Ngoại ngữ → UPSERT DiemCong
                    String dcKey = cccd + "_" + manganh + "_" + matohop;
                    DiemCong dc = buildDiemCong(dt.getThiSinh(), manganh, matohop, dto.getDiemCong(), dcKey);
                    dcUpserts.add(dc);
                }
            }
        }

        // ── Bước 3: Batch flush DB ─────────────────────────────────────────
        if (!n1Updates.isEmpty()) {
            List<String> batchErrors = flushN1Updates(n1Updates);
            errors.addAll(batchErrors);
        }

        if (!dcUpserts.isEmpty()) {
            List<String> batchErrors = flushDiemCongUpserts(dcUpserts);
            errors.addAll(batchErrors);
        }

        return errors;
    }

    // ──────────────────────────────────────────────────────────────────────────
    //  PRIVATE — Validation
    // ──────────────────────────────────────────────────────────────────────────

    /**
     * Validate 1 dòng Excel trước khi xử lý.
     * Trả về {@code null} nếu hợp lệ, hoặc chuỗi mô tả lỗi.
     */
    private String validateRow(IeltsImportDTO dto) {
        if (dto.getCccd() == null || dto.getCccd().trim().isEmpty()) {
            return "Bắt buộc phải có CCCD";
        }
        if (dto.getDiemQd() != null) {
            BigDecimal qd = dto.getDiemQd();
            if (qd.compareTo(BigDecimal.ZERO) < 0 || qd.compareTo(new BigDecimal("10")) > 0) {
                return "Điểm quy đổi phải nằm trong khoảng 0–10 (CCCD=" + dto.getCccd() + ")";
            }
        }
        if (dto.getDiemCong() != null) {
            BigDecimal dc = dto.getDiemCong();
            if (dc.compareTo(BigDecimal.ZERO) < 0 || dc.compareTo(new BigDecimal("10")) > 0) {
                return "Điểm cộng phải nằm trong khoảng 0–10 (CCCD=" + dto.getCccd() + ")";
            }
        }
        return null;
    }

    // ──────────────────────────────────────────────────────────────────────────
    //  PRIVATE — Subset Check
    // ──────────────────────────────────────────────────────────────────────────

    /**
     * Kiểm tra xem thí sinh có đủ điểm môn văn hóa mà tổ hợp yêu cầu không.
     *
     * <p>Quy tắc: nếu {@code nth.monX = true} (tổ hợp yêu cầu môn X)
     * thì {@code dt.monX} phải &gt; 0 (thí sinh có điểm môn đó).
     * Ngoại ngữ (N1) được kiểm tra riêng qua cột {@code n1}.
     *
     * @param dt  Bản ghi điểm thi của thí sinh (tập hợp cha).
     * @param nth Tổ hợp xét tuyển cần kiểm tra.
     * @return {@code true} nếu thí sinh hội đủ điều kiện.
     */
    private boolean passesSubsetCheck(DiemThiXetTuyen dt, NganhToHop nth) {
        // Toán
        if (Boolean.TRUE.equals(nth.getTo()) && !isPositive(dt.getTo()))   return false;
        // Ngữ văn
        if (Boolean.TRUE.equals(nth.getVa()) && !isPositive(dt.getVa()))   return false;
        // Vật lý
        if (Boolean.TRUE.equals(nth.getLi()) && !isPositive(dt.getLi()))   return false;
        // Hóa học
        if (Boolean.TRUE.equals(nth.getHo()) && !isPositive(dt.getHo()))   return false;
        // Sinh học
        if (Boolean.TRUE.equals(nth.getSi()) && !isPositive(dt.getSi()))   return false;
        // Lịch sử
        if (Boolean.TRUE.equals(nth.getSu()) && !isPositive(dt.getSu()))   return false;
        // Địa lý
        if (Boolean.TRUE.equals(nth.getDi()) && !isPositive(dt.getDi()))   return false;
        // Tin học
        if (Boolean.TRUE.equals(nth.getTi()) && !isPositive(dt.getTi()))   return false;
        // GDCD/KTPL
        if (Boolean.TRUE.equals(nth.getKtpl()) && !isPositive(dt.getKtpl())) return false;
        return true;
    }

    // ──────────────────────────────────────────────────────────────────────────
    //  PRIVATE — Batch: UPDATE N1_CC
    // ──────────────────────────────────────────────────────────────────────────

    /**
     * Batch UPDATE {@code N1_CC} = GREATEST(N1_THI, diemQd) cho danh sách thí sinh.
     *
     * <p>Dùng Hibernate native SQL để tận dụng {@code GREATEST()} của MySQL,
     * đồng thời flush theo batch để kiểm soát bộ nhớ.
     *
     * @param commands Danh sách lệnh UPDATE (cccd + điểm quy đổi).
     * @return Danh sách lỗi (rỗng nếu thành công).
     */
    private List<String> flushN1Updates(List<N1UpdateCommand> commands) {
        List<String> errors = new ArrayList<>();
        Session session = null;
        Transaction tx  = null;

        try {
            session = HibernateUtil.getSessionFactory().openSession();
            tx = session.beginTransaction();

            int count = 0;
            for (N1UpdateCommand cmd : commands) {
                /*
                 * SQL tương đương:
                 *   UPDATE xt_diemthixettuyen
                 *   SET    N1_CC = GREATEST(COALESCE(N1_THI, 0), :diemQd)
                 *   WHERE  cccd  = :cccd
                 *
                 * COALESCE bảo vệ trường hợp N1_THI = NULL.
                 */
                session.createNativeMutationQuery(
                        "UPDATE xt_diemthixettuyen " +
                        "SET N1_CC = GREATEST(COALESCE(N1_THI, 0), :diemQd) " +
                        "WHERE cccd = :cccd"
                )
                .setParameter("diemQd", cmd.diemQd())
                .setParameter("cccd",   cmd.cccd())
                .executeUpdate();

                if (++count % BATCH_SIZE == 0) {
                    session.flush();
                }
            }

            tx.commit();

        } catch (Exception e) {
            if (tx != null && tx.isActive()) {
                try { tx.rollback(); } catch (Exception rb) {
                    System.err.println("Rollback N1_CC thất bại: " + rb.getMessage());
                }
            }
            e.printStackTrace();
            errors.add("Lỗi batch UPDATE N1_CC: " + e.getMessage());

        } finally {
            if (session != null && session.isOpen()) session.close();
        }

        return errors;
    }

    // ──────────────────────────────────────────────────────────────────────────
    //  PRIVATE — Batch: UPSERT DiemCong
    // ──────────────────────────────────────────────────────────────────────────

    /**
     * Bulk UPSERT vào {@code xt_diemcongxetuyen}.
     *
     * <p>Dùng MySQL {@code INSERT INTO ... ON DUPLICATE KEY UPDATE} để đảm bảo
     * idempotent (chạy import nhiều lần vẫn an toàn).
     * Khóa duy nhất {@code dc_keys} = {@code {cccd}_{manganh}_{matohop}}.
     *
     * @param entities Danh sách {@link DiemCong} cần UPSERT.
     * @return Danh sách lỗi (rỗng nếu thành công).
     */
    private List<String> flushDiemCongUpserts(List<DiemCong> entities) {
        List<String> errors = new ArrayList<>();
        Session session = null;
        Transaction tx  = null;

        try {
            session = HibernateUtil.getSessionFactory().openSession();
            tx = session.beginTransaction();

            int count = 0;
            for (DiemCong dc : entities) {
                /*
                 * UPSERT MySQL:
                 *   INSERT INTO xt_diemcongxetuyen (ts_cccd, manganh, matohop, diemCC, dc_keys)
                 *   VALUES (:cccd, :manganh, :matohop, :diemCC, :dcKeys)
                 *   ON DUPLICATE KEY UPDATE diemCC = VALUES(diemCC)
                 *
                 * Chỉ cập nhật diemCC khi key trùng; các cột khác giữ nguyên.
                 */
                session.createNativeMutationQuery(
                        "INSERT INTO xt_diemcongxetuyen (ts_cccd, manganh, matohop, diemCC, dc_keys) " +
                        "VALUES (:cccd, :manganh, :matohop, :diemCC, :dcKeys) " +
                        "ON DUPLICATE KEY UPDATE diemCC = VALUES(diemCC)"
                )
                .setParameter("cccd",    dc.getThiSinh().getCccd())
                .setParameter("manganh", dc.getManganh())
                .setParameter("matohop", dc.getMatohop())
                .setParameter("diemCC",  dc.getDiemCC())
                .setParameter("dcKeys",  dc.getDcKeys())
                .executeUpdate();

                if (++count % BATCH_SIZE == 0) {
                    session.flush();
                }
            }

            tx.commit();

        } catch (Exception e) {
            if (tx != null && tx.isActive()) {
                try { tx.rollback(); } catch (Exception rb) {
                    System.err.println("Rollback DiemCong UPSERT thất bại: " + rb.getMessage());
                }
            }
            e.printStackTrace();
            errors.add("Lỗi batch UPSERT xt_diemcongxetuyen: " + e.getMessage());

        } finally {
            if (session != null && session.isOpen()) session.close();
        }

        return errors;
    }

    // ──────────────────────────────────────────────────────────────────────────
    //  PRIVATE — Helpers
    // ──────────────────────────────────────────────────────────────────────────

    /**
     * Xây dựng entity {@link DiemCong} từ các tham số.
     * diemCC được làm tròn 2 chữ số thập phân. NULL → 0.
     */
    private DiemCong buildDiemCong(ThiSinh thiSinh,
                                   String manganh,
                                   String matohop,
                                   BigDecimal rawDiemCong,
                                   String dcKey) {
        DiemCong dc = new DiemCong();
        dc.setThiSinh(thiSinh);
        dc.setManganh(manganh);
        dc.setMatohop(matohop);
        dc.setDiemCC(coalesceZero(rawDiemCong).setScale(2, RoundingMode.HALF_UP));
        dc.setDcKeys(dcKey);
        return dc;
    }

    /** Trả về {@code true} nếu giá trị không null và > 0. */
    private boolean isPositive(BigDecimal value) {
        return value != null && value.compareTo(BigDecimal.ZERO) > 0;
    }

    /**
     * Tương đương {@code COALESCE(value, 0)} — tránh NullPointerException khi so sánh điểm.
     */
    private BigDecimal coalesceZero(BigDecimal value) {
        return value != null ? value : BigDecimal.ZERO;
    }

    // ──────────────────────────────────────────────────────────────────────────
    //  PRIVATE — Inner Records / Value Objects
    // ──────────────────────────────────────────────────────────────────────────

    /**
     * Lệnh UPDATE N1_CC gọn nhẹ, đóng gói (cccd, diemQd) để truyền vào batch.
     */
    private record N1UpdateCommand(String cccd, BigDecimal diemQd) {}
}
