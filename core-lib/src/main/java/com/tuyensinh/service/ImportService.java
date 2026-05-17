package com.tuyensinh.service;

import com.tuyensinh.dto.DiemThiImportDTO;
import com.tuyensinh.dto.ThiSinhImportDTO;
import com.tuyensinh.dao.DiemThiXetTuyenDAO;
import com.tuyensinh.model.DiemThiXetTuyen;
import com.tuyensinh.model.ThiSinh;
import com.tuyensinh.model.ThiSinhAccount;
import com.tuyensinh.util.HibernateUtil;
import com.tuyensinh.util.PasswordUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;

import java.io.File;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ImportService {

    private final BaseImportService<ThiSinhImportDTO, ThiSinh> baseImportService;

    private static final Set<String> THPT_KEYS = new HashSet<>(Arrays.asList(
        "TO","LI","HO","SI","VA","SU","DI","KTPL","N1_THI","N1_CC","TI"
    ));
    private static final Set<String> VSAT_KEYS = new HashSet<>(Arrays.asList(
        "NK1","NK2","N1_THI","N1_CC","TI"
    ));
    private static final Set<String> DGNL_KEYS = new HashSet<>(Arrays.asList(
        "NL1","TI","KTPL","CNCN","CNNN"
    ));

    public ImportService() {
        this.baseImportService = new BaseImportService<>();
    }

    public List<String> importThiSinh(File file) {
        return baseImportService.importFromExcel(
                file,
                ThiSinhImportDTO.class,
                
                // 1. MAPPER
                dto -> {
                    ThiSinh ts = new ThiSinh();
                    
                    ts.setCccd(dto.getCccd());
                    ts.setSobaodanh(dto.getSoBaoDanh());
                    ts.setHo(dto.getHo());
                    ts.setTen(dto.getTen());
                    
                    String ngaySinh = dto.getNgaySinh(); 
                    ts.setNgaySinh(ngaySinh);
                    
                    ts.setDienThoai(dto.getDienThoai());
                    ts.setGioiTinh(dto.getGioiTinh());
                    ts.setEmail(dto.getEmail());

                    // Tạo Account
                    ThiSinhAccount account = new ThiSinhAccount();
                    String rawPassword = (ngaySinh != null && !ngaySinh.isEmpty()) ? ngaySinh : "123456"; 
                    account.setPasswordHash(PasswordUtil.hash(rawPassword)); 

                    account.setThiSinh(ts);
                    ts.setAccount(account);

                    return ts;
                },
                
                // 2. SAVE CONSUMER
                entities -> {
                    Session session = null;
                    Transaction tx = null;
                    try {
                        // Mở Session thủ công, KHÔNG dùng try-with-resources
                        session = HibernateUtil.getSessionFactory().openSession();
                        tx = session.beginTransaction();
                        
                        int count = 0;
                        for (ThiSinh entity : entities) {
                            session.persist(entity); 
                            
                            // Batch processing
                            if (++count % 50 == 0) {
                                session.flush();
                                session.clear();
                            }
                        }
                        
                        // Nếu vòng lặp chạy ok -> Chốt lưu
                        tx.commit(); 
                        
                    } catch (Exception e) {
                        // Xảy ra lỗi -> Rollback ngay lập tức KHI SESSION VẪN CÒN ĐANG MỞ
                        if (tx != null && tx.isActive()) {
                            try {
                                tx.rollback();
                            } catch (Exception rollbackEx) {
                                // Bắt luôn lỗi rác nếu rollback thất bại để không đè mất lỗi chính
                                System.err.println("Lỗi Rollback: " + rollbackEx.getMessage());
                            }
                        }
                        
                        // Ném lỗi GỐC ra ngoài để UI hiển thị. (In thêm StackTrace ra console để bạn dễ debug)
                        e.printStackTrace(); 
                        throw new RuntimeException("Lỗi lưu Database (Đã Rollback an toàn): " + e.getMessage());
                        
                    } finally {
                        // Dọn dẹp: Đảm bảo Session LÀ THỨ CUỐI CÙNG bị đóng
                        if (session != null && session.isOpen()) {
                            session.close();
                        }
                    }
                },
                
                // 3. VALIDATOR
                dto -> {
                    if (dto.getCccd() == null || dto.getCccd().trim().isEmpty()) return "Bắt buộc phải có số CCCD";
                    if (dto.getTen() == null || dto.getTen().trim().isEmpty()) return "Bắt buộc phải có Tên";
                    return null; 
                }
        );
    }

    public List<String> importDiemThi(File file) {
        BaseImportService<DiemThiImportDTO, DiemThiXetTuyen> diemImportService = new BaseImportService<>();
        Set<String> cccdSeen = new HashSet<>();

        return diemImportService.importFromExcel(
                file,
                DiemThiImportDTO.class,

                dto -> {
                    DiemThiXetTuyen diem = new DiemThiXetTuyen();
                    ThiSinh ts = findThiSinhForImport(dto.getCccd(), dto.getSoBaoDanh());

                    diem.setThiSinh(ts);
                    diem.setSobaodanh(emptyToNull(dto.getSoBaoDanh()));
                    diem.setDPhuongthuc(DiemThiXetTuyenDAO.normalizeMethod(dto.getPhuongThuc()));

                    diem.setTo(normalizeScore(dto.getTo()));
                    diem.setLi(normalizeScore(dto.getLi()));
                    diem.setHo(normalizeScore(dto.getHo()));
                    diem.setSi(normalizeScore(dto.getSi()));
                    diem.setSu(normalizeScore(dto.getSu()));
                    diem.setDi(normalizeScore(dto.getDi()));
                    diem.setVa(normalizeScore(dto.getVa()));
                    diem.setN1Thi(normalizeScore(dto.getN1Thi()));
                    diem.setN1Cc(normalizeScore(dto.getN1Cc()));
                    diem.setCncn(normalizeScore(dto.getCncn()));
                    diem.setCnnn(normalizeScore(dto.getCnnn()));
                    diem.setTi(normalizeScore(dto.getTi()));
                    diem.setKtpl(normalizeScore(dto.getKtpl()));
                    diem.setNl1(normalizeScore(dto.getNl1()));
                    diem.setNk1(normalizeScore(dto.getNk1()));
                    diem.setNk2(normalizeScore(dto.getNk2()));
                    return diem;
                },

                entities -> {
                    Session session = null;
                    Transaction tx = null;
                    try {
                        session = HibernateUtil.getSessionFactory().openSession();
                        tx = session.beginTransaction();

                        int count = 0;
                        for (DiemThiXetTuyen entity : entities) {
                            String cccd = entity.getThiSinh().getCccd();
                            ThiSinh managedThiSinh = session.createQuery(
                                            "FROM ThiSinh t WHERE t.cccd = :cccd",
                                            ThiSinh.class
                                    )
                                    .setParameter("cccd", cccd)
                                    .uniqueResult();

                            DiemThiXetTuyen existing = session.createQuery(
                                            "SELECT d FROM DiemThiXetTuyen d JOIN FETCH d.thiSinh ts WHERE ts.cccd = :cccd",
                                            DiemThiXetTuyen.class
                                    )
                                    .setParameter("cccd", cccd)
                                    .uniqueResult();

                            if (existing == null) {
                                entity.setThiSinh(managedThiSinh);
                                session.persist(entity);
                            } else {
                                existing.setSobaodanh(entity.getSobaodanh());
                                existing.setDPhuongthuc(entity.getDPhuongthuc());
                                existing.setTo(entity.getTo());
                                existing.setLi(entity.getLi());
                                existing.setHo(entity.getHo());
                                existing.setSi(entity.getSi());
                                existing.setSu(entity.getSu());
                                existing.setDi(entity.getDi());
                                existing.setVa(entity.getVa());
                                existing.setN1Thi(entity.getN1Thi());
                                existing.setN1Cc(entity.getN1Cc());
                                existing.setCncn(entity.getCncn());
                                existing.setCnnn(entity.getCnnn());
                                existing.setTi(entity.getTi());
                                existing.setKtpl(entity.getKtpl());
                                existing.setNl1(entity.getNl1());
                                existing.setNk1(entity.getNk1());
                                existing.setNk2(entity.getNk2());
                                session.merge(existing);
                            }

                            if (++count % 50 == 0) {
                                session.flush();
                                session.clear();
                            }
                        }

                        tx.commit();

                    } catch (Exception e) {
                        if (tx != null && tx.isActive()) {
                            try {
                                tx.rollback();
                            } catch (Exception rollbackEx) {
                                System.err.println("Lỗi Rollback: " + rollbackEx.getMessage());
                            }
                        }
                        e.printStackTrace();
                        throw new RuntimeException("Lỗi lưu điểm thi (Đã Rollback an toàn): " + e.getMessage());

                    } finally {
                        if (session != null && session.isOpen()) {
                            session.close();
                        }
                    }
                },

                dto -> {
                    String cccd = dto.getCccd() != null ? dto.getCccd().trim() : "";
                    if (cccd.isEmpty()) {
                        return "Bắt buộc phải có CCCD";
                    }

                    if (!cccdSeen.add(cccd)) {
                        return "CCCD bị trùng trong file Excel: " + cccd;
                    }

                    try {
                        findThiSinhForImport(dto.getCccd(), dto.getSoBaoDanh());
                    } catch (Exception e) {
                        return e.getMessage();
                    }

                    String pt = DiemThiXetTuyenDAO.normalizeMethod(dto.getPhuongThuc());
                    if (pt != null && !(DiemThiXetTuyenDAO.PT_THPT.equals(pt)
                            || DiemThiXetTuyenDAO.PT_VSAT.equals(pt)
                            || DiemThiXetTuyenDAO.PT_DGNL.equals(pt))) {
                        return "Phương thức không hợp lệ: " + pt + " (chỉ chấp nhận THPT/VSAT/DGNL)";
                    }

                    String scoreError = validateScoreRange(dto);
                    if (scoreError != null) {
                        return scoreError;
                    }

                    String methodError = validateMethodRules(dto, pt);
                    if (methodError != null) {
                        return methodError;
                    }
                    return null;
                }
        );
    }

    private ThiSinh findThiSinhByCccd(String cccd) {
        Session session = null;
        try {
            session = HibernateUtil.getSessionFactory().openSession();
            ThiSinh ts = session.createQuery(
                            "FROM ThiSinh t WHERE t.cccd = :cccd",
                            ThiSinh.class
                    )
                    .setParameter("cccd", cccd)
                    .uniqueResult();
            if (ts == null) {
                throw new IllegalArgumentException("Không tìm thấy thí sinh với CCCD: " + cccd);
            }
            return ts;
        } finally {
            if (session != null && session.isOpen()) {
                session.close();
            }
        }
    }

    private ThiSinh findThiSinhForImport(String cccdOrCode, String soBaoDanh) {
        String key = cccdOrCode != null ? cccdOrCode.trim() : "";
        String sbd = soBaoDanh != null ? soBaoDanh.trim() : "";

        if (key.isEmpty() && sbd.isEmpty()) {
            throw new IllegalArgumentException("Thiếu cả CCCD và SBD để đối chiếu thí sinh");
        }

        Session session = null;
        try {
            session = HibernateUtil.getSessionFactory().openSession();

            // Ưu tiên khớp trực tiếp theo khóa chính đầu vào
            if (!key.isEmpty()) {
                ThiSinh byKey = session.createQuery(
                                "FROM ThiSinh t WHERE t.cccd = :key OR t.sobaodanh = :key",
                                ThiSinh.class
                        )
                        .setParameter("key", key)
                        .setMaxResults(1)
                        .uniqueResult();
                if (byKey != null) {
                    return byKey;
                }
            }

            // Fallback theo SBD từ file điểm (nếu có)
            if (!sbd.isEmpty()) {
                ThiSinh bySbd = session.createQuery(
                                "FROM ThiSinh t WHERE t.sobaodanh = :sbd OR t.cccd = :sbd",
                                ThiSinh.class
                        )
                        .setParameter("sbd", sbd)
                        .setMaxResults(1)
                        .uniqueResult();
                if (bySbd != null) {
                    return bySbd;
                }
            }

            throw new IllegalArgumentException(
                    "Không tìm thấy thí sinh với khóa: " + (!key.isEmpty() ? key : sbd) +
                    " (đã thử khớp theo cả CCCD và SBD)"
            );
        } finally {
            if (session != null && session.isOpen()) {
                session.close();
            }
        }
    }

    private String emptyToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private BigDecimal normalizeScore(BigDecimal value) {
        if (value == null) {
            return null;
        }
        return value.setScale(2, RoundingMode.HALF_UP);
    }

    private String validateMethodRules(DiemThiImportDTO dto, String method) {
        if (method == null) {
            return null;
        }

        Set<String> allowed = allowedScoreKeys(method);
        String disallowed = firstDisallowedKey(dto, allowed);
        if (disallowed != null) {
            return "Phương thức " + method + " không cho phép môn " + disallowed + ".";
        }

        if (DiemThiXetTuyenDAO.PT_DGNL.equals(method)) {
            if (!hasScore(dto.getNl1())) {
                return "Phương thức DGNL cần có điểm NL1.";
            }
            return null;
        }

        if (DiemThiXetTuyenDAO.PT_VSAT.equals(method)) {
            if (!hasScore(dto.getNk1()) || !hasScore(dto.getNk2())) {
                return "Phương thức VSAT cần có đủ Năng khiếu 1 và Năng khiếu 2.";
            }
            return null;
        }

        int count = countScores(dto, allowed);
        if (count < 3) {
            return "Phương thức THPT cần có ít nhất 3 môn.";
        }
        return null;
    }

    private Set<String> allowedScoreKeys(String method) {
        if (DiemThiXetTuyenDAO.PT_VSAT.equals(method)) {
            return VSAT_KEYS;
        }
        if (DiemThiXetTuyenDAO.PT_DGNL.equals(method)) {
            return DGNL_KEYS;
        }
        return THPT_KEYS;
    }

    private int countScores(DiemThiImportDTO dto, Set<String> allowed) {
        int count = 0;
        if (allowed.contains("TO") && hasScore(dto.getTo())) count++;
        if (allowed.contains("LI") && hasScore(dto.getLi())) count++;
        if (allowed.contains("HO") && hasScore(dto.getHo())) count++;
        if (allowed.contains("SI") && hasScore(dto.getSi())) count++;
        if (allowed.contains("SU") && hasScore(dto.getSu())) count++;
        if (allowed.contains("DI") && hasScore(dto.getDi())) count++;
        if (allowed.contains("VA") && hasScore(dto.getVa())) count++;
        if (allowed.contains("N1_THI") && hasScore(dto.getN1Thi())) count++;
        if (allowed.contains("N1_CC") && hasScore(dto.getN1Cc())) count++;
        if (allowed.contains("CNCN") && hasScore(dto.getCncn())) count++;
        if (allowed.contains("CNNN") && hasScore(dto.getCnnn())) count++;
        if (allowed.contains("TI") && hasScore(dto.getTi())) count++;
        if (allowed.contains("KTPL") && hasScore(dto.getKtpl())) count++;
        if (allowed.contains("NL1") && hasScore(dto.getNl1())) count++;
        if (allowed.contains("NK1") && hasScore(dto.getNk1())) count++;
        if (allowed.contains("NK2") && hasScore(dto.getNk2())) count++;
        return count;
    }

    private String firstDisallowedKey(DiemThiImportDTO dto, Set<String> allowed) {
        if (!allowed.contains("TO") && hasScore(dto.getTo())) return "TO";
        if (!allowed.contains("LI") && hasScore(dto.getLi())) return "LI";
        if (!allowed.contains("HO") && hasScore(dto.getHo())) return "HO";
        if (!allowed.contains("SI") && hasScore(dto.getSi())) return "SI";
        if (!allowed.contains("SU") && hasScore(dto.getSu())) return "SU";
        if (!allowed.contains("DI") && hasScore(dto.getDi())) return "DI";
        if (!allowed.contains("VA") && hasScore(dto.getVa())) return "VA";
        if (!allowed.contains("N1_THI") && hasScore(dto.getN1Thi())) return "N1_THI";
        if (!allowed.contains("N1_CC") && hasScore(dto.getN1Cc())) return "N1_CC";
        if (!allowed.contains("CNCN") && hasScore(dto.getCncn())) return "CNCN";
        if (!allowed.contains("CNNN") && hasScore(dto.getCnnn())) return "CNNN";
        if (!allowed.contains("TI") && hasScore(dto.getTi())) return "TI";
        if (!allowed.contains("KTPL") && hasScore(dto.getKtpl())) return "KTPL";
        if (!allowed.contains("NL1") && hasScore(dto.getNl1())) return "NL1";
        if (!allowed.contains("NK1") && hasScore(dto.getNk1())) return "NK1";
        if (!allowed.contains("NK2") && hasScore(dto.getNk2())) return "NK2";
        return null;
    }

    private boolean hasScore(BigDecimal value) {
        return value != null;
    }

    private String validateScoreRange(DiemThiImportDTO dto) {
        String err;
        err = checkScore("TO", dto.getTo()); if (err != null) return err;
        err = checkScore("LI", dto.getLi()); if (err != null) return err;
        err = checkScore("HO", dto.getHo()); if (err != null) return err;
        err = checkScore("SI", dto.getSi()); if (err != null) return err;
        err = checkScore("SU", dto.getSu()); if (err != null) return err;
        err = checkScore("DI", dto.getDi()); if (err != null) return err;
        err = checkScore("VA", dto.getVa()); if (err != null) return err;
        err = checkScore("N1_THI", dto.getN1Thi()); if (err != null) return err;
        err = checkScore("N1_CC", dto.getN1Cc()); if (err != null) return err;
        err = checkScore("CNCN", dto.getCncn()); if (err != null) return err;
        err = checkScore("CNNN", dto.getCnnn()); if (err != null) return err;
        err = checkScore("TI", dto.getTi()); if (err != null) return err;
        err = checkScore("KTPL", dto.getKtpl()); if (err != null) return err;
        err = checkScore("NL1", dto.getNl1()); if (err != null) return err;
        err = checkScore("NK1", dto.getNk1()); if (err != null) return err;
        err = checkScore("NK2", dto.getNk2()); if (err != null) return err;
        return null;
    }

    private String checkScore(String mon, BigDecimal value) {
        if (value == null) {
            return null;
        }
        BigDecimal max = "NL1".equals(mon) ? new BigDecimal("1200") : new BigDecimal("10");
        if (value.compareTo(BigDecimal.ZERO) < 0 || value.compareTo(max) > 0) {
            return "Điểm " + mon + " phải nằm trong khoảng 0 - " + max.stripTrailingZeros().toPlainString();
        }
        return null;
    }
}