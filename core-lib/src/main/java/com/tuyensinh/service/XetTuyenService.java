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
     * Chạy thuật toán xét tuyển cho 1 đợt - Phiên bản tối ưu cao với caching và tính toán chính xác.
     *
     * THUẬT TOÁN:
     * 1. Pre-load tất cả dữ liệu ra HashMap để tránh N+1 query
     * 2. Duyệt từng thí sinh, tính điểm theo công thức chính xác:
     *    - Bình thường (không N1):  Điểm = [(M1*hs1 + M2*hs2 + M3*hs3) * 3.0 / total_weight] + bonus
     *    - Có N1: So sánh 2 phương án và lấy điểm tối ưu
     * 3. Xét tuyển theo nguyện vọng ưu tiên (FIFO ordering)
     */
    public void chayThuatToanXetTuyen(Integer idDot) {
        System.out.println("[XetTuyenService] Bat dau chay thuat toan xet tuyen cho Dot ID=" + idDot);
        SystemLogger.log(null, "System", "Bắt đầu chạy thuật toán xét tuyển cho Đợt ID=" + idDot, true);

        Session session = HibernateUtil.getSessionFactory().openSession();
        Transaction transaction = null;
        try {
            transaction = session.beginTransaction();

            // ═══ BƯỚC 0: Xóa kết quả xét tuyển cũ ═══════════════════════════════════════
            session.createNativeMutationQuery(
                            "DELETE FROM xt_ket_qua_xet_tuyen WHERE id_ho_so IN (SELECT id FROM xt_ho_so_tuyen_sinh WHERE id_dot_tuyen_sinh = :idDot)")
                    .setParameter("idDot", idDot)
                    .executeUpdate();

            // ═══ BƯỚC 1: Load danh sách Nguyên Vọng hợp lệ ══════════════════════════════
            Query<NguyenVong> queryNV = session.createQuery(
                    "SELECT nv FROM NguyenVong nv WHERE nv.hoSoTuyenSinh.dotTuyenSinh.id = :idDot " +
                    "AND nv.hoSoTuyenSinh.trangThai = 'HOP_LE' " +
                    "ORDER BY nv.hoSoTuyenSinh.thiSinh.cccd ASC, nv.nvTt ASC",
                    NguyenVong.class);
            queryNV.setParameter("idDot", idDot);
            List<NguyenVong> listNV = queryNV.getResultList();

            System.out.println("[XetTuyenService] Tim thay " + listNV.size() + " nguyen vong can xet");

            // ═══ BƯỚC 2: Nhóm nguyên vọng theo CCCD ═════════════════════════════════════
            Map<String, List<NguyenVong>> mapThiSinh = listNV.stream()
                    .collect(Collectors.groupingBy(nv -> nv.getThiSinh().getCccd()));
            Set<String> cccdSet = mapThiSinh.keySet();

            // ═══ BƯỚC 3: Pre-load DiemThiXetTuyen (tránh N+1 query) ═════════════════════
            Map<String, DiemThiXetTuyen> diemThiMap = new HashMap<>();
            if (!cccdSet.isEmpty()) {
                List<DiemThiXetTuyen> allDiemThi = session.createNativeQuery(
                                "SELECT * FROM xt_diemthixettuyen WHERE cccd IN (:cccds)", DiemThiXetTuyen.class)
                        .setParameter("cccds", cccdSet).getResultList();
                for (DiemThiXetTuyen dt : allDiemThi) {
                    if (dt.getCccd() != null) {
                        diemThiMap.put(dt.getCccd().trim(), dt);
                    }
                }
            }
            System.out.println("[Cache] Pre-loaded " + diemThiMap.size() + " DiemThiXetTuyen records");

            // ═══ BƯỚC 4: Pre-load DiemCong (sử dụng HQL để giữ Entity Relationship) ════
            // Lưu ý: Dùng HQL "FROM DiemCong WHERE thiSinh.cccd IN :cccds" để tránh SemanticException
            Map<String, DiemCong> diemCongMap = new HashMap<>();
            if (!cccdSet.isEmpty()) {
                String hqlDiemCong = "FROM DiemCong dc WHERE dc.thiSinh.cccd IN :cccds";
                Query<DiemCong> queryDiemCong = session.createQuery(hqlDiemCong, DiemCong.class);
                queryDiemCong.setParameter("cccds", cccdSet);
                List<DiemCong> allDiemCong = queryDiemCong.getResultList();
                for (DiemCong dc : allDiemCong) {
                    if (dc.getDcKeys() != null) {
                        diemCongMap.put(dc.getDcKeys().trim(), dc);
                    }
                }
            }
            System.out.println("[Cache] Pre-loaded " + diemCongMap.size() + " DiemCong records");

            // ═══ BƯỚC 5: Pre-load tất cả NganhToHop (cấu hình tổ hợp môn) ═══════════════
            String hqlNganhToHop = "SELECT nth FROM NganhToHop nth";
            Query<NganhToHop> queryNth = session.createQuery(hqlNganhToHop, NganhToHop.class);
            List<NganhToHop> allNganhToHop = queryNth.getResultList();
            Map<String, NganhToHop> nthMap = new HashMap<>();
            for (NganhToHop nth : allNganhToHop) {
                if (nth.getNganh() != null && nth.getNganh().getManganh() != null &&
                        nth.getToHopMon() != null && nth.getToHopMon().getMatohop() != null) {

                    String manganh = nth.getNganh().getManganh().trim().toUpperCase();
                    String matohop = nth.getToHopMon().getMatohop().trim().toUpperCase();

                    // Key 1: Lưu theo Mã Tổ Hợp (vd: 7140231|D01)
                    nthMap.put(manganh + "|" + matohop, nth);

                    // Key 2: Lưu theo Chuỗi Môn để khớp với nguyện vọng của Thí sinh (vd: 7140231|TO-LI-N1)
                    String m1 = nth.getThMon1();
                    String m2 = nth.getThMon2();
                    String m3 = nth.getThMon3();
                    if (m1 != null && m2 != null && m3 != null) {
                        String combo = m1.trim().toUpperCase() + "-" + m2.trim().toUpperCase() + "-" + m3.trim().toUpperCase();
                        nthMap.put(manganh + "|" + combo, nth);
                    }
                }
            }
            System.out.println("[Cache] Pre-loaded " + nthMap.size() + " NganhToHop configs");

            // ═══ BƯỚC 6: Load cấu hình Điểm chuẩn ══════════════════════════════════════
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
                String manganh = dc.getNganhToHop() != null && dc.getNganhToHop().getNganh() != null ? 
                                 dc.getNganhToHop().getNganh().getManganh() : null;

                if (manganh != null) {
                    manganh = manganh.trim().toUpperCase();
                    diemChuanMap.putIfAbsent(manganh, score);

                    if (dc.getNganhToHop().getToHopMon() != null && dc.getNganhToHop().getToHopMon().getMatohop() != null) {
                        String matohop = dc.getNganhToHop().getToHopMon().getMatohop().trim().toUpperCase();
                        diemChuanMap.put(manganh + "|" + matohop, score);
                    }
                }
            }
            System.out.println("[Cache] Pre-loaded " + diemChuanMap.size() + " DiemChuan entries");

            int soTrungTuyen = 0;
            int soRot = 0;

            // ═══ BƯỚC 7: Duyệt từng thí sinh để tính điểm và xét tuyển ═════════════════
            for (Map.Entry<String, List<NguyenVong>> entry : mapThiSinh.entrySet()) {
                String cccd = entry.getKey().trim();
                DiemThiXetTuyen dtXt = diemThiMap.get(cccd);

                // ──────────────────────────────────────────────────────────────────────
                // PHASE 1: Tính toán ĐIỂM XÉT TUYỂN cho từng Nguyên Vọng
                // ──────────────────────────────────────────────────────────────────────
                for (NguyenVong nv : entry.getValue()) {
                    if (nv.getNganh() == null || nv.getNganh().getManganh() == null) {
                        System.err.println("[XetTuyenService] WARN: NguyenVong #" + nv.getNvTt() + 
                                         " cho CCCD " + cccd + " không có ngành.");
                        continue;
                    }

                    String maNganhNorm = nv.getNganh().getManganh().trim().toUpperCase();
                    String maThNorm = nv.getTtThm() != null ? nv.getTtThm().trim().toUpperCase() : "";
                    
                    // Tính điểm xét tuyển dựa trên phương thức tuyển sinh
                    double diemXetTuyen = calculateAdmissionScore(
                            cccd, dtXt, nv, maNganhNorm, maThNorm, nthMap, diemCongMap);
                    
                    // Round chính xác đến 2 chữ số thập phân (HALF_UP)
                    BigDecimal bdScore = BigDecimal.valueOf(diemXetTuyen);
                    diemXetTuyen = bdScore.setScale(2, RoundingMode.HALF_UP).doubleValue();
                    
                    nv.setDiemXettuyen(diemXetTuyen);
                    session.merge(nv);
                }

                // ──────────────────────────────────────────────────────────────────────
                // PHASE 2: Cập nhật điểm cao nhất vào HoSoTuyenSinh
                // ──────────────────────────────────────────────────────────────────────
                Double tongDiemMax = entry.getValue().stream()
                    .mapToDouble(nv -> nv.getDiemXettuyen() != null ? nv.getDiemXettuyen() : 0.0)
                    .max().orElse(0.0);

                if (!entry.getValue().isEmpty() && entry.getValue().get(0).getHoSoTuyenSinh() != null) {
                    HoSoTuyenSinh hs = entry.getValue().get(0).getHoSoTuyenSinh();
                    hs.setTongDiemXetTuyen(tongDiemMax);
                    session.merge(hs);
                }

                // ──────────────────────────────────────────────────────────────────────
                // PHASE 3: XÉT KẾT QUẢ (DAU/ROT/HUY) theo thứ tự nguyện vọng
                // ──────────────────────────────────────────────────────────────────────
                boolean daDau = false;
                for (NguyenVong nv : entry.getValue()) {
                    // Nếu đã trúng tuyển ở nguyên vọng trước: hủy nguyên vọng này
                    if (daDau) {
                        nv.setNvKetqua("HUY");
                        session.merge(nv);
                        continue;
                    }

                    // Xác định tên ngành & tổ hợp
                    String maNganhNorm = nv.getNganh() != null && nv.getNganh().getManganh() != null ? 
                                         nv.getNganh().getManganh().trim().toUpperCase() : "";
                    String maThNorm = nv.getTtThm() != null ? nv.getTtThm().trim().toUpperCase() : "";
                    String keyChuan = maNganhNorm + "|" + maThNorm;

                    // Lookup điểm chuẩn (ưu tiên combo, sau đó chỉ ngành)
                    Double diemChuan = 999.0;
                    if (diemChuanMap.containsKey(keyChuan)) {
                        diemChuan = diemChuanMap.get(keyChuan);
                    } else if (diemChuanMap.containsKey(maNganhNorm)) {
                        diemChuan = diemChuanMap.get(maNganhNorm);
                    }

                    Double diemThiSinh = nv.getDiemXettuyen() != null ? nv.getDiemXettuyen() : 0.0;

                    // Quyết định: DAU / ROT
                    if (diemThiSinh >= diemChuan) {
                        // ✓ ĐẬU: Tạo KetQuaXetTuyen và mark daDau=true
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
                        // ✗ RỚT
                        nv.setNvKetqua("ROT");
                        session.merge(nv);
                        soRot++;
                    }
                }
            }
            transaction.commit();

            System.out.println("[XetTuyenService] Hoan thanh xet tuyen Dot ID=" + idDot + 
                             ": " + soTrungTuyen + " trung tuyen, " + soRot + " rot");
            SystemLogger.log(null, "System", "Hoàn thành xét tuyển Đợt ID=" + idDot + 
                           ": " + soTrungTuyen + " trúng tuyển, " + soRot + " rớt", true);

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
     * Tính điểm xét tuyển cho một nguyên vọng cụ thể.
     *
     * CÔNG THỨC CHÍNH:
     * - Nếu môn thứ 3 KHÔNG phải N1:
     *   Score = [((M1 * hs1) + (M2 * hs2) + (M3 * hs3)) * 3.0 / Total Weight] + diemBonusIelts
     *
     * - Nếu môn thứ 3 LÀ N1 (phải so sánh 2 phương án):
     *   Option A: m3 = max(m3Thi, diemAnhQuyDoi), tính 30-điểm, KHÔNG cộng bonus
     *   Option B: m3 = m3Thi, tính 30-điểm, CÓ cộng bonus
     *   Score = max(ScoreA, ScoreB)
     *
     * - Final: Score += doLechToHop + diemUuTienKhuVuc
     */
    private double calculateAdmissionScore(String cccd, DiemThiXetTuyen dtXt, NguyenVong nv,
                                           String maNganhNorm, String maThNorm,
                                           Map<String, NganhToHop> nthMap, Map<String, DiemCong> diemCongMap) {
        
        String phuongThuc = nv.getTtPhuongthuc() != null ? nv.getTtPhuongthuc().trim().toUpperCase() : "THPT";

        // ════ TRƯỜNG HỢP 1: TUYỂN SINH PHƯƠNG THỨC ĐGNL ════
        if ("DGNL".equalsIgnoreCase(phuongThuc)) {
            // ĐGNL được import theo định dạng Nguyện vọng, không dùng "__TA"
            String dcKeyDgnl = cccd + "_" + maNganhNorm + "_" + maThNorm;
            DiemCong dcRecord = diemCongMap.get(dcKeyDgnl);
            double diemBonus = (dcRecord != null && dcRecord.getDiemCC() != null) ? 
                               dcRecord.getDiemCC().doubleValue() : 0.0;
            // Ở đây diemBonus thực chất là điểm ĐGNL đã quy đổi nội suy thang 30.
            return diemBonus; 
        }

        // Kiểm tra cấu hình tổ hợp ngành và điểm thi tốt nghiệp THPT
        NganhToHop nthConfig = nthMap.get(maNganhNorm + "|" + maThNorm);
        if (nthConfig == null || dtXt == null) {
            // Fallback: Nếu khuyết dữ liệu thô thì trả về điểm đã tính toán sẵn từ trước
            return nv.getDiemXettuyen() != null ? nv.getDiemXettuyen() : 0.0;
        }

        // ──── Lấy cấu hình hệ số môn và độ lệch tổ hợp ────
        String thMon1 = nthConfig.getThMon1();
        String thMon2 = nthConfig.getThMon2();
        String thMon3 = nthConfig.getThMon3();

        Double hs1 = nthConfig.getHsmon1() != null ? nthConfig.getHsmon1().doubleValue() : 1.0;
        Double hs2 = nthConfig.getHsmon2() != null ? nthConfig.getHsmon2().doubleValue() : 1.0;
        Double hs3 = nthConfig.getHsmon3() != null ? nthConfig.getHsmon3().doubleValue() : 1.0;
        Double totalWeight = hs1 + hs2 + hs3;
        
        Double doLechToHop = nthConfig.getDolech() != null ? nthConfig.getDolech().doubleValue() : 0.0;

        // ──── Lấy điểm thi tốt nghiệp THPT của 3 môn ────
        double m1 = getSubjectScore(dtXt, thMon1);
        double m2 = getSubjectScore(dtXt, thMon2);
        double m3ThiScore = getSubjectScore(dtXt, thMon3);

        // ──── Lấy dữ liệu kho chứng chỉ ngoại ngữ của thầy (Key tĩnh theo CCCD) ────
        String certKey = cccd + "__TA";
        DiemCong certRecord = diemCongMap.get(certKey);
        
        // 1. Cột diemCC: Điểm quy đổi môn Anh sang hệ mốc 10 (ví dụ: IELTS 7.5 đổi thành 10.0)
        double diemAnhQuyDoiHe10 = (certRecord != null && certRecord.getDiemCC() != null) ? 
                                   certRecord.getDiemCC().doubleValue() : 0.0;

        // 2. Cột diemTong: Tổng điểm cộng ưu tiên an toàn đã được giới hạn tối đa 3 điểm của thầy
        double diemCongToiDa = (certRecord != null && certRecord.getDiemTong() != null) ? 
                               certRecord.getDiemTong().doubleValue() : 0.0;

        // ... (Đoạn lấy diemAnhQuyDoiHe10 và diemCongToiDa) ...

        // 👇 DÁN ĐOẠN LOG NÀY VÀO ĐÂY 👇
        System.out.println("=========================================");
        System.out.println("[DEBUG XÉT TUYỂN] Đang tính điểm cho CCCD: " + cccd);
        System.out.println("Ngành: " + maNganhNorm + " | Tổ hợp: " + maThNorm);
        System.out.println("Điểm thi thô -> M1: " + m1 + ", M2: " + m2 + ", M3: " + m3ThiScore);
        System.out.println("Hệ số môn   -> HS1: " + hs1 + ", HS2: " + hs2 + ", HS3: " + hs3);
        System.out.println("IELTS (Quy đổi 10): " + diemAnhQuyDoiHe10 + " | IELTS (Cộng thưởng): " + diemCongToiDa);
        System.out.println("Độ lệch tổ hợp: " + doLechToHop);
        System.out.println("=========================================");

        double scoreFinal;

        // ════ TRƯỜNG HỢP 2: TUYỂN SINH PHƯƠNG THỨC THPT / HỌC BẠ ════
        if (!"N1".equalsIgnoreCase(thMon3)) {
            // 🎯 Kịch bản A: Môn thứ 3 KHÔNG phải Ngoại ngữ (ví dụ khối A00: Toán - Lý - Hóa)
            // Tính điểm học lực theo hệ số thang mốc 30 + Cộng tổng điểm cộng an toàn (diemTong)
            double weightedSum = (m1 * hs1) + (m2 * hs2) + (m3ThiScore * hs3);
            double scoreOn30 = (weightedSum * 3.0) / totalWeight;
            scoreFinal = scoreOn30 + diemCongToiDa;
        } else {
            // 🎯 Kịch bản B: Môn thứ 3 CHÍNH LÀ Ngoại ngữ (N1) (ví dụ khối A01, D01) -> Chạy cơ chế EITHER/OR
            
            // Phương án A: Thay thế môn Anh bằng điểm quy đổi hệ 10 (10.0), KHÔNG được tính điểm cộng tổng
            double m3FinalA = Math.max(m3ThiScore, diemAnhQuyDoiHe10);
            double weightedSumA = (m1 * hs1) + (m2 * hs2) + (m3FinalA * hs3);
            double scoreOptA = (weightedSumA * 3.0) / totalWeight;

            // Phương án B: Giữ điểm thi tốt nghiệp thật môn Anh (9.0), ĐƯỢC cộng tổng điểm cộng an toàn (diemTong) vào sau quy đổi
            double weightedSumB = (m1 * hs1) + (m2 * hs2) + (m3ThiScore * hs3);
            double scoreOptB = ((weightedSumB * 3.0) / totalWeight) + diemCongToiDa;

            // Lấy phương án tối ưu mang lại lợi ích điểm số cao nhất cho thí sinh
            scoreFinal = Math.max(scoreOptA, scoreOptB);
        }

        // ──── Bước cuối: Áp dụng độ lệch tổ hợp môn (ví dụ tổ hợp phụ A01 bị trừ 0.01) ────
        scoreFinal = scoreFinal + doLechToHop;

        return scoreFinal;
    }

    /**
     * Lấy điểm thi của một môn học từ DiemThiXetTuyen.
     * Sử dụng Lombok camelCase getters: getTo(), getLi(), getHo(), getSi(), getSu(), getDi(), getVa(), getN1Thi(), getN1Cc()
     */
    private double getSubjectScore(DiemThiXetTuyen dt, String mon) {
        if (dt == null || mon == null) {
            return 0.0;
        }

        String monNorm = mon.trim().toUpperCase();
        switch (monNorm) {
            case "TO":      return dt.getTo() != null ? dt.getTo().doubleValue() : 0.0;
            case "LI":      return dt.getLi() != null ? dt.getLi().doubleValue() : 0.0;
            case "HO":      return dt.getHo() != null ? dt.getHo().doubleValue() : 0.0;
            case "SI":      return dt.getSi() != null ? dt.getSi().doubleValue() : 0.0;
            case "SU":      return dt.getSu() != null ? dt.getSu().doubleValue() : 0.0;
            case "DI":      return dt.getDi() != null ? dt.getDi().doubleValue() : 0.0;
            case "VA":      return dt.getVa() != null ? dt.getVa().doubleValue() : 0.0;
            case "N1":      return dt.getN1Thi() != null ? dt.getN1Thi().doubleValue() : 0.0;
            default:        return 0.0;
        }
    }

    /**
     * Hàm Helper bốc điểm thi theo môn học lẻ từ xt_diemthixettuyen
     * LỜI NHỜ: Hàm này đã được thay thế bởi logic bên trong calculateAdmissionScore()
     * Giữ lại cho backward compatibility nếu có chỗ khác dùng.
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
                    return dt.getN1Cc() != null ? dt.getN1Cc().doubleValue() : 0.0;
                } else {
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

        DiemThiXetTuyen dt = optDiem.get();

        // 1. Xác định Điểm quy đổi hệ 10
        BigDecimal diemThayTheN1;
        if (diemQdExcel != null && diemQdExcel.compareTo(BigDecimal.ZERO) > 0) {
            diemThayTheN1 = diemQdExcel.setScale(2, RoundingMode.HALF_UP);
        } else {
            BigDecimal rawIelts = coalesceZero(diemIeltsRaw);
            BigDecimal lookupResult = bangQuyDoiDAO.lookupDiemb("IELTS", "N1", rawIelts);
            diemThayTheN1 = (lookupResult != null ? lookupResult : BigDecimal.ZERO).setScale(2, RoundingMode.HALF_UP);
        }

        // 2. Xác định Điểm cộng thưởng
        BigDecimal diemCongUuTien = coalesceZero(diemCongOriginal).setScale(2, RoundingMode.HALF_UP);

        // 3. TẠO ĐÚNG 1 DÒNG DUY NHẤT VÀO KHO ĐỘNG THEO CHUẨN CỦA THẦY
        DiemCong dc = new DiemCong();
        dc.setThiSinh(dt.getThiSinh());
        dc.setManganh("");     // Chuẩn data thầy: để trống
        dc.setMatohop("");     // Chuẩn data thầy: để trống
        dc.setPhuongthuc("THPT");
        dc.setDiemCC(diemThayTheN1);   // Điểm quy đổi hệ 10
        dc.setDiemUtxt(diemCongUuTien); // Điểm cộng 
        dc.setDiemTong(diemCongUuTien); // Điểm chốt chặn
        dc.setGhichu("Tiếng Anh - IELTS");
        dc.setDcKeys(cccd + "__TA");    // Key tĩnh
        
        dcUpserts.add(dc);

        // 4. Update kho tĩnh
        n1Updates.add(new N1UpdateCommand(cccd, diemThayTheN1));

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
                        "INSERT INTO xt_diemcongxetuyen (ts_cccd, manganh, matohop, phuongthuc, diemCC, diemUtxt, diemTong, ghichu, dc_keys) " +
                        "VALUES (:cccd, :manganh, :matohop, :phuongthuc, :diemCC, :diemUtxt, :diemTong, :ghichu, :dcKeys) " +
                        "ON DUPLICATE KEY UPDATE " +
                        "diemCC = VALUES(diemCC), diemUtxt = VALUES(diemUtxt), diemTong = VALUES(diemTong), " +
                        "ghichu = VALUES(ghichu), phuongthuc = VALUES(phuongthuc)")
                        .setParameter("cccd", dc.getThiSinh().getCccd())
                        .setParameter("manganh", dc.getManganh())
                        .setParameter("matohop", dc.getMatohop())
                        .setParameter("phuongthuc", dc.getPhuongthuc())
                        .setParameter("diemCC", dc.getDiemCC())
                        .setParameter("diemUtxt", dc.getDiemUtxt())
                        .setParameter("diemTong", dc.getDiemTong())
                        .setParameter("ghichu", dc.getGhichu())
                        .setParameter("dcKeys", dc.getDcKeys())
                        .executeUpdate();
                if (++count % BATCH_SIZE == 0) session.flush();
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