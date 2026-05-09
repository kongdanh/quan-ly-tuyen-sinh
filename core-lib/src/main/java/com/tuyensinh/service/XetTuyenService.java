package com.tuyensinh.service;

import com.tuyensinh.dao.DiemCongDAO;
import com.tuyensinh.dao.DiemThiXetTuyenDAO;
import com.tuyensinh.dao.NguyenVongDAO;
import com.tuyensinh.dto.DgnlVsatRowDTO;
import com.tuyensinh.dto.IeltsImportDTO;
import com.tuyensinh.model.DiemCong;
import com.tuyensinh.model.DiemThiXetTuyen;
import com.tuyensinh.model.NganhToHop;
import com.tuyensinh.model.ThiSinh;
import com.tuyensinh.util.ExcelReaderUtil;
import com.tuyensinh.util.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;

import java.io.File;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

/**
 * Service xử lý toàn bộ luồng nghiệp vụ Xét tuyển.
 * <ul>
 *   <li>{@link #processIELTSImport(File)}  — Chứng chỉ IELTS / ngoại ngữ</li>
 *   <li>{@link #processDgnlVsatImport(File)} — File đa-sheet DGNL + VSAT</li>
 * </ul>
 */
public class XetTuyenService {

    // ── Dependencies ────────────────────────────────────────────────────────────
    private final DiemThiXetTuyenDAO diemThiDAO   = new DiemThiXetTuyenDAO();
    private final NguyenVongDAO      nguyenVongDAO = new NguyenVongDAO();
    private final DiemCongDAO        diemCongDAO   = new DiemCongDAO();
    private final BaseImportService<IeltsImportDTO, IeltsImportDTO> baseImport = new BaseImportService<>();

    private static final int BATCH_SIZE = 50;

    // ── Tên sheet (case-insensitive so sánh qua toLowerCase) ───────────────────
    private static final String SHEET_DGNL = "dgnl";
    private static final String SHEET_VSAT = "vsat";

    // ── Mã môn VSAT → tên cột DB ───────────────────────────────────────────────
    // Mỗi môn có thể mang 2 mã khác nhau tuỳ đơn vị tổ chức thi:
    //   Mã chuẩn (TO_VS / LI_VS / VA_VS / N1_VS) — do VSAT quy định.
    //   Mã ngắn  (M1 / M2 / M3 / M8)             — do một số trường tự đặt.
    // Cả hai đều ánh xạ về cùng cột trong xt_diemthixettuyen.
    //
    //   Excel MAMONTHI │ Cột DB   │ Môn
    //   ───────────────┼──────────┼────────────
    //   TO_VS, M1      │ `TO`     │ Toán       (backtick: reserved keyword MySQL)
    //   LI_VS, M2      │ LI       │ Vật lý
    //   VA_VS, M3      │ VA       │ Ngữ văn
    //   N1_VS, M8      │ N1_THI   │ Ngoại ngữ (điểm thi)
    private static final Map<String, String> VSAT_MON_MAP;
    static {
        VSAT_MON_MAP = new LinkedHashMap<>();
        // Mã chuẩn VSAT
        VSAT_MON_MAP.put("TO_VS", "TO");
        VSAT_MON_MAP.put("LI_VS", "LI");
        VSAT_MON_MAP.put("VA_VS", "VA");
        VSAT_MON_MAP.put("N1_VS", "N1_THI");
        // Alias ngắn (một số trường dùng mã Mx thay vì XX_VS)
        VSAT_MON_MAP.put("M1",    "TO");
        VSAT_MON_MAP.put("M2",    "LI");
        VSAT_MON_MAP.put("M3",    "VA");
        VSAT_MON_MAP.put("M8",    "N1_THI");
    }

    // ══════════════════════════════════════════════════════════════════════════════
    //  1. IELTS IMPORT (từ bài trước — giữ nguyên)
    // ══════════════════════════════════════════════════════════════════════════════

    public List<String> processIELTSImport(File file) {
        List<String> errors = new ArrayList<>();
        List<IeltsImportDTO> rows = new ArrayList<>();

        List<String> parseErrors = baseImport.importFromExcel(
                file, IeltsImportDTO.class,
                dto -> dto,
                parsed -> rows.addAll(parsed),
                dto -> validateIeltsRow(dto)
        );
        if (!parseErrors.isEmpty()) return parseErrors;

        List<N1UpdateCommand> n1Updates = new ArrayList<>();
        List<DiemCong>        dcUpserts = new ArrayList<>();

        for (IeltsImportDTO dto : rows) {
            String cccd = dto.getCccd().trim();
            Optional<DiemThiXetTuyen> optDiem = diemThiDAO.findByCccd(cccd);
            if (optDiem.isEmpty()) {
                errors.add("[SKIP] CCCD=" + cccd + " không tồn tại trong xt_diemthixettuyen.");
                continue;
            }
            List<NganhToHop> toHopList = nguyenVongDAO.findNganhToHopByCccd(cccd);
            if (toHopList.isEmpty()) {
                errors.add("[SKIP] CCCD=" + cccd + " không có nguyện vọng trong xt_nguyenvongxettuyen.");
                continue;
            }
            DiemThiXetTuyen dt = optDiem.get();
            for (NganhToHop nth : toHopList) {
                if (!passesSubsetCheck(dt, nth)) continue;
                String manganh = nth.getNganh().getManganh();
                String matohop = nth.getToHopMon().getMatohop();
                if (Boolean.TRUE.equals(nth.getN1())) {
                    n1Updates.add(new N1UpdateCommand(cccd, coalesceZero(dto.getDiemQd())));
                } else {
                    dcUpserts.add(buildDiemCong(dt.getThiSinh(), manganh, matohop,
                            dto.getDiemCong(), cccd + "_" + manganh + "_" + matohop));
                }
            }
        }
        if (!n1Updates.isEmpty()) errors.addAll(flushN1Updates(n1Updates));
        if (!dcUpserts.isEmpty()) errors.addAll(flushDiemCongUpserts(dcUpserts));
        return errors;
    }

    // ══════════════════════════════════════════════════════════════════════════════
    //  2. DGNL + VSAT IMPORT — entry point
    // ══════════════════════════════════════════════════════════════════════════════

    /**
     * Điểm vào chính: đọc file Excel đa-sheet và phân phối sang
     * {@link #processDgnlSheet} / {@link #processVsatSheet}.
     *
     * <p>File có thể chứa 1 hoặc cả 2 sheet "DGNL" và "VSAT"
     * (tên không phân biệt hoa/thường). Sheet không khớp sẽ được bỏ qua.
     *
     * @param file File Excel nhiều sheet.
     * @return Tổng hợp lỗi từ tất cả sheet; rỗng nếu thành công hoàn toàn.
     */
    public List<String> processDgnlVsatImport(File file) {
        List<String> errors = new ArrayList<>();
        Map<String, List<DgnlVsatRowDTO>> allSheets;

        // ── Đọc tất cả sheet bằng ExcelReaderUtil mở rộng ─────────────────────
        try {
            allSheets = ExcelReaderUtil.readAllSheets(file, DgnlVsatRowDTO.class);
        } catch (Exception e) {
            errors.add("Lỗi đọc file Excel: " + e.getMessage());
            return errors;
        }

        if (allSheets.isEmpty()) {
            errors.add("File Excel không có sheet nào hợp lệ.");
            return errors;
        }

        boolean foundAny = false;
        for (Map.Entry<String, List<DgnlVsatRowDTO>> entry : allSheets.entrySet()) {
            String sheetName = entry.getKey();
            List<DgnlVsatRowDTO> rows = entry.getValue();

            // Gán tên sheet vào từng DTO để dùng khi log lỗi
            rows.forEach(r -> r.setSheetName(sheetName));

            if (sheetName.equalsIgnoreCase(SHEET_DGNL)) {
                foundAny = true;
                errors.addAll(processDgnlSheet(rows));
            } else if (sheetName.equalsIgnoreCase(SHEET_VSAT)) {
                foundAny = true;
                errors.addAll(processVsatSheet(rows));
            } else {
                System.out.println("[XetTuyenService] Bỏ qua sheet không nhận dạng được: '" + sheetName + "'");
            }
        }

        if (!foundAny) {
            errors.add("Không tìm thấy sheet 'DGNL' hoặc 'VSAT' trong file.");
        }
        return errors;
    }

    // ══════════════════════════════════════════════════════════════════════════════
    //  3. DGNL SHEET — ghi thẳng vào cột NL1
    // ══════════════════════════════════════════════════════════════════════════════

    /**
     * Xử lý sheet DGNL.
     *
     * <p><b>Logic:</b>
     * <ol>
     *   <li>Mỗi dòng: MAMONTHI phải là "DGNL"; DIEM thang 1200.</li>
     *   <li>CCCD (cột CMND) phải tồn tại trong {@code xt_diemthixettuyen}.</li>
     *   <li>1 thí sinh có thể có nhiều dòng (nhiều đợt thi) →
     *       SQL {@code GREATEST(COALESCE(NL1,0), :diem)} đảm bảo chỉ ghi
     *       nếu điểm mới cao hơn điểm đã có.</li>
     *   <li>Điểm ghi là điểm thô (thang 1200) — KHÔNG quy đổi,
     *       để giữ đúng semantic cột NL1 trong DB.</li>
     * </ol>
     *
     * @param rows Danh sách DTO từ sheet DGNL.
     * @return Danh sách lỗi.
     */
    List<String> processDgnlSheet(List<DgnlVsatRowDTO> rows) {
        List<String> errors      = new ArrayList<>();
        List<ScoreUpdateCmd> cmds = new ArrayList<>();

        int rowNum = 2; // Excel row index (1 = header)
        for (DgnlVsatRowDTO dto : rows) {

            // ── Validate ───────────────────────────────────────────────────────
            String err = validateDgnlVsatRow(dto, "DGNL");
            if (err != null) { errors.add("DGNL dòng " + rowNum + ": " + err); rowNum++; continue; }

            if (!"DGNL".equalsIgnoreCase(dto.getMamonthi())) {
                // Sheet DGNL đôi khi có dòng rác với mã môn khác → bỏ qua nhẹ nhàng
                System.out.println("[DGNL] Bỏ qua dòng " + rowNum + " MAMONTHI=" + dto.getMamonthi());
                rowNum++; continue;
            }

            String cccd = dto.getCmnd().trim();
            if (!existsInDiemThi(cccd)) {
                errors.add("[DGNL SKIP] CCCD=" + cccd + " không tồn tại trong xt_diemthixettuyen.");
                rowNum++; continue;
            }

            // Điểm thô thang 1200 — ghi thẳng vào NL1
            BigDecimal diem = coalesceZero(dto.getDiem()).setScale(2, RoundingMode.HALF_UP);
            cmds.add(new ScoreUpdateCmd(cccd, "NL1", diem));
            rowNum++;
        }

        if (!cmds.isEmpty()) errors.addAll(flushColumnUpdates(cmds, "DGNL"));
        return errors;
    }

    // ══════════════════════════════════════════════════════════════════════════════
    //  4. VSAT SHEET — quy đổi thang, GREATEST so sánh
    // ══════════════════════════════════════════════════════════════════════════════

    /**
     * Xử lý sheet VSAT.
     *
     * <p><b>Logic:</b>
     * <ol>
     *   <li>Mỗi dòng = 1 môn thi. MAMONTHI ∈ {TO_VS, LI_VS, VA_VS, N1_VS}.
     *       Mã khác (M1, M2, M8...) → log và skip.</li>
     *   <li>Quy đổi điểm: {@code diemQuyDoi = DIEM / THANGDIEM * 10},
     *       làm tròn 2 chữ số thập phân. Nếu THANGDIEM null/0 → mặc định 150.</li>
     *   <li>CCCD phải tồn tại trong {@code xt_diemthixettuyen}.</li>
     *   <li>UPDATE: {@code SET col = GREATEST(COALESCE(col,0), :diem)}
     *       — chỉ ghi nếu điểm mới cao hơn điểm cũ.</li>
     * </ol>
     *
     * @param rows Danh sách DTO từ sheet VSAT.
     * @return Danh sách lỗi.
     */
    List<String> processVsatSheet(List<DgnlVsatRowDTO> rows) {
        List<String> errors      = new ArrayList<>();
        List<ScoreUpdateCmd> cmds = new ArrayList<>();

        // Thang điểm mặc định VSAT = 150
        BigDecimal DEFAULT_THANG = new BigDecimal("150");

        int rowNum = 2;
        for (DgnlVsatRowDTO dto : rows) {

            // ── Validate ───────────────────────────────────────────────────────
            String err = validateDgnlVsatRow(dto, "VSAT");
            if (err != null) { errors.add("VSAT dòng " + rowNum + ": " + err); rowNum++; continue; }

            String maMon = dto.getMamonthi() == null ? "" : dto.getMamonthi().trim().toUpperCase();
            String dbCol = VSAT_MON_MAP.get(maMon);
            if (dbCol == null) {
                // Mã môn không nằm trong danh sách cần xử lý → bỏ qua
                System.out.println("[VSAT] Bỏ qua dòng " + rowNum
                        + " MAMONTHI=" + maMon + " (không ánh xạ)");
                rowNum++; continue;
            }

            String cccd = dto.getCmnd().trim();
            if (!existsInDiemThi(cccd)) {
                errors.add("[VSAT SKIP] CCCD=" + cccd + " không tồn tại trong xt_diemthixettuyen.");
                rowNum++; continue;
            }

            // ── Quy đổi điểm về thang 10 ──────────────────────────────────────
            BigDecimal thang  = (dto.getThangdiem() != null
                    && dto.getThangdiem().compareTo(BigDecimal.ZERO) > 0)
                    ? dto.getThangdiem() : DEFAULT_THANG;
            BigDecimal diemQD = coalesceZero(dto.getDiem())
                    .multiply(BigDecimal.TEN)
                    .divide(thang, 2, RoundingMode.HALF_UP);

            cmds.add(new ScoreUpdateCmd(cccd, dbCol, diemQD));
            rowNum++;
        }

        if (!cmds.isEmpty()) errors.addAll(flushColumnUpdates(cmds, "VSAT"));
        return errors;
    }

    // ══════════════════════════════════════════════════════════════════════════════
    //  5. BATCH FLUSH — dùng chung cho DGNL và VSAT
    // ══════════════════════════════════════════════════════════════════════════════

    /**
     * Batch UPDATE nhiều cột khác nhau trong {@code xt_diemthixettuyen}.
     *
     * <p>SQL template (cột được truyền qua string ghép — an toàn vì đã
     * whitelist qua {@link #VSAT_MON_MAP} + hằng "NL1"):
     * <pre>
     * UPDATE xt_diemthixettuyen
     * SET &lt;col&gt; = GREATEST(COALESCE(&lt;col&gt;, 0), :diem)
     * WHERE cccd = :cccd
     * </pre>
     *
     * @param cmds   Danh sách lệnh UPDATE.
     * @param source Tên nguồn (DGNL / VSAT) — chỉ dùng để log.
     * @return Danh sách lỗi.
     */
    private List<String> flushColumnUpdates(List<ScoreUpdateCmd> cmds, String source) {
        List<String> errors = new ArrayList<>();
        Session     session = null;
        Transaction tx      = null;

        try {
            session = HibernateUtil.getSessionFactory().openSession();
            tx      = session.beginTransaction();
            int count = 0;

            for (ScoreUpdateCmd cmd : cmds) {
                /*
                 * Whitelist cột để tránh SQL Injection (col chỉ đến từ VSAT_MON_MAP hoặc "NL1").
                 * Tên cột có thể chứa ký tự backtick (ví dụ `TO`) — thêm backtick bọc ngoài để
                 * MySQL xử lý đúng tên cột reserved keyword.
                 */
                String colSafe = "`" + cmd.dbCol() + "`";
                String sql =
                        "UPDATE xt_diemthixettuyen " +
                        "SET " + colSafe + " = GREATEST(COALESCE(" + colSafe + ", 0), :diem) " +
                        "WHERE cccd = :cccd";

                session.createNativeMutationQuery(sql)
                        .setParameter("diem", cmd.diem())
                        .setParameter("cccd", cmd.cccd())
                        .executeUpdate();

                if (++count % BATCH_SIZE == 0) session.flush();
            }

            tx.commit();
            System.out.println("[" + source + "] Batch UPDATE hoàn tất: " + cmds.size() + " lệnh.");

        } catch (Exception e) {
            if (tx != null && tx.isActive()) {
                try { tx.rollback(); } catch (Exception rb) {
                    System.err.println("Rollback " + source + " thất bại: " + rb.getMessage());
                }
            }
            e.printStackTrace();
            errors.add("Lỗi batch UPDATE " + source + ": " + e.getMessage());
        } finally {
            if (session != null && session.isOpen()) session.close();
        }
        return errors;
    }

    // ══════════════════════════════════════════════════════════════════════════════
    //  6. IELTS HELPERS (giữ nguyên từ bài trước)
    // ══════════════════════════════════════════════════════════════════════════════

    private List<String> flushN1Updates(List<N1UpdateCommand> commands) {
        List<String> errors = new ArrayList<>();
        Session session = null;
        Transaction tx  = null;
        try {
            session = HibernateUtil.getSessionFactory().openSession();
            tx = session.beginTransaction();
            int count = 0;
            for (N1UpdateCommand cmd : commands) {
                session.createNativeMutationQuery(
                        "UPDATE xt_diemthixettuyen " +
                        "SET N1_CC = GREATEST(COALESCE(N1_THI, 0), :diemQd) " +
                        "WHERE cccd = :cccd")
                        .setParameter("diemQd", cmd.diemQd())
                        .setParameter("cccd",   cmd.cccd())
                        .executeUpdate();
                if (++count % BATCH_SIZE == 0) session.flush();
            }
            tx.commit();
        } catch (Exception e) {
            if (tx != null && tx.isActive()) try { tx.rollback(); } catch (Exception ignored) {}
            e.printStackTrace();
            errors.add("Lỗi batch UPDATE N1_CC: " + e.getMessage());
        } finally {
            if (session != null && session.isOpen()) session.close();
        }
        return errors;
    }

    private List<String> flushDiemCongUpserts(List<DiemCong> entities) {
        List<String> errors = new ArrayList<>();
        Session session = null;
        Transaction tx  = null;
        try {
            session = HibernateUtil.getSessionFactory().openSession();
            tx = session.beginTransaction();
            int count = 0;
            for (DiemCong dc : entities) {
                session.createNativeMutationQuery(
                        "INSERT INTO xt_diemcongxetuyen (ts_cccd, manganh, matohop, diemCC, dc_keys) " +
                        "VALUES (:cccd, :manganh, :matohop, :diemCC, :dcKeys) " +
                        "ON DUPLICATE KEY UPDATE diemCC = VALUES(diemCC)")
                        .setParameter("cccd",    dc.getThiSinh().getCccd())
                        .setParameter("manganh", dc.getManganh())
                        .setParameter("matohop", dc.getMatohop())
                        .setParameter("diemCC",  dc.getDiemCC())
                        .setParameter("dcKeys",  dc.getDcKeys())
                        .executeUpdate();
                if (++count % BATCH_SIZE == 0) session.flush();
            }
            tx.commit();
        } catch (Exception e) {
            if (tx != null && tx.isActive()) try { tx.rollback(); } catch (Exception ignored) {}
            e.printStackTrace();
            errors.add("Lỗi batch UPSERT xt_diemcongxetuyen: " + e.getMessage());
        } finally {
            if (session != null && session.isOpen()) session.close();
        }
        return errors;
    }

    // ══════════════════════════════════════════════════════════════════════════════
    //  7. VALIDATION HELPERS
    // ══════════════════════════════════════════════════════════════════════════════

    private String validateIeltsRow(IeltsImportDTO dto) {
        if (dto.getCccd() == null || dto.getCccd().trim().isEmpty()) return "Bắt buộc phải có CCCD";
        if (dto.getDiemQd() != null) {
            BigDecimal v = dto.getDiemQd();
            if (v.compareTo(BigDecimal.ZERO) < 0 || v.compareTo(new BigDecimal("10")) > 0)
                return "Điểm quy đổi phải 0–10 (CCCD=" + dto.getCccd() + ")";
        }
        if (dto.getDiemCong() != null) {
            BigDecimal v = dto.getDiemCong();
            if (v.compareTo(BigDecimal.ZERO) < 0 || v.compareTo(new BigDecimal("10")) > 0)
                return "Điểm cộng phải 0–10 (CCCD=" + dto.getCccd() + ")";
        }
        return null;
    }

    private String validateDgnlVsatRow(DgnlVsatRowDTO dto, String source) {
        if (dto.getCmnd() == null || dto.getCmnd().trim().isEmpty())
            return "Bắt buộc phải có CMND/CCCD";
        if (dto.getMamonthi() == null || dto.getMamonthi().trim().isEmpty())
            return "Bắt buộc phải có MAMONTHI";
        if (dto.getDiem() == null)
            return "Bắt buộc phải có DIEM (CCCD=" + dto.getCmnd() + ")";
        if (dto.getDiem().compareTo(BigDecimal.ZERO) < 0)
            return "DIEM không được âm (CCCD=" + dto.getCmnd() + ")";
        return null;
    }

    /** Kiểm tra nhanh cccd có trong xt_diemthixettuyen không — dùng Optional từ DAO có sẵn. */
    private boolean existsInDiemThi(String cccd) {
        return diemThiDAO.findByCccd(cccd).isPresent();
    }

    // ══════════════════════════════════════════════════════════════════════════════
    //  8. SHARED HELPERS
    // ══════════════════════════════════════════════════════════════════════════════

    private boolean passesSubsetCheck(DiemThiXetTuyen dt, NganhToHop nth) {
        if (Boolean.TRUE.equals(nth.getTo())   && !isPositive(dt.getTo()))   return false;
        if (Boolean.TRUE.equals(nth.getVa())   && !isPositive(dt.getVa()))   return false;
        if (Boolean.TRUE.equals(nth.getLi())   && !isPositive(dt.getLi()))   return false;
        if (Boolean.TRUE.equals(nth.getHo())   && !isPositive(dt.getHo()))   return false;
        if (Boolean.TRUE.equals(nth.getSi())   && !isPositive(dt.getSi()))   return false;
        if (Boolean.TRUE.equals(nth.getSu())   && !isPositive(dt.getSu()))   return false;
        if (Boolean.TRUE.equals(nth.getDi())   && !isPositive(dt.getDi()))   return false;
        if (Boolean.TRUE.equals(nth.getTi())   && !isPositive(dt.getTi()))   return false;
        if (Boolean.TRUE.equals(nth.getKtpl()) && !isPositive(dt.getKtpl())) return false;
        return true;
    }

    private DiemCong buildDiemCong(ThiSinh ts, String manganh, String matohop,
                                   BigDecimal rawDiem, String dcKey) {
        DiemCong dc = new DiemCong();
        dc.setThiSinh(ts);
        dc.setManganh(manganh);
        dc.setMatohop(matohop);
        dc.setDiemCC(coalesceZero(rawDiem).setScale(2, RoundingMode.HALF_UP));
        dc.setDcKeys(dcKey);
        return dc;
    }

    private boolean isPositive(BigDecimal v) {
        return v != null && v.compareTo(BigDecimal.ZERO) > 0;
    }

    private BigDecimal coalesceZero(BigDecimal v) {
        return v != null ? v : BigDecimal.ZERO;
    }

    // ══════════════════════════════════════════════════════════════════════════════
    //  9. VALUE OBJECTS
    // ══════════════════════════════════════════════════════════════════════════════

    /** Lệnh UPDATE 1 cột điểm cho 1 thí sinh (dùng chung DGNL + VSAT). */
    private record ScoreUpdateCmd(String cccd, String dbCol, BigDecimal diem) {}

    /** Lệnh UPDATE N1_CC cho IELTS import. */
    private record N1UpdateCommand(String cccd, BigDecimal diemQd) {}
}
