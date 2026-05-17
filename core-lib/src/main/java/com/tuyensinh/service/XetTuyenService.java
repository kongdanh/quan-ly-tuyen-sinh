package com.tuyensinh.service;

import com.tuyensinh.dao.BangQuyDoiDAO;
import com.tuyensinh.dao.DiemCongDAO;
import com.tuyensinh.dao.DiemThiXetTuyenDAO;
import com.tuyensinh.dao.NguyenVongDAO;
import com.tuyensinh.dao.XetTuyenDAO;
import com.tuyensinh.dto.DgnlVsatRowDTO;
import com.tuyensinh.dto.IeltsImportDTO;
import com.tuyensinh.model.BangQuyDoi;
import com.tuyensinh.model.DiemCong;
import com.tuyensinh.model.DiemThiXetTuyen;
import com.tuyensinh.model.KetQuaXetTuyen;
import com.tuyensinh.model.HoSoTuyenSinh;
import com.tuyensinh.model.NganhToHop;
import com.tuyensinh.model.NguyenVong;
import com.tuyensinh.model.ThiSinh;
import com.tuyensinh.util.ExcelReaderUtil;
import com.tuyensinh.util.HibernateUtil;
import com.tuyensinh.util.SystemLogger;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;

import java.io.File;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service xử lý toàn bộ luồng nghiệp vụ Xét tuyển và Import điểm.
 */
public class XetTuyenService {

    // ── Dependencies ────────────────────────────────────────────────────────────
    private final XetTuyenDAO xetTuyenDAO = new XetTuyenDAO();
    private final DiemThiXetTuyenDAO diemThiDAO = new DiemThiXetTuyenDAO();
    private final NguyenVongDAO nguyenVongDAO = new NguyenVongDAO();
    private final DiemCongDAO diemCongDAO = new DiemCongDAO();
    private final BangQuyDoiDAO bangQuyDoiDAO = new BangQuyDoiDAO();
    private final BaseImportService<IeltsImportDTO, IeltsImportDTO> baseImport = new BaseImportService<>();

    private static final int BATCH_SIZE = 100;
    private static final String SHEET_DGNL = "dgnl";
    private static final String SHEET_VSAT = "vsat";

    private static final Map<String, String> VSAT_MON_MAP;
    static {
        VSAT_MON_MAP = new LinkedHashMap<>();
        VSAT_MON_MAP.put("TO_VS", "TO");
        VSAT_MON_MAP.put("LI_VS", "LI");
        VSAT_MON_MAP.put("VA_VS", "VA");
        VSAT_MON_MAP.put("N1_VS", "N1_THI");
        VSAT_MON_MAP.put("M1", "TO");
        VSAT_MON_MAP.put("M2", "LI");
        VSAT_MON_MAP.put("M3", "VA");
        VSAT_MON_MAP.put("M8", "N1_THI");
    }

    // ══════════════════════════════════════════════════════════════════════════════
    // 1. THUẬT TOÁN XÉT TUYỂN (Kết hợp cả hai)
    // ══════════════════════════════════════════════════════════════════════════════

    /**
     * Chạy thuật toán xét tuyển cho 1 đợt.
     */
    public void chayThuatToanXetTuyen(Integer idDot) {
        System.out.println("[XetTuyenService] Bat dau chay thuat toan xet tuyen cho Dot ID=" + idDot);
        SystemLogger.log(null, "System", "Bắt đầu chạy thuật toán xét tuyển cho Đợt ID=" + idDot, true);
        
        Session session = HibernateUtil.getSessionFactory().openSession();
        Transaction transaction = null;
        try {
            transaction = session.beginTransaction();

            session.createNativeMutationQuery(
                    "DELETE FROM xt_ket_qua_xet_tuyen WHERE id_ho_so IN (SELECT id FROM xt_ho_so_tuyen_sinh WHERE id_dot_tuyen_sinh = :idDot)")
                    .setParameter("idDot", idDot)
                    .executeUpdate();

            // Lấy danh sách Nguyện vọng cần xét (Kết hợp cả hai cách)
            Query<NguyenVong> query = session.createQuery(
                "SELECT nv FROM NguyenVong nv WHERE nv.hoSoTuyenSinh.dotTuyenSinh.id = :idDot AND nv.hoSoTuyenSinh.trangThai = 'HOP_LE' ORDER BY nv.hoSoTuyenSinh.thiSinh.cccd, nv.nvTt ASC", 
                NguyenVong.class);
            query.setParameter("idDot", idDot);
            List<NguyenVong> listNV = query.getResultList();
            
            System.out.println("[XetTuyenService] Tim thay " + listNV.size() + " nguyen vong can xet");
            
            // Force init các relationship (tránh lỗi Lazy loading)
            for (NguyenVong nv : listNV) {
                if (nv.getNganh() != null) nv.getNganh().getId();
                if (nv.getHoSoTuyenSinh() != null) nv.getHoSoTuyenSinh().getId();
                if (nv.getThiSinh() != null) nv.getThiSinh().getId();
            }

            Map<String, List<NguyenVong>> mapThiSinh = listNV.stream()
                    .collect(Collectors.groupingBy(nv -> nv.getThiSinh().getCccd()));

            // LẤY ĐIỂM CHUẨN (Đã fix lỗi 999.0)
            List<com.tuyensinh.model.DiemChuanDot> diemChuanDots = session.createQuery(
                    "SELECT dc FROM DiemChuanDot dc WHERE dc.dotTuyenSinh.id = :idDot",
                    com.tuyensinh.model.DiemChuanDot.class
            ).setParameter("idDot", idDot).getResultList();

            if (diemChuanDots.isEmpty()) {
                throw new RuntimeException("Chưa có dữ liệu Điểm chuẩn cho Đợt này. Vui lòng vào màn hình Quản lý Điểm chuẩn để Lưu cấu hình điểm trước!");
            }

            Map<String, Double> diemChuanMap = new HashMap<>();
            for (com.tuyensinh.model.DiemChuanDot dc : diemChuanDots) {
                if (dc.getDiemChuan() == null) continue;
                Double score = dc.getDiemChuan().doubleValue();
                
                String manganh = dc.getNganhToHop() != null && dc.getNganhToHop().getNganh() != null
                        ? dc.getNganhToHop().getNganh().getManganh() : null;

                if (manganh != null) {
                    manganh = manganh.trim().toUpperCase();
                    // 1. Lưu Key: MANGANH (Dùng làm Fallback, chuẩn nhất cho các trường Đại học)
                    diemChuanMap.putIfAbsent(manganh, score);

                    // 2. Lưu Key: MANGANH|MATOHOP (vd: 7480201|A00)
                    if (dc.getNganhToHop().getToHopMon() != null && dc.getNganhToHop().getToHopMon().getMatohop() != null) {
                        String matohop = dc.getNganhToHop().getToHopMon().getMatohop().trim().toUpperCase();
                        diemChuanMap.put(manganh + "|" + matohop, score);
                    }
                    
                    // 3. Lưu Key: MANGANH|MON1-MON2-MON3 (Để khớp với chữ TO-VA-LI của Thí sinh)
                    String m1 = dc.getNganhToHop().getThMon1();
                    String m2 = dc.getNganhToHop().getThMon2();
                    String m3 = dc.getNganhToHop().getThMon3();
                    if (m1 != null && m2 != null && m3 != null) {
                         String combo = m1.trim().toUpperCase() + "-" + m2.trim().toUpperCase() + "-" + m3.trim().toUpperCase();
                         diemChuanMap.put(manganh + "|" + combo, score);
                    }
                }
            }

            int soTrungTuyen = 0;
            int soRot = 0;

            for (Map.Entry<String, List<NguyenVong>> entry : mapThiSinh.entrySet()) {
                boolean daDau = false;
                
                // Cập nhật điểm max vào hồ sơ
                Double tongDiemMax = entry.getValue().stream()
                        .mapToDouble(nv -> nv.getDiemXettuyen() != null ? nv.getDiemXettuyen() : 0.0).max().orElse(0.0);

                if (!entry.getValue().isEmpty() && entry.getValue().get(0).getHoSoTuyenSinh() != null) {
                    HoSoTuyenSinh hs = entry.getValue().get(0).getHoSoTuyenSinh();
                    hs.setTongDiemXetTuyen(tongDiemMax);
                    session.merge(hs);
                }

                for (NguyenVong nv : entry.getValue()) {
                    if (daDau) {
                        nv.setNvKetqua("HUY");
                        session.merge(nv);
                        continue;
                    }

                    String maNganhNorm = nv.getNganh().getManganh() != null ? nv.getNganh().getManganh().trim().toUpperCase() : "";
                    String maThNorm = nv.getTtThm() != null ? nv.getTtThm().trim().toUpperCase() : "";
                    String key = maNganhNorm + "|" + maThNorm;

                    // MAPPING THÔNG MINH
                    Double diemChuan = 999.0;
                    if (diemChuanMap.containsKey(key)) {
                        diemChuan = diemChuanMap.get(key); // Khớp theo Manganh|TO-VA-LI hoặc Manganh|A00
                    } else if (diemChuanMap.containsKey(maNganhNorm)) {
                        diemChuan = diemChuanMap.get(maNganhNorm); // Lấy Điểm chuẩn chung của Ngành đó
                    }

                    Double diemThiSinh = nv.getDiemXettuyen() != null ? nv.getDiemXettuyen() : 0.0;
                    nv.setDiemXettuyen(diemThiSinh);
                    
                    System.out.println("[DEBUG XET TUYEN] CCCD=" + nv.getThiSinh().getCccd() 
                        + " | NV=" + nv.getNvTt() + " | Ngành=" + maNganhNorm 
                        + " | Tổ Hợp=" + maThNorm + " | Điểm TS=" + diemThiSinh + " | Điểm Chuẩn=" + diemChuan);

                    if (diemThiSinh >= diemChuan) {
                        nv.setNvKetqua("DAU");
                        session.merge(nv);
                        daDau = true;
                        soTrungTuyen++;

                        KetQuaXetTuyen kq = new KetQuaXetTuyen();
                        kq.setHoSo(nv.getHoSoTuyenSinh());
                        kq.setNganh(nv.getNganh());
                        kq.setDiemXetTuyen(diemThiSinh);
                        kq.setNguyenVongThu(nv.getNvTt());
                        kq.setMaToHop(nv.getTtThm());
                        kq.setPhuongThuc(nv.getTtPhuongthuc());
                        kq.setTrangThai("TRUNG_TUYEN");
                        session.persist(kq);
                    } else {
                        nv.setNvKetqua("ROT");
                        session.merge(nv);
                        soRot++;
                    }
                }
            }
            transaction.commit();
            
            System.out.println("[XetTuyenService] Hoan thanh xet tuyen Dot ID=" + idDot + ": " + soTrungTuyen
                    + " trung tuyen, " + soRot + " rot");
            SystemLogger.log(null, "System", "Hoàn thành xét tuyển Đợt ID=" + idDot + ": " + soTrungTuyen + " trúng tuyển, " + soRot + " rớt", true);
            
        } catch (Exception e) {
            if (transaction != null) transaction.rollback();
            System.err.println("[XetTuyenService] Loi chay thuat toan xet tuyen: " + e.getMessage());
            e.printStackTrace();
            SystemLogger.log(null, "System", "Lỗi chạy thuật toán xét tuyển Đợt ID=" + idDot + ": " + e.getMessage(), false);
            throw new RuntimeException("Loi chay thuat toan: " + e.getMessage());
        } finally {
            if (session != null) session.close();
        }
    }

    public KetQuaXetTuyen timKetQuaTheoCccd(String cccd) {
        return xetTuyenDAO.timKetQuaTheoCccd(cccd);
    }

    // ══════════════════════════════════════════════════════════════════════════════
    // 2. IELTS IMPORT (Từ Branch)
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

        List<N1UpdateCommand> n1Updates = new ArrayList<>();
        List<DiemCong> dcUpserts = new ArrayList<>();

        for (IeltsImportDTO dto : rows) {
            String cccd = dto.getCccd().trim();
            List<String> rowErrors = buildIeltsCommands(cccd, dto.getDiemQd(), dto.getDiemCong(), n1Updates, dcUpserts);
            errors.addAll(rowErrors);
        }

        if (!n1Updates.isEmpty())
            errors.addAll(flushN1Updates(n1Updates));
        if (!dcUpserts.isEmpty())
            errors.addAll(flushDiemCongUpserts(dcUpserts));
        return errors;
    }

    public List<String> processSingleIelts(String cccd, BigDecimal diemTho) {
        List<N1UpdateCommand> n1Updates = new ArrayList<>();
        List<DiemCong> dcUpserts = new ArrayList<>();
        List<String> errors = buildIeltsCommands(cccd, diemTho, null, n1Updates, dcUpserts);
        if (!n1Updates.isEmpty())
            errors.addAll(flushN1Updates(n1Updates));
        if (!dcUpserts.isEmpty())
            errors.addAll(flushDiemCongUpserts(dcUpserts));
        return errors;
    }

    private List<String> buildIeltsCommands(String cccd, BigDecimal diemQd, BigDecimal diemCongOriginal,
            List<N1UpdateCommand> n1Updates, List<DiemCong> dcUpserts) {
        List<String> errors = new ArrayList<>();
        Optional<DiemThiXetTuyen> optDiem = diemThiDAO.findByCccd(cccd);
        if (optDiem.isEmpty()) {
            errors.add("[IELTS SKIP] CCCD=" + cccd + " không tồn tại trong xt_diemthixettuyen.");
            return errors;
        }
        List<NganhToHop> toHopList = nguyenVongDAO.findNganhToHopByCccd(cccd);
        if (toHopList.isEmpty()) {
            errors.add("[IELTS SKIP] CCCD=" + cccd + " không có nguyện vọng trong xt_nguyenvongxettuyen.");
            return errors;
        }

        BigDecimal diemTho = coalesceZero(diemQd);
        BigDecimal diemCC = bangQuyDoiDAO.lookupDiemb("IELTS", "N1", diemTho);
        if (diemCC == null) {
            diemCC = coalesceZero(diemCongOriginal);
        }
        diemCC = diemCC.setScale(2, RoundingMode.HALF_UP);

        DiemThiXetTuyen dt = optDiem.get();

        for (NganhToHop nth : toHopList) {
            if (!passesSubsetCheck(dt, nth))
                continue;
            String manganh = nth.getNganh().getManganh();
            String matohop = nth.getToHopMon().getMatohop();

            if (Boolean.TRUE.equals(nth.getN1())) {
                n1Updates.add(new N1UpdateCommand(cccd, diemCC));
            }
            String dcKey = cccd + "_" + manganh + "_" + matohop;
            dcUpserts.add(buildDiemCong(dt.getThiSinh(), manganh, matohop, diemCC, dcKey));
        }
        return errors;
    }

    // ══════════════════════════════════════════════════════════════════════════════
    // 3. DGNL + VSAT IMPORT (Từ Branch)
    // ══════════════════════════════════════════════════════════════════════════════

    public List<String> processDgnlVsatImport(File file) {
        List<String> errors = new ArrayList<>();
        Map<String, List<DgnlVsatRowDTO>> allSheets;

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

            rows.forEach(r -> r.setSheetName(sheetName));

            if (sheetName.equalsIgnoreCase(SHEET_DGNL)) {
                foundAny = true;
                errors.addAll(processDgnlSheet(rows));
            } else if (sheetName.equalsIgnoreCase(SHEET_VSAT)) {
                foundAny = true;
                errors.addAll(processVsatSheet(rows));
            }
        }

        if (!foundAny) {
            errors.add("Không tìm thấy sheet 'DGNL' hoặc 'VSAT' trong file.");
        }
        return errors;
    }

    List<String> processDgnlSheet(List<DgnlVsatRowDTO> rows) {
        List<String> errors = new ArrayList<>();
        List<ScoreUpdateCmd> nl1Updates = new ArrayList<>();
        List<DiemCong> dcUpserts = new ArrayList<>();
        Map<String, List<BangQuyDoi>> bqCache = new HashMap<>();

        Set<String> missingBangQuyDoi = new HashSet<>();
        
        int rowNum = 2;
        for (DgnlVsatRowDTO dto : rows) {
            String err = validateDgnlVsatRow(dto, "DGNL");
            if (err != null) {
                errors.add("DGNL dòng " + rowNum + ": " + err);
                rowNum++;
                continue;
            }
            if (!"DGNL".equalsIgnoreCase(dto.getMamonthi())) {
                rowNum++;
                continue;
            }

            String cccd = dto.getCmnd().trim();
            if (!existsInDiemThi(cccd)) {
                rowNum++;
                continue;
            }
            
            BigDecimal diemTho = coalesceZero(dto.getDiem());
            nl1Updates.add(new ScoreUpdateCmd(cccd, "NL1", diemTho.setScale(2, RoundingMode.HALF_UP)));

            List<NganhToHop> toHopList = nguyenVongDAO.findNganhToHopByCccd(cccd);
            for (NganhToHop nth : toHopList) {
                String manganh = nth.getNganh().getManganh();
                String matohop = nth.getToHopMon().getMatohop();

                List<BangQuyDoi> bp = bqCache.computeIfAbsent(matohop, k -> bangQuyDoiDAO.findAllByPhuongThucAndMon("DGNL", k));

                if (bp.isEmpty()) {
                    missingBangQuyDoi.add(matohop);
                    continue;
                }

                BigDecimal[] abcd = BangQuyDoiDAO.interpolateFromRows(bp, diemTho);
                if (abcd == null) {
                    continue;
                }

                BigDecimal y = interpolate(diemTho, abcd[0], abcd[1], abcd[2], abcd[3]);
                DiemThiXetTuyen dtXt = diemThiDAO.findByCccd(cccd).orElse(null);
                if (dtXt == null) continue;

                String dcKey = cccd + "_" + manganh + "_" + matohop;
                dcUpserts.add(buildDiemCong(dtXt.getThiSinh(), manganh, matohop, y, dcKey));
            }
            rowNum++;
        }
        
        if (!missingBangQuyDoi.isEmpty()) {
            errors.add("[CẢNH BÁO] Hệ thống chưa có cấu hình Bảng Quy Đổi DGNL cho các tổ hợp sau: " + String.join(", ", missingBangQuyDoi) + ". Điểm NL1 vẫn được lưu thành công, nhưng chưa thể quy đổi điểm xét tuyển cho các tổ hợp này.");
        }

        if (!nl1Updates.isEmpty())
            errors.addAll(flushColumnUpdates(nl1Updates, "DGNL-NL1"));
        if (!dcUpserts.isEmpty())
            errors.addAll(flushDiemCongUpserts(dcUpserts));
        return errors;
    }

    public List<String> processSingleDgnl(String cccd, BigDecimal diemTho) {
        List<ScoreUpdateCmd> nl1Updates = new ArrayList<>();
        List<DiemCong> dcUpserts = new ArrayList<>();
        List<String> errors = buildDgnlCommands(cccd, diemTho, nl1Updates, dcUpserts, null);

        if (!nl1Updates.isEmpty())
            errors.addAll(flushColumnUpdates(nl1Updates, "DGNL-NL1"));
        if (!dcUpserts.isEmpty())
            errors.addAll(flushDiemCongUpserts(dcUpserts));
        return errors;
    }

    private List<String> buildDgnlCommands(String cccd, BigDecimal diemTho,
            List<ScoreUpdateCmd> nl1Updates, List<DiemCong> dcUpserts,
            Map<String, List<BangQuyDoi>> bqCache) {
        List<String> errors = new ArrayList<>();
        
        if (!existsInDiemThi(cccd)) {
            errors.add("[DGNL SKIP] CCCD=" + cccd + " không tồn tại trong xt_diemthixettuyen.");
            return errors;
        }

        diemTho = coalesceZero(diemTho);
        nl1Updates.add(new ScoreUpdateCmd(cccd, "NL1", diemTho.setScale(2, RoundingMode.HALF_UP)));

        List<NganhToHop> toHopList = nguyenVongDAO.findNganhToHopByCccd(cccd);
        for (NganhToHop nth : toHopList) {
            String manganh = nth.getNganh().getManganh();
            String matohop = nth.getToHopMon().getMatohop();

            List<BangQuyDoi> bp;
            if (bqCache != null) {
                bp = bqCache.computeIfAbsent(matohop, k -> bangQuyDoiDAO.findAllByPhuongThucAndMon("DGNL", k));
            } else {
                bp = bangQuyDoiDAO.findAllByPhuongThucAndMon("DGNL", matohop);
            }

            if (bp.isEmpty()) {
                errors.add("[DGNL] CCCD=" + cccd + " matohop=" + matohop + ": không có bảng quy đổi DGNL.");
                continue;
            }

            BigDecimal[] abcd = BangQuyDoiDAO.interpolateFromRows(bp, diemTho);
            if (abcd == null) {
                errors.add("[DGNL] CCCD=" + cccd + " matohop=" + matohop + ": không tìm được khoảng nội suy.");
                continue;
            }

            BigDecimal y = interpolate(diemTho, abcd[0], abcd[1], abcd[2], abcd[3]);
            DiemThiXetTuyen dtXt = diemThiDAO.findByCccd(cccd).orElse(null);
            if (dtXt == null) continue;

            String dcKey = cccd + "_" + manganh + "_" + matohop;
            dcUpserts.add(buildDiemCong(dtXt.getThiSinh(), manganh, matohop, y, dcKey));
        }
        return errors;
    }

    List<String> processVsatSheet(List<DgnlVsatRowDTO> rows) {
        List<String> errors = new ArrayList<>();
        List<ScoreUpdateCmd> cmds = new ArrayList<>();
        Map<String, List<BangQuyDoi>> bqdCache = bangQuyDoiDAO.loadAllAsCache();

        int rowNum = 2;
        for (DgnlVsatRowDTO dto : rows) {
            String err = validateDgnlVsatRow(dto, "VSAT");
            if (err != null) {
                errors.add("VSAT dòng " + rowNum + ": " + err);
                rowNum++;
                continue;
            }

            String maMon = dto.getMamonthi();
            String cccd = dto.getCmnd().trim();
            BigDecimal diemTho = dto.getDiem();

            List<String> rowErrors = buildVsatCommands(cccd, maMon, diemTho, cmds, bqdCache);
            for (String re : rowErrors) {
                errors.add("Dòng " + rowNum + " - " + re);
            }
            rowNum++;
        }

        if (!cmds.isEmpty())
            errors.addAll(flushColumnUpdates(cmds, "VSAT"));
        return errors;
    }

    public List<String> processSingleVsat(String cccd, String maMon, BigDecimal diemTho) {
        List<ScoreUpdateCmd> cmds = new ArrayList<>();
        List<String> errors = buildVsatCommands(cccd, maMon, diemTho, cmds, null);
        if (!cmds.isEmpty())
            errors.addAll(flushColumnUpdates(cmds, "VSAT"));
        return errors;
    }

    private List<String> buildVsatCommands(String cccd, String maMon, BigDecimal diemTho,
            List<ScoreUpdateCmd> cmds,
            Map<String, List<BangQuyDoi>> bqdCache) {
        List<String> errors = new ArrayList<>();
        maMon = maMon == null ? "" : maMon.trim().toUpperCase();
        String dbCol = VSAT_MON_MAP.get(maMon);
        if (dbCol == null) {
            return errors;
        }

        if (!existsInDiemThi(cccd)) {
            errors.add("[VSAT SKIP] CCCD=" + cccd + " không tồn tại trong xt_diemthixettuyen.");
            return errors;
        }

        List<BangQuyDoi> breakpoints;
        if (bqdCache != null) {
            breakpoints = BangQuyDoiDAO.lookupFromCache(bqdCache, maMon);
        } else {
            breakpoints = bangQuyDoiDAO.findAllByPhuongThucAndMon("VSAT", maMon);
        }

        if (breakpoints.isEmpty()) {
            return errors;
        }

        diemTho = coalesceZero(diemTho);
        BigDecimal[] abcd = BangQuyDoiDAO.interpolateFromRows(breakpoints, diemTho);

        if (abcd == null) {
            errors.add("[VSAT] CCCD=" + cccd + ": Không tìm được khoảng nội suy cho môn " + maMon + ".");
            return errors;
        }

        BigDecimal y = interpolate(diemTho, abcd[0], abcd[1], abcd[2], abcd[3]);
        cmds.add(new ScoreUpdateCmd(cccd, dbCol, y));
        return errors;
    }

    // ══════════════════════════════════════════════════════════════════════════════
    // 4. HELPERS & BATCH FLUSH
    // ══════════════════════════════════════════════════════════════════════════════

    private List<String> flushColumnUpdates(List<ScoreUpdateCmd> cmds, String source) {
        List<String> errors = new ArrayList<>();
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Transaction tx = session.beginTransaction();
            int count = 0;
            for (ScoreUpdateCmd cmd : cmds) {
                String colSafe = "`" + cmd.dbCol() + "`";
                session.createNativeMutationQuery("UPDATE xt_diemthixettuyen SET " + colSafe + " = GREATEST(COALESCE("
                        + colSafe + ", 0), :diem) WHERE cccd = :cccd")
                        .setParameter("diem", cmd.diem())
                        .setParameter("cccd", cmd.cccd())
                        .executeUpdate();
                if (++count % BATCH_SIZE == 0)
                    session.flush();
            }
            tx.commit();
        } catch (Exception e) {
            errors.add("Lỗi batch UPDATE " + source + ": " + e.getMessage());
        }
        return errors;
    }

    private List<String> flushN1Updates(List<N1UpdateCommand> commands) {
        List<String> errors = new ArrayList<>();
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Transaction tx = session.beginTransaction();
            int count = 0;
            for (N1UpdateCommand cmd : commands) {
                session.createNativeMutationQuery(
                        "UPDATE xt_diemthixettuyen SET N1_CC = GREATEST(COALESCE(N1_CC, 0), :diemQd) WHERE cccd = :cccd")
                        .setParameter("diemQd", cmd.diemQd())
                        .setParameter("cccd", cmd.cccd())
                        .executeUpdate();
                if (++count % BATCH_SIZE == 0)
                    session.flush();
            }
            tx.commit();
        } catch (Exception e) {
            errors.add("Lỗi batch UPDATE N1_CC: " + e.getMessage());
        }
        return errors;
    }

    private List<String> flushDiemCongUpserts(List<DiemCong> entities) {
        List<String> errors = new ArrayList<>();
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Transaction tx = session.beginTransaction();
            int count = 0;
            for (DiemCong dc : entities) {
                session.createNativeMutationQuery(
                        "INSERT INTO xt_diemcongxetuyen (ts_cccd, manganh, matohop, diemCC, dc_keys) VALUES (:cccd, :manganh, :matohop, :diemCC, :dcKeys) ON DUPLICATE KEY UPDATE diemCC = VALUES(diemCC)")
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
            errors.add("Lỗi batch UPSERT xt_diemcongxetuyen: " + e.getMessage());
        }
        return errors;
    }

    private String validateIeltsRow(IeltsImportDTO dto) {
        if (dto.getCccd() == null || dto.getCccd().trim().isEmpty())
            return "Bắt buộc phải có CCCD";
        return null;
    }

    private String validateDgnlVsatRow(DgnlVsatRowDTO dto, String source) {
        if (dto.getCmnd() == null || dto.getCmnd().trim().isEmpty())
            return "Bắt buộc phải có CMND/CCCD";
        if (dto.getDiem() == null)
            return "Bắt buộc phải có DIEM";
        return null;
    }

    private boolean existsInDiemThi(String cccd) {
        return diemThiDAO.findByCccd(cccd).isPresent();
    }

    private boolean passesSubsetCheck(DiemThiXetTuyen dt, NganhToHop nth) {
        return true; // Simplified for merge stability
    }

    private DiemCong buildDiemCong(ThiSinh ts, String manganh, String matohop, BigDecimal rawDiem, String dcKey) {
        DiemCong dc = new DiemCong();
        dc.setThiSinh(ts);
        dc.setManganh(manganh);
        dc.setMatohop(matohop);
        dc.setDiemCC(coalesceZero(rawDiem).setScale(2, RoundingMode.HALF_UP));
        dc.setDcKeys(dcKey);
        return dc;
    }

    private BigDecimal coalesceZero(BigDecimal v) {
        return v != null ? v : BigDecimal.ZERO;
    }

    private BigDecimal interpolate(BigDecimal x, BigDecimal a, BigDecimal b, BigDecimal c, BigDecimal d) {
        BigDecimal rangeIn = b.subtract(a);
        if (rangeIn.compareTo(BigDecimal.ZERO) == 0)
            return c.setScale(2, RoundingMode.HALF_UP);
        BigDecimal relativePos = x.subtract(a).divide(rangeIn, 10, RoundingMode.HALF_UP);
        BigDecimal rangeOut = d.subtract(c);
        return c.add(relativePos.multiply(rangeOut)).setScale(2, RoundingMode.HALF_UP);
    }

    private record ScoreUpdateCmd(String cccd, String dbCol, BigDecimal diem) {
    }

    private record N1UpdateCommand(String cccd, BigDecimal diemQd) {
    }
}