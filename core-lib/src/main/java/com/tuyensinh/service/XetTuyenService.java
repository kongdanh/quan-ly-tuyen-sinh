package com.tuyensinh.service;

import com.tuyensinh.dao.BangQuyDoiDAO;
import com.tuyensinh.dao.DiemCongDAO;
import com.tuyensinh.dao.DiemThiXetTuyenDAO;
import com.tuyensinh.dao.NguyenVongDAO;
import com.tuyensinh.dto.DgnlVsatRowDTO;
import com.tuyensinh.dto.IeltsImportDTO;
import com.tuyensinh.model.BangQuyDoi;
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
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.*;

/**
 * Service xử lý toàn bộ luồng nghiệp vụ Xét tuyển.
 * <ul>
 * <li>{@link #processIELTSImport(File)} — Chứng chỉ IELTS / ngoại ngữ</li>
 * <li>{@link #processDgnlVsatImport(File)} — File đa-sheet DGNL + VSAT</li>
 * </ul>
 */
public class XetTuyenService {

    // ── Dependencies ────────────────────────────────────────────────────────────
    private final DiemThiXetTuyenDAO diemThiDAO = new DiemThiXetTuyenDAO();
    private final NguyenVongDAO nguyenVongDAO = new NguyenVongDAO();
    private final DiemCongDAO diemCongDAO = new DiemCongDAO();
    private final BangQuyDoiDAO bangQuyDoiDAO = new BangQuyDoiDAO();
    private final BaseImportService<IeltsImportDTO, IeltsImportDTO> baseImport = new BaseImportService<>();

    /** Batch size theo yêu cầu performance: 100 rows/flush. */
    private static final int BATCH_SIZE = 100;

    // ── Tên sheet (case-insensitive so sánh qua toLowerCase) ───────────────────
    private static final String SHEET_DGNL = "dgnl";
    private static final String SHEET_VSAT = "vsat";

    // ── Mã môn VSAT → tên cột DB ───────────────────────────────────────────────
    // Mỗi môn có thể mang 2 mã khác nhau tuỳ đơn vị tổ chức thi:
    // Mã chuẩn (TO_VS / LI_VS / VA_VS / N1_VS) — do VSAT quy định.
    // Mã ngắn (M1 / M2 / M3 / M8) — do một số trường tự đặt.
    // Cả hai đều ánh xạ về cùng cột trong xt_diemthixettuyen.
    //
    // Excel MAMONTHI │ Cột DB │ Môn
    // ───────────────┼──────────┼────────────
    // TO_VS, M1 │ `TO` │ Toán (backtick: reserved keyword MySQL)
    // LI_VS, M2 │ LI │ Vật lý
    // VA_VS, M3 │ VA │ Ngữ văn
    // N1_VS, M8 │ N1_THI │ Ngoại ngữ (điểm thi)
    private static final Map<String, String> VSAT_MON_MAP;
    static {
        VSAT_MON_MAP = new LinkedHashMap<>();
        // Mã chuẩn VSAT
        VSAT_MON_MAP.put("TO_VS", "TO");
        VSAT_MON_MAP.put("LI_VS", "LI");
        VSAT_MON_MAP.put("VA_VS", "VA");
        VSAT_MON_MAP.put("N1_VS", "N1_THI");
        // Alias ngắn (một số trường dùng mã Mx thay vì XX_VS)
        VSAT_MON_MAP.put("M1", "TO");
        VSAT_MON_MAP.put("M2", "LI");
        VSAT_MON_MAP.put("M3", "VA");
        VSAT_MON_MAP.put("M8", "N1_THI");
    }

    // ══════════════════════════════════════════════════════════════════════════════
    // 1. IELTS IMPORT (từ bài trước — giữ nguyên)
    // ══════════════════════════════════════════════════════════════════════════════

    public List<String> processIELTSImport(File file) {
        List<String> errors = new ArrayList<>();
        List<IeltsImportDTO> rows = new ArrayList<>();

        List<String> parseErrors = baseImport.importFromExcel(
                file, IeltsImportDTO.class,
                dto -> dto,
                parsed -> rows.addAll(parsed),
                dto -> validateIeltsRow(dto));
        if (!parseErrors.isEmpty())
            return parseErrors;

        // Batch cho 2 luồng song song: UPDATE N1_CC và UPSERT DiemCong
        List<N1UpdateCommand> n1Updates  = new ArrayList<>();
        List<DiemCong>        dcUpserts  = new ArrayList<>();

        // Cache breakpoints IELTS theo d_mon (chứng chỉ) — gọi DB 1 lần / loại
        // Key = d_mon trong xt_bangquydoi (ví dụ: "N1", "IELTS"...)
        // Nếu không có cấu hình → dùng diểm thô trực tiếp (fallback)
        final String IELTS_PHUONG_THUC = "IELTS";
        final String IELTS_MA_MON      = "N1";   // mã môn IELTS trong bangquydoi
        BigDecimal cachedDiemb = null; // Cache kết quả lookup cho 1 file

        for (IeltsImportDTO dto : rows) {
            String cccd = dto.getCccd().trim();

            // ── Kiểm tra tạp hợp cha ─────────────────────────────────────
            Optional<DiemThiXetTuyen> optDiem = diemThiDAO.findByCccd(cccd);
            if (optDiem.isEmpty()) {
                errors.add("[IELTS SKIP] CCCD=" + cccd + " không tồn tại trong xt_diemthixettuyen.");
                continue;
            }
            List<NganhToHop> toHopList = nguyenVongDAO.findNganhToHopByCccd(cccd);
            if (toHopList.isEmpty()) {
                errors.add("[IELTS SKIP] CCCD=" + cccd + " không có nguyện vọng trong xt_nguyenvongxettuyen.");
                continue;
            }

            // ── Bước 1: Tra cứu d_diemb từ xt_bangquydoi (step-based) ──────────
            // d_diemb = điểm cộng chứng chỉ được ghi vào diemCC
            BigDecimal diemTho = coalesceZero(dto.getDiemQd());
            BigDecimal diemCC  = bangQuyDoiDAO.lookupDiemb(IELTS_PHUONG_THUC, IELTS_MA_MON, diemTho);
            if (diemCC == null) {
                // Fallback: không có bảng quy đổi → dùng diemCong từ Excel trực tiếp
                diemCC = coalesceZero(dto.getDiemCong());
            }
            diemCC = diemCC.setScale(2, RoundingMode.HALF_UP);

            DiemThiXetTuyen dt = optDiem.get();

            // ── Bước 2: UPSERT DiemCong theo từng nguyện vọng ────────────────────
            for (NganhToHop nth : toHopList) {
                if (!passesSubsetCheck(dt, nth))
                    continue;
                String manganh = nth.getNganh().getManganh();
                String matohop = nth.getToHopMon().getMatohop();

                if (Boolean.TRUE.equals(nth.getN1())) {
                    // Tổ hợp có ngoại ngữ: cập nhật N1_CC trong xt_diemthixettuyen
                    n1Updates.add(new N1UpdateCommand(cccd, coalesceZero(dto.getDiemQd())));
                }
                // Mọi tổ hợp: UPSERT diemCC vào xt_diemcongxetuyen
                // (diemCC = điểm cộng chứng chỉ IELTS, role: điểm ưu tiên cho PT thi THPT)
                String dcKey = cccd + "_" + manganh + "_" + matohop;
                dcUpserts.add(buildDiemCong(dt.getThiSinh(), manganh, matohop, diemCC, dcKey));
            }
        }

        if (!n1Updates.isEmpty())
            errors.addAll(flushN1Updates(n1Updates));
        if (!dcUpserts.isEmpty())
            errors.addAll(flushDiemCongUpserts(dcUpserts));
        return errors;
    }

    // ══════════════════════════════════════════════════════════════════════════════
    // 2. DGNL + VSAT IMPORT — entry point
    // ══════════════════════════════════════════════════════════════════════════════

    /**
     * Điểm vào chính: đọc file Excel đa-sheet và phân phối sang
     * {@link #processDgnlSheet} / {@link #processVsatSheet}.
     *
     * <p>
     * File có thể chứa 1 hoặc cả 2 sheet "DGNL" và "VSAT"
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
    // 3. DGNL SHEET — nội suy tuyến tính → thang 30 → cột NL1
    // ══════════════════════════════════════════════════════════════════════════════

    /**
     * Xử lý sheet DGNL — 4 bước.
     *
     * <p><b>Bước 1 — Lưu điểm gốc:</b> UPDATE điểm thô (thang 1200) vào cột {@code NL1}.
     *
     * <p><b>Bước 2 — Truy vấn Nguyện vọng:</b> Lấy danh sách cặp
     * {@code {manganh, matohop}} từ {@code xt_nguyenvongxettuyen}.
     *
     * <p><b>Bước 3 — Nội suy theo Tổ Hợp:</b> Với mỗi {@code matohop},
     * lọc bảng quy đổi theo {@code d_phuongthuc='DGNL'} và {@code d_mon=matohop},
     * tính {@code y = c + (x-a)/(b-a)*(d-c)} ra thang 30.
     *
     * <p><b>Bước 4 — UPSERT DiemCong:</b> Ghi {@code y} vào cột {@code diemCC}
     * của {@code xt_diemcongxetuyen} với khóa {@code dc_keys = {cccd}_{manganh}_{matohop}}.
     *
     * @param rows Danh sách DTO từ sheet DGNL.
     * @return Danh sách lỗi.
     */
    List<String> processDgnlSheet(List<DgnlVsatRowDTO> rows) {
        List<String> errors = new ArrayList<>();

        // Bộ nhớ đệm cho 2 batch: UPDATE NL1 và UPSERT DiemCong
        List<ScoreUpdateCmd> nl1Updates = new ArrayList<>();
        List<DiemCong>       dcUpserts  = new ArrayList<>();

        // Cache breakpoints DGNL per-tổ-hợp: Key = matohop, Value = breakpoints
        // Mỗi matohop chỉ gọi DB 1 lần cho toàn bộ sheet
        final String DGNL_PT = "DGNL";
        Map<String, List<BangQuyDoi>> bqCache = new HashMap<>();

        int rowNum = 2;
        for (DgnlVsatRowDTO dto : rows) {

            // ── Validate ─────────────────────────────────────────────────
            String err = validateDgnlVsatRow(dto, "DGNL");
            if (err != null) {
                errors.add("DGNL dòng " + rowNum + ": " + err);
                rowNum++;
                continue;
            }
            if (!"DGNL".equalsIgnoreCase(dto.getMamonthi())) {
                System.out.println("[DGNL] Bỏ qua dòng " + rowNum + " MAMONTHI=" + dto.getMamonthi());
                rowNum++;
                continue;
            }

            String cccd = dto.getCmnd().trim();
            if (!existsInDiemThi(cccd)) {
                errors.add("[DGNL SKIP] CCCD=" + cccd + " không tồn tại trong xt_diemthixettuyen.");
                rowNum++;
                continue;
            }

            BigDecimal diemTho = coalesceZero(dto.getDiem());

            // ── Bước 1: Lưu điểm gốc (NL1 = điểm thô thang 1200) ──────────────
            nl1Updates.add(new ScoreUpdateCmd(cccd, "NL1",
                    diemTho.setScale(2, RoundingMode.HALF_UP)));

            // ── Bước 2: Truy vấn nguyện vọng ──────────────────────────────────
            List<NganhToHop> toHopList = nguyenVongDAO.findNganhToHopByCccd(cccd);
            if (toHopList.isEmpty()) {
                // Không có nguyện vọng → chỉ lưu NL1, không có DiemCong
                rowNum++;
                continue;
            }

            // ── Bước 3 & 4: Nội suy per-tổ-hợp → UPSERT DiemCong ───────────────
            for (NganhToHop nth : toHopList) {
                String manganh = nth.getNganh().getManganh();
                String matohop = nth.getToHopMon().getMatohop();

                // Cache breakpoints theo matohop (1 lần / tổ hợp)
                List<BangQuyDoi> bp = bqCache.computeIfAbsent(
                        matohop, k -> bangQuyDoiDAO.findAllByPhuongThucAndMon(DGNL_PT, k));

                if (bp.isEmpty()) {
                    errors.add("[DGNL] CCCD=" + cccd + " matohop=" + matohop
                            + ": không có bảng quy đổi DGNL cho tổ hợp này.");
                    continue;
                }

                BigDecimal[] abcd = BangQuyDoiDAO.interpolateFromRows(bp, diemTho);
                if (abcd == null) {
                    errors.add("[DGNL] CCCD=" + cccd + " matohop=" + matohop
                            + ": không tìm được khoảng nội suy cho điểm=" + diemTho);
                    continue;
                }

                // y = nội suy tuyến tính (thang 30)
                BigDecimal y = interpolate(diemTho, abcd[0], abcd[1], abcd[2], abcd[3]);

                // Lấy ThiSinh từ bảng xt_diemthixettuyen
                DiemThiXetTuyen dtXt = diemThiDAO.findByCccd(cccd).orElse(null);
                if (dtXt == null) continue; // không nên xảy ra (vừa kiểm tra ở trên)

                String dcKey = cccd + "_" + manganh + "_" + matohop;
                dcUpserts.add(buildDiemCong(dtXt.getThiSinh(), manganh, matohop, y, dcKey));
            }
            rowNum++;
        }

        // Flush 2 batch riêng biệt: NL1 trước, DiemCong sau
        if (!nl1Updates.isEmpty())
            errors.addAll(flushColumnUpdates(nl1Updates, "DGNL-NL1"));
        if (!dcUpserts.isEmpty())
            errors.addAll(flushDiemCongUpserts(dcUpserts));
        return errors;
    }

    // ══════════════════════════════════════════════════════════════════════════════
    // 4. VSAT SHEET — nội suy tuyến tính → thang 10 → các cột TO/LI/VA/N1_THI
    // ══════════════════════════════════════════════════════════════════════════════

    /**
     * Xử lý sheet VSAT với nội suy tuyến tính từ bảng {@code xt_bangquydoi}.
     *
     * <p>
     * <b>Logic:</b>
     * <ol>
     * <li>Cache breakpoints theo từng mã môn chuẩn VSAT
     * ({@code TO_VS, LI_VS, VA_VS, N1_VS})
     * — mỗi mã môn chỉ gọi DB 1 lần cho toàn bộ sheet.</li>
     * <li>Alias ngắn (M1/M2/M3/M8) được chuẩn hóa về mã chuẩn để tra cứu
     * cache.</li>
     * <li>Nội suy tuyến tính: {@code y = c + (x-a)/(b-a) * (d-c)},
     * kết quả làm tròn 2 chữ số, nằm trong thang 10.</li>
     * <li>UPDATE: {@code GREATEST(COALESCE(col,0), :y)} — chỉ ghi nếu cao hơn.</li>
     * </ol>
     *
     * @param rows Danh sách DTO từ sheet VSAT.
     * @return Danh sách lỗi.
     */
    List<String> processVsatSheet(List<DgnlVsatRowDTO> rows) {
        List<String> errors = new ArrayList<>();
        List<ScoreUpdateCmd> cmds  = new ArrayList<>();

        // ── Tải toàn bộ xt_bangquydoi vào in-memory cache 1 LẦN duy nhất ──────
        // Key: mã môn gốc (TO_VS) VÀ mã chuẩn hóa (TO) — cả hai đều được index
        // Tránh N lần SELECT lẻ tẻ trong vòng lặp → tối ưu performance
        Map<String, List<BangQuyDoi>> bqdCache = bangQuyDoiDAO.loadAllAsCache();

        int rowNum = 2;
        for (DgnlVsatRowDTO dto : rows) {

            // ── Validate ───────────────────────────────────────────────────────
            String err = validateDgnlVsatRow(dto, "VSAT");
            if (err != null) {
                errors.add("VSAT dòng " + rowNum + ": " + err);
                rowNum++;
                continue;
            }

            String maMon = dto.getMamonthi() == null ? "" : dto.getMamonthi().trim().toUpperCase();

            // Lấy cột DB đích — VSAT_MON_MAP hỗ trợ cả mã chuẩn lẫn alias ngắn
            String dbCol = VSAT_MON_MAP.get(maMon);
            if (dbCol == null) {
                System.out.println("[VSAT] Bỏ qua dòng " + rowNum
                        + " MAMONTHI=" + maMon + " (không ánh xạ DB column)");
                rowNum++;
                continue;
            }

            String cccd = dto.getCmnd().trim();
            if (!existsInDiemThi(cccd)) {
                errors.add("[VSAT SKIP] CCCD=" + cccd + " không tồn tại trong xt_diemthixettuyen.");
                rowNum++;
                continue;
            }

            // ── Tra breakpoints từ cache (tự động normalize suffix _VS/_DGNL) ──
            // lookupFromCache: thử key gốc (TO_VS) → fallback key chuẩn hóa (TO)
            List<BangQuyDoi> breakpoints = BangQuyDoiDAO.lookupFromCache(bqdCache, maMon);

            if (breakpoints.isEmpty()) {
                // Cảnh báo chi tiết + skip dòng — KHÔNG ngắt toàn bộ tiến trình
                String normalized = BangQuyDoiDAO.normalizeMaMon(maMon);
                System.err.println("[VSAT] CẢNH BÁO dòng " + rowNum
                        + " CCCD=" + cccd
                        + ": Không tìm thấy bảng quy đổi cho mã môn='" + maMon + "'"
                        + " (đã thử key chuẩn hóa='" + normalized + "')."
                        + " Kiểm tra cột d_mon trong xt_bangquydoi.");
                rowNum++;
                continue;
            }

            // ── Nội suy tuyến tính → thang 10 ─────────────────────────────────
            BigDecimal diemTho = coalesceZero(dto.getDiem());
            BigDecimal[] abcd  = BangQuyDoiDAO.interpolateFromRows(breakpoints, diemTho);

            if (abcd == null) {
                errors.add("[VSAT] Dòng " + rowNum + " CCCD=" + cccd
                        + ": Không tìm được khoảng nội suy cho điểm=" + diemTho
                        + " mã môn=" + maMon);
                rowNum++;
                continue;
            }

            BigDecimal y = interpolate(diemTho, abcd[0], abcd[1], abcd[2], abcd[3]);
            cmds.add(new ScoreUpdateCmd(cccd, dbCol, y));
            rowNum++;
        }

        if (!cmds.isEmpty())
            errors.addAll(flushColumnUpdates(cmds, "VSAT"));
        return errors;
    }

    // ══════════════════════════════════════════════════════════════════════════════
    // 5. BATCH FLUSH — dùng chung cho DGNL và VSAT
    // ══════════════════════════════════════════════════════════════════════════════

    /**
     * Batch UPDATE nhiều cột khác nhau trong {@code xt_diemthixettuyen}.
     *
     * <p>
     * SQL template (cột được truyền qua string ghép — an toàn vì đã
     * whitelist qua {@link #VSAT_MON_MAP} + hằng "NL1"):
     * 
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
        Session session = null;
        Transaction tx = null;

        try {
            session = HibernateUtil.getSessionFactory().openSession();
            tx = session.beginTransaction();
            int count = 0;

            for (ScoreUpdateCmd cmd : cmds) {
                /*
                 * Whitelist cột để tránh SQL Injection (col chỉ đến từ VSAT_MON_MAP hoặc
                 * "NL1").
                 * Tên cột có thể chứa ký tự backtick (ví dụ `TO`) — thêm backtick bọc ngoài để
                 * MySQL xử lý đúng tên cột reserved keyword.
                 */
                String colSafe = "`" + cmd.dbCol() + "`";
                String sql = "UPDATE xt_diemthixettuyen " +
                        "SET " + colSafe + " = GREATEST(COALESCE(" + colSafe + ", 0), :diem) " +
                        "WHERE cccd = :cccd";

                session.createNativeMutationQuery(sql)
                        .setParameter("diem", cmd.diem())
                        .setParameter("cccd", cmd.cccd())
                        .executeUpdate();

                if (++count % BATCH_SIZE == 0)
                    session.flush();
            }

            tx.commit();
            System.out.println("[" + source + "] Batch UPDATE hoàn tất: " + cmds.size() + " lệnh.");

        } catch (Exception e) {
            if (tx != null && tx.isActive()) {
                try {
                    tx.rollback();
                } catch (Exception rb) {
                    System.err.println("Rollback " + source + " thất bại: " + rb.getMessage());
                }
            }
            e.printStackTrace();
            errors.add("Lỗi batch UPDATE " + source + ": " + e.getMessage());
        } finally {
            if (session != null && session.isOpen())
                session.close();
        }
        return errors;
    }

    // ══════════════════════════════════════════════════════════════════════════════
    // 6. IELTS HELPERS
    // ══════════════════════════════════════════════════════════════════════════════

    private List<String> flushN1Updates(List<N1UpdateCommand> commands) {
        List<String> errors = new ArrayList<>();
        Session session = null;
        Transaction tx = null;
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
                        .setParameter("cccd", cmd.cccd())
                        .executeUpdate();
                if (++count % BATCH_SIZE == 0)
                    session.flush();
            }
            tx.commit();
        } catch (Exception e) {
            if (tx != null && tx.isActive())
                try {
                    tx.rollback();
                } catch (Exception ignored) {
                }
            e.printStackTrace();
            errors.add("Lỗi batch UPDATE N1_CC: " + e.getMessage());
        } finally {
            if (session != null && session.isOpen())
                session.close();
        }
        return errors;
    }

    private List<String> flushDiemCongUpserts(List<DiemCong> entities) {
        List<String> errors = new ArrayList<>();
        Session session = null;
        Transaction tx = null;
        try {
            session = HibernateUtil.getSessionFactory().openSession();
            tx = session.beginTransaction();
            int count = 0;
            for (DiemCong dc : entities) {
                session.createNativeMutationQuery(
                        "INSERT INTO xt_diemcongxetuyen (ts_cccd, manganh, matohop, diemCC, dc_keys) " +
                                "VALUES (:cccd, :manganh, :matohop, :diemCC, :dcKeys) " +
                                "ON DUPLICATE KEY UPDATE diemCC = VALUES(diemCC)")
                        .setParameter("cccd", dc.getThiSinh().getCccd())
                        .setParameter("manganh", dc.getManganh())
                        .setParameter("matohop", dc.getMatohop())
                        .setParameter("diemCC", dc.getDiemCC())
                        .setParameter("dcKeys", dc.getDcKeys())
                        .executeUpdate();
                if (++count % BATCH_SIZE == 0)
                    session.flush();
            }
            tx.commit();
        } catch (Exception e) {
            if (tx != null && tx.isActive())
                try {
                    tx.rollback();
                } catch (Exception ignored) {
                }
            e.printStackTrace();
            errors.add("Lỗi batch UPSERT xt_diemcongxetuyen: " + e.getMessage());
        } finally {
            if (session != null && session.isOpen())
                session.close();
        }
        return errors;
    }

    // ══════════════════════════════════════════════════════════════════════════════
    // 7. VALIDATION HELPERS
    // ══════════════════════════════════════════════════════════════════════════════

    private String validateIeltsRow(IeltsImportDTO dto) {
        if (dto.getCccd() == null || dto.getCccd().trim().isEmpty())
            return "Bắt buộc phải có CCCD";
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

    /**
     * Kiểm tra nhanh cccd có trong xt_diemthixettuyen không — dùng Optional từ DAO
     * có sẵn.
     */
    private boolean existsInDiemThi(String cccd) {
        return diemThiDAO.findByCccd(cccd).isPresent();
    }

    // ══════════════════════════════════════════════════════════════════════════════
    // 8. SHARED HELPERS
    // ══════════════════════════════════════════════════════════════════════════════

    private boolean passesSubsetCheck(DiemThiXetTuyen dt, NganhToHop nth) {
        if (Boolean.TRUE.equals(nth.getTo()) && !isPositive(dt.getTo()))
            return false;
        if (Boolean.TRUE.equals(nth.getVa()) && !isPositive(dt.getVa()))
            return false;
        if (Boolean.TRUE.equals(nth.getLi()) && !isPositive(dt.getLi()))
            return false;
        if (Boolean.TRUE.equals(nth.getHo()) && !isPositive(dt.getHo()))
            return false;
        if (Boolean.TRUE.equals(nth.getSi()) && !isPositive(dt.getSi()))
            return false;
        if (Boolean.TRUE.equals(nth.getSu()) && !isPositive(dt.getSu()))
            return false;
        if (Boolean.TRUE.equals(nth.getDi()) && !isPositive(dt.getDi()))
            return false;
        if (Boolean.TRUE.equals(nth.getTi()) && !isPositive(dt.getTi()))
            return false;
        if (Boolean.TRUE.equals(nth.getKtpl()) && !isPositive(dt.getKtpl()))
            return false;
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

    /**
     * Nội suy tuyến tính: {@code y = c + (x - a) / (b - a) * (d - c)}.
     *
     * <p>
     * Toàn bộ phép tính dùng {@link BigDecimal} với {@link RoundingMode#HALF_UP}
     * để đảm bảo độ chính xác theo yêu cầu nghiệp vụ.
     *
     * <p>
     * Trường hợp đặc biệt: nếu {@code b == a} (khoảng bằng 0 — breakpoint đơn),
     * trả về {@code c} để tránh chia cho 0.
     *
     * @param x Điểm thô cần quy đổi (nằm trong [a, b]).
     * @param a Ngưỡng điểm thô dưới.
     * @param b Ngưỡng điểm thô trên.
     * @param c Ngưỡng điểm quy đổi dưới (tương ứng với a).
     * @param d Ngưỡng điểm quy đổi trên (tương ứng với b).
     * @return Điểm quy đổi y, làm tròn 2 chữ số thập phân.
     */
    private BigDecimal interpolate(BigDecimal x,
            BigDecimal a, BigDecimal b,
            BigDecimal c, BigDecimal d) {
        BigDecimal rangeIn = b.subtract(a); // (b - a)
        if (rangeIn.compareTo(BigDecimal.ZERO) == 0) {
            // Breakpoint đơn → trả về c, làm tròn
            return c.setScale(2, RoundingMode.HALF_UP);
        }
        // y = c + (x - a) / (b - a) * (d - c)
        BigDecimal rangeOut = d.subtract(c); // (d - c)
        BigDecimal ratio = x.subtract(a)
                .divide(rangeIn, 10, RoundingMode.HALF_UP); // độ chính xác cao trung gian
        return c.add(ratio.multiply(rangeOut))
                .setScale(2, RoundingMode.HALF_UP);
    }

    // ══════════════════════════════════════════════════════════════════════════════
    // 9. VALUE OBJECTS
    // ══════════════════════════════════════════════════════════════════════════════

    /** Lệnh UPDATE 1 cột điểm cho 1 thí sinh (dùng chung DGNL + VSAT). */
    private record ScoreUpdateCmd(String cccd, String dbCol, BigDecimal diem) {
    }

    /** Lệnh UPDATE N1_CC cho IELTS import. */
    private record N1UpdateCommand(String cccd, BigDecimal diemQd) {
    }
}
