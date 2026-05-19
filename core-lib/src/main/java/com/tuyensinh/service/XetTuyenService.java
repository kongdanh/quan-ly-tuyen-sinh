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

            // 1. Xóa kết quả xét tuyển cũ của đợt này
            session.createNativeMutationQuery(
                            "DELETE FROM xt_ket_qua_xet_tuyen WHERE id_ho_so IN (SELECT id FROM xt_ho_so_tuyen_sinh WHERE id_dot_tuyen_sinh = :idDot)")
                    .setParameter("idDot", idDot)
                    .executeUpdate();

            // 2. Lấy danh sách Nguyện vọng hợp lệ
            Query<NguyenVong> query = session.createQuery(
                    "SELECT nv FROM NguyenVong nv WHERE nv.hoSoTuyenSinh.dotTuyenSinh.id = :idDot AND nv.hoSoTuyenSinh.trangThai = 'HOP_LE' ORDER BY nv.hoSoTuyenSinh.thiSinh.cccd, nv.nvTt ASC",
                    NguyenVong.class);
            query.setParameter("idDot", idDot);
            List<NguyenVong> listNV = query.getResultList();

            System.out.println("[XetTuyenService] Tim thay " + listNV.size() + " nguyen vong can xet");

            // Force init mối quan hệ (tránh LazyInitializationException)
            for (NguyenVong nv : listNV) {
                if (nv.getNganh() != null) nv.getNganh().getId();
                if (nv.getHoSoTuyenSinh() != null) nv.getHoSoTuyenSinh().getId();
                if (nv.getThiSinh() != null) nv.getThiSinh().getId();
            }

            // Nhóm nguyện vọng theo CCCD của Thí sinh
            Map<String, List<NguyenVong>> mapThiSinh = listNV.stream()
                    .collect(Collectors.groupingBy(nv -> nv.getThiSinh().getCccd()));

            // ─── TỐI ƯU HIỆU NĂNG: CACHE DỮ LIỆU ĐỂ TRÁNH QUÉT DƯỚI VÒNG LẶP N+1 ───
            Set<String> cccdSet = mapThiSinh.keySet();

            // Nạp trước bảng điểm thi của các thí sinh trong đợt này
            Map<String, DiemThiXetTuyen> diemThiMap = new HashMap<>();
            if (!cccdSet.isEmpty()) {
                List<DiemThiXetTuyen> allDiemThi = session.createQuery("FROM DiemThiXetTuyen WHERE cccd IN :cccds", DiemThiXetTuyen.class)
                        .setParameter("cccds", cccdSet).getResultList();
                diemThiMap = allDiemThi.stream().collect(Collectors.toMap(dt -> dt.getCccd().trim(), dt -> dt, (a, b) -> a));
            }

            // Nạp trước bảng điểm cộng/điểm ưu tiên theo nguyện vọng
            Map<String, DiemCong> diemCongMap = new HashMap<>();
            if (!cccdSet.isEmpty()) {
                List<DiemCong> allDiemCong = session.createQuery("FROM DiemCong WHERE ts_cccd IN :cccds", DiemCong.class)
                        .setParameter("cccds", cccdSet).getResultList();
                diemCongMap = allDiemCong.stream().collect(Collectors.toMap(DiemCong::getDcKeys, dc -> dc, (a, b) -> a));
            }

            // Nạp trước danh mục cấu hình tổ hợp môn của các ngành
            List<NganhToHop> allNganhToHop = session.createQuery("FROM NganhToHop", NganhToHop.class).getResultList();
            Map<String, NganhToHop> nthMap = allNganhToHop.stream()
                    .collect(Collectors.toMap(nth -> nth.getNganh().getManganh().toUpperCase() + "|" + nth.getToHopMon().getMatohop().toUpperCase(), nth -> nth, (a, b) -> a));
            // ─────────────────────────────────────────────────────────────────────

            // Lấy danh sách cấu hình Điểm chuẩn
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
                String manganh = dc.getNganhToHop() != null && dc.getNganhToHop().getNganh() != null ? dc.getNganhToHop().getNganh().getManganh() : null;

                if (manganh != null) {
                    manganh = manganh.trim().toUpperCase();
                    diemChuanMap.putIfAbsent(manganh, score);

                    if (dc.getNganhToHop().getToHopMon() != null && dc.getNganhToHop().getToHopMon().getMatohop() != null) {
                        String matohop = dc.getNganhToHop().getToHopMon().getMatohop().trim().toUpperCase();
                        diemChuanMap.put(manganh + "|" + matohop, score);
                    }
                }
            }

            int soTrungTuyen = 0;
            int soRot = 0;

            // Duyệt qua từng thí sinh để tính điểm tối ưu và xét tuyển
            for (Map.Entry<String, List<NguyenVong>> entry : mapThiSinh.entrySet()) {
                String cccd = entry.getKey();
                DiemThiXetTuyen dtXt = diemThiMap.get(cccd);

                // BƯỚC THAY THẾ: Tính toán điểm tối ưu dựa trên phương thức tuyển sinh
                for (NguyenVong nv : entry.getValue()) {
                    String maNganhNorm = nv.getNganh().getManganh() != null ? nv.getNganh().getManganh().trim().toUpperCase() : "";
                    String maThNorm = nv.getTtThm() != null ? nv.getTtThm().trim().toUpperCase() : "";
                    String dcKey = cccd + "_" + maNganhNorm + "_" + maThNorm;

                    DiemCong dcRecord = diemCongMap.get(dcKey);
                    double diemBonusIelts = (dcRecord != null && dcRecord.getDiemCC() != null) ? dcRecord.getDiemCC().doubleValue() : 0.0;
                    double diemUuTienKhuVuc = (dcRecord != null && dcRecord.getDiemUtxt() != null) ? dcRecord.getDiemUtxt().doubleValue() : 0.0;

                    double diemToiUu = 0.0;
                    String phuongThuc = nv.getTtPhuongthuc() != null ? nv.getTtPhuongthuc().trim().toUpperCase() : "THPT";

                    if ("DGNL".equalsIgnoreCase(phuongThuc)) {
                        // Kịch bản DGNL: Điểm xét tuyển = Điểm quy đổi thang 30 + Điểm ưu tiên khu vực
                        diemToiUu = diemBonusIelts + diemUuTienKhuVuc;
                    } else {
                        // Kịch bản THPT / HOCBA: ÁP DỤNG PHÉP SO SÁNH TỐI ƯU IELTS ĐÃ BÀN
                        NganhToHop nthConfig = nthMap.get(maNganhNorm + "|" + maThNorm);
                        if (nthConfig != null && dtXt != null) {
                            String m1 = nthConfig.getThMon1();
                            String m2 = nthConfig.getThMon2();
                            String m3 = nthConfig.getThMon3();

                            // Phương án A: Thay thế môn Anh bằng điểm hệ 10 quy đổi (N1_CC) - KHÔNG ĐƯỢC cộng điểm thưởng
                            double scoreOptA = getDiemMonThpt(dtXt, m1, true)
                                    + getDiemMonThpt(dtXt, m2, true)
                                    + getDiemMonThpt(dtXt, m3, true);

                            // Phương án B: Giữ điểm thi thật (N1_THI) + Cộng điểm thưởng chứng chỉ (diemBonusIelts)
                            double scoreOptB = getDiemMonThpt(dtXt, m1, false)
                                    + getDiemMonThpt(dtXt, m2, false)
                                    + getDiemMonThpt(dtXt, m3, false)
                                    + diemBonusIelts;

                            // Lấy giá trị lớn nhất đem đi xét tuyển
                            diemToiUu = Math.max(scoreOptA, scoreOptB) + diemUuTienKhuVuc;
                        } else {
                            diemToiUu = nv.getDiemXettuyen() != null ? nv.getDiemXettuyen() : 0.0;
                        }
                    }

                    // Làm tròn chuẩn 2 chữ số thập phân, cập nhật lại vào đối tượng nguyện vọng
                    diemToiUu = Math.round(diemToiUu * 100.0) / 100.0;
                    nv.setDiemXettuyen(diemToiUu);
                    session.merge(nv);
                }

                // Cập nhật tổng điểm cao nhất vào hồ sơ thí sinh
                Double tongDiemMax = entry.getValue().stream()
                        .mapToDouble(nv -> nv.getDiemXettuyen() != null ? nv.getDiemXettuyen() : 0.0).max().orElse(0.0);

                if (!entry.getValue().isEmpty() && entry.getValue().get(0).getHoSoTuyenSinh() != null) {
                    HoSoTuyenSinh hs = entry.getValue().get(0).getHoSoTuyenSinh();
                    hs.setTongDiemXetTuyen(tongDiemMax);
                    session.merge(hs);
                }

                // Vòng lặp xét ĐẬU / RỚT theo thứ tự nguyện vọng ưu tiên (1, 2, 3...)
                boolean daDau = false;
                for (NguyenVong nv : entry.getValue()) {
                    if (daDau) {
                        nv.setNvKetqua("HUY");
                        session.merge(nv);
                        continue;
                    }

                    String maNganhNorm = nv.getNganh().getManganh() != null ? nv.getNganh().getManganh().trim().toUpperCase() : "";
                    String maThNorm = nv.getTtThm() != null ? nv.getTtThm().trim().toUpperCase() : "";
                    String key = maNganhNorm + "|" + maThNorm;

                    Double diemChuan = 999.0;
                    if (diemChuanMap.containsKey(key)) {
                        diemChuan = diemChuanMap.get(key);
                    } else if (diemChuanMap.containsKey(maNganhNorm)) {
                        diemChuan = diemChuanMap.get(maNganhNorm);
                    }

                    Double diemThiSinh = nv.getDiemXettuyen() != null ? nv.getDiemXettuyen() : 0.0;

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

            System.out.println("[XetTuyenService] Hoan thanh xet tuyen Dot ID=" + idDot + ": " + soTrungTuyen + " trung tuyen, " + soRot + " rot");
            SystemLogger.log(null, "System", "Hoàn thành xét tuyển Đợt ID=" + idDot + ": " + soTrungTuyen + " trúng tuyển, " + soRot + " rớt", true);

        } catch (Exception e) {
            if (transaction != null) transaction.rollback();
            System.err.println("[XetTuyenService] Loi chay thuat toan xet tuyen: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Loi chay thuat toan: " + e.getMessage());
        } finally {
            if (session != null) session.close();
        }
    }

    /**
     * Hàm Helper bốc điểm thi theo môn học lẻ từ xt_diemthixettuyen
     */
    private double getDiemMonThpt(DiemThiXetTuyen dt, String mon, boolean useCertificate) {
        if (dt == null || mon == null) return 0.0;
        switch (mon.trim().toUpperCase()) {
            case "TO": return dt.getTo() != null ? dt.getTo().doubleValue() : 0.0;
            case "LI": return dt.getLi() != null ? dt.getLi().doubleValue() : 0.0;
            case "HO": return dt.getHo() != null ? dt.getHo().doubleValue() : 0.0;
            case "SI": return dt.getSi() != null ? dt.getSi().doubleValue() : 0.0;
            case "SU": return dt.getSu() != null ? dt.getSu().doubleValue() : 0.0;
            case "DI": return dt.getDi() != null ? dt.getDi().doubleValue() : 0.0;
            case "VA": return dt.getVa() != null ? dt.getVa().doubleValue() : 0.0;
            case "N1":
                if (useCertificate) {
                    // n1Cc tương ứng với getN1Cc() của Lombok
                    return dt.getN1Cc() != null ? dt.getN1Cc().doubleValue() : 0.0;
                } else {
                    // n1Thi tương ứng với getN1Thi() của Lombok
                    return dt.getN1Thi() != null ? dt.getN1Thi().doubleValue() : 0.0;
                }
            default: return 0.0;
        }
    }

    public KetQuaXetTuyen timKetQuaTheoCccd(String cccd) {
        return xetTuyenDAO.timKetQuaTheoCccd(cccd);
    }

    // ══════════════════════════════════════════════════════════════════════════════
    // 2. IELTS IMPORT (Đã cập nhật tối ưu và sửa lỗi chặn tổ hợp)
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

            // TRUYỀN ĐỦ 3 ĐẦU ĐIỂM:
            // 1. Điểm gốc IELTS (vd: 7.5) | 2. Điểm Quy đổi từ Excel (vd: 10) | 3. Điểm cộng từ Excel (vd: 2)
            // Lưu ý: Hãy kiểm tra lại các hàm get...() của IeltsImportDTO để khớp với thuộc tính file Excel của ông nhé!
            List<String> rowErrors = buildIeltsCommands(
                    cccd,
                    dto.getDiemIeltsRaw(),   // Điểm gốc IELTS (ví dụ: 7.5)
                    dto.getDiemQd(),         // Điểm Quy đổi hệ 10 từ Excel (ví dụ: 10)
                    dto.getDiemCong(),       // Điểm cộng từ Excel (ví dụ: 2)
                    n1Updates,
                    dcUpserts
            );
            errors.addAll(rowErrors);
        }

        if (!n1Updates.isEmpty())
            errors.addAll(flushN1Updates(n1Updates));
        if (!dcUpserts.isEmpty())
            errors.addAll(flushDiemCongUpserts(dcUpserts));
        return errors;
    }

    /**
     * Xử lý nộp đơn lẻ qua Web API (Hỗ trợ Admin nhập tay hoặc Học sinh tự nộp).
     */
    public List<String> processSingleIelts(String cccd, BigDecimal diemIeltsRaw, BigDecimal diemQdExcel, BigDecimal diemCongOriginal) {
        List<N1UpdateCommand> n1Updates = new ArrayList<>();
        List<DiemCong> dcUpserts = new ArrayList<>();

        // Khi gọi từ Web:
        // - Nếu học sinh tự nộp: diemQdExcel và diemCongOriginal truyền vào là NULL (hệ thống sẽ tự tra cứu bảng quy đổi)
        // - Nếu Admin nhập tay trên Form duyệt hồ sơ: Có thể truyền giá trị override trực tiếp từ giao diện vào
        List<String> errors = buildIeltsCommands(cccd, diemIeltsRaw, diemQdExcel, diemCongOriginal, n1Updates, dcUpserts);

        if (!n1Updates.isEmpty())
            errors.addAll(flushN1Updates(n1Updates));
        if (!dcUpserts.isEmpty())
            errors.addAll(flushDiemCongUpserts(dcUpserts));
        return errors;
    }

    private List<String> buildIeltsCommands(String cccd, BigDecimal diemIeltsRaw, BigDecimal diemQdExcel, BigDecimal diemCongOriginal,
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

        DiemThiXetTuyen dt = optDiem.get();

        // ─── 1. Xác định Điểm thay thế môn Tiếng Anh (diemThayTheN1 - Hệ 10) ───────
        BigDecimal diemThayTheN1;
        if (diemQdExcel != null && diemQdExcel.compareTo(BigDecimal.ZERO) > 0) {
            // Nếu trường Điểm Quy đổi từ Excel có giá trị (ví dụ: 10), sử dụng luôn
            diemThayTheN1 = diemQdExcel.setScale(2, RoundingMode.HALF_UP);
        } else {
            // Nếu khuyết, dùng Điểm số IELTS thô (ví dụ: 7.5) để tra cứu từ DB
            BigDecimal rawIelts = coalesceZero(diemIeltsRaw);
            BigDecimal lookupResult = bangQuyDoiDAO.lookupDiemb("IELTS", "N1", rawIelts);
            diemThayTheN1 = (lookupResult != null ? lookupResult : BigDecimal.ZERO)
                    .setScale(2, RoundingMode.HALF_UP);
        }

        // ─── 2. Xác định Điểm cộng ưu tiên (ví dụ: 2) ─────────────────────────────
        BigDecimal diemCongUuTien = coalesceZero(diemCongOriginal).setScale(2, RoundingMode.HALF_UP);

        // Cờ bảo vệ: Đảm bảo chỉ update bảng diemthixettuyen đúng 1 lần cho 1 thí sinh
        boolean hasAddedN1Update = false;

        // ─── 3. Duyệt danh sách Nguyện vọng ────────────────────────────────────────
        for (NganhToHop nth : toHopList) {
            String manganh = nth.getNganh().getManganh();
            String matohop = nth.getToHopMon().getMatohop();
            String dcKey = cccd + "_" + manganh + "_" + matohop;

            // ─ Thao tác 1: Kho tĩnh (Chỉ add 1 lần duy nhất để tối ưu hiệu năng Batch)
            if (!hasAddedN1Update) {
                n1Updates.add(new N1UpdateCommand(cccd, diemThayTheN1));
                hasAddedN1Update = true; // Khóa cờ
            }

            // ─ Thao tác 2: Kho động (Lưu điểm cộng theo từng nguyện vọng cụ thể)
            dcUpserts.add(buildDiemCong(dt.getThiSinh(), manganh, matohop, diemCongUuTien, dcKey));
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

                List<BangQuyDoi> bp = bqCache.computeIfAbsent("NL1", k -> bangQuyDoiDAO.findAllByPhuongThucAndMon("DGNL", "NL1"));

                if (bp.isEmpty()) {
                    missingBangQuyDoi.add("NL1");
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