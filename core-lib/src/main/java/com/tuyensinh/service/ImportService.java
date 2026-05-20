package com.tuyensinh.service;

import com.tuyensinh.dto.DiemThiImportDTO;
import com.tuyensinh.dto.ThiSinhImportDTO;
import com.tuyensinh.dao.DiemThiXetTuyenDAO;
import com.tuyensinh.model.DiemThiXetTuyen;
import com.tuyensinh.model.ThiSinh;
import com.tuyensinh.model.ThiSinhAccount;
import com.tuyensinh.util.HibernateUtil;
import com.tuyensinh.util.PasswordUtil;
import com.tuyensinh.util.SystemLogger;
import org.hibernate.Session;
import org.hibernate.Transaction;

import java.io.File;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Map;
import java.util.HashMap;

public class ImportService {

    private final BaseImportService<ThiSinhImportDTO, ThiSinh> baseImportService;
    private final Map<String, ThiSinh> thiSinhCache = new HashMap<>();

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

    /**
     * Import danh sach thi sinh tu file Excel.
     * Tu dong tao tai khoan voi mat khau mac dinh la ngay sinh.
     */
    public List<String> importThiSinh(File file) {
        System.out.println("[ImportService] Bat dau import thi sinh tu file: " + file.getName());
        
        List<String> errors = baseImportService.importFromExcel(
                file,
                ThiSinhImportDTO.class,
                
                dto -> {
                    ThiSinh ts = new ThiSinh();
                    ts.setCccd(dto.getCccd());
                    ts.setSobaodanh(dto.getSoBaoDanh());
                    
                    String fullName = trimToNull(dto.getHoTen());
                    if (fullName != null) {
                        int lastSpace = fullName.lastIndexOf(' ');
                        if (lastSpace > 0) {
                            ts.setHo(fullName.substring(0, lastSpace));
                            ts.setTen(fullName.substring(lastSpace + 1));
                        } else {
                            ts.setHo("");
                            ts.setTen(fullName);
                        }
                    } else {
                        ts.setHo(trimToNull(dto.getHo()));
                        ts.setTen(trimToNull(dto.getTen()));
                    }
                    
                    String ngaySinh = pickFirstNonBlank(dto.getNgaySinh(), dto.getNgaySinhAlt());
                    ts.setNgaySinh(ngaySinh);
                    ts.setDienThoai(dto.getDienThoai());
                    ts.setGioiTinh(pickFirstNonBlank(dto.getGioiTinh(), dto.getGioiTinhAlt()));
                    ts.setEmail(dto.getEmail());
                    ts.setNoiSinh(dto.getNoiSinh());
                    ts.setDoiTuong(dto.getDoiTuongUuTien());
                    ts.setKhuVuc(dto.getKhuVucUuTien());

                    ThiSinhAccount account = new ThiSinhAccount();
                    String rawPassword = (ngaySinh != null && !ngaySinh.isEmpty()) ? ngaySinh : "123456"; 
                    account.setPasswordHash(PasswordUtil.hash(rawPassword)); 
                    account.setThiSinh(ts);
                    ts.setAccount(account);
                    return ts;
                },
                
                entities -> {
                    Session session = null;
                    Transaction tx = null;
                    try {
                        session = HibernateUtil.getSessionFactory().openSession();
                        tx = session.beginTransaction();
                        
                        int count = 0;
                        for (ThiSinh entity : entities) {
                            ThiSinh existing = session.createQuery(
                                    "FROM ThiSinh t WHERE t.cccd = :cccd", ThiSinh.class)
                                    .setParameter("cccd", entity.getCccd())
                                    .uniqueResult();
                            
                            if (existing == null) {
                                session.persist(entity);
                            } else {
                                existing.setSobaodanh(entity.getSobaodanh());
                                existing.setHo(entity.getHo());
                                existing.setTen(entity.getTen());
                                existing.setNgaySinh(entity.getNgaySinh());
                                existing.setDienThoai(entity.getDienThoai());
                                existing.setGioiTinh(entity.getGioiTinh());
                                existing.setEmail(entity.getEmail());
                                existing.setNoiSinh(entity.getNoiSinh());
                                existing.setDoiTuong(entity.getDoiTuong());
                                existing.setKhuVuc(entity.getKhuVuc());
                                session.merge(existing);
                            }
                            
                            if (++count % 50 == 0) {
                                session.flush();
                                session.clear();
                            }
                        }
                        
                        tx.commit(); 
                        System.out.println("[ImportService] Import thanh cong " + entities.size() + " thi sinh");
                        SystemLogger.log(null, "System", "Import " + entities.size() + " thí sinh từ file Excel", true);
                        
                    } catch (Exception e) {
                        if (tx != null && tx.isActive()) {
                            try {
                                tx.rollback();
                            } catch (Exception rollbackEx) {
                                System.err.println("[ImportService] Loi rollback: " + rollbackEx.getMessage());
                            }
                        }
                        System.err.println("[ImportService] Loi import thi sinh: " + e.getMessage());
                        e.printStackTrace(); 
                        SystemLogger.log(null, "System", "Lỗi import thí sinh: " + e.getMessage(), false);
                        throw new RuntimeException("Loi luu Database (Da Rollback an toan): " + e.getMessage());
                        
                    } finally {
                        if (session != null && session.isOpen()) {
                            session.close();
                        }
                    }
                },
                
                dto -> {
                    if (dto.getCccd() == null || dto.getCccd().trim().isEmpty()) return "Bat buoc phai co so CCCD";
                    
                    boolean hasTen = (dto.getTen() != null && !dto.getTen().trim().isEmpty());
                    boolean hasHoTen = (dto.getHoTen() != null && !dto.getHoTen().trim().isEmpty());
                    
                    if (!hasTen && !hasHoTen) return "Bat buoc phai co Ten hoac Ho Ten";
                    return null; 
                }
        );
        
        if (errors.isEmpty()) {
            System.out.println("[ImportService] Import thi sinh hoan thanh, khong co loi");
        } else {
            System.out.println("[ImportService] Import thi sinh co " + errors.size() + " loi");
        }
        return errors;
    }

    private static String pickFirstNonBlank(String primary, String secondary) {
        String first = trimToNull(primary);
        if (first != null) {
            return first;
        }
        return trimToNull(secondary);
    }

    private static String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    /**
     * Import diem thi tu file Excel.
     * Neu thi sinh da co diem thi cap nhat, chua co thi tao moi.
     */
    public List<String> importDiemThi(File file, String defaultMethod) {
        System.out.println("[ImportService] Bat dau import diem thi tu file: " + file.getName());
        BaseImportService<DiemThiImportDTO, DiemThiXetTuyen> diemImportService = new BaseImportService<>();
        Set<String> cccdSeen = new HashSet<>();

        List<String> errors = diemImportService.importFromExcel(
                file,
                DiemThiImportDTO.class,

                dto -> {
                    DiemThiXetTuyen diem = new DiemThiXetTuyen();
                    ThiSinh ts = findThiSinhForImportCached(dto.getCccd(), dto.getSoBaoDanh());
                    diem.setThiSinh(ts);
                    diem.setSobaodanh(emptyToNull(dto.getSoBaoDanh()));
                    
                    String pt = dto.getPhuongThuc() != null && !dto.getPhuongThuc().trim().isEmpty() 
                                ? DiemThiXetTuyenDAO.normalizeMethod(dto.getPhuongThuc()) 
                                : defaultMethod;
                    diem.setDPhuongthuc(pt);
                    
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

                        // Tải sẵn toàn bộ ThiSinh và DiemThiXetTuyen vào bộ nhớ để Lookup O(1)
                        System.out.println("[ImportService] Dang tai du lieu tu DB de toi uu hoa...");
                        List<ThiSinh> allTs = session.createQuery("FROM ThiSinh", ThiSinh.class).list();
                        Map<String, ThiSinh> dbThiSinhMap = new HashMap<>();
                        for (ThiSinh t : allTs) dbThiSinhMap.put(t.getCccd(), t);

                        List<DiemThiXetTuyen> allDt = session.createQuery("SELECT d FROM DiemThiXetTuyen d JOIN FETCH d.thiSinh ts", DiemThiXetTuyen.class).list();
                        Map<String, DiemThiXetTuyen> dbDiemThiMap = new HashMap<>();
                        for (DiemThiXetTuyen d : allDt) dbDiemThiMap.put(d.getThiSinh().getCccd(), d);
                        System.out.println("[ImportService] Tai xong du lieu DB, tien hanh luu batch...");

                        int count = 0;
                        for (DiemThiXetTuyen entity : entities) {
                            String cccd = entity.getThiSinh().getCccd();
                            ThiSinh managedThiSinh = dbThiSinhMap.get(cccd);
                            DiemThiXetTuyen existing = dbDiemThiMap.get(cccd);

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
                        System.out.println("[ImportService] Import thanh cong diem thi cho " + entities.size() + " thi sinh");
                        SystemLogger.log(null, "System", "Import điểm thi cho " + entities.size() + " thí sinh từ file Excel", true);

                    } catch (Exception e) {
                        if (tx != null && tx.isActive()) {
                            try {
                                tx.rollback();
                            } catch (Exception rollbackEx) {
                                System.err.println("[ImportService] Loi rollback: " + rollbackEx.getMessage());
                            }
                        }
                        System.err.println("[ImportService] Loi import diem thi: " + e.getMessage());
                        e.printStackTrace();
                        SystemLogger.log(null, "System", "Lỗi import điểm thi: " + e.getMessage(), false);
                        throw new RuntimeException("Loi luu diem thi (Da Rollback an toan): " + e.getMessage());

                    } finally {
                        if (session != null && session.isOpen()) {
                            session.close();
                        }
                    }
                },

                dto -> {
                    String cccd = dto.getCccd() != null ? dto.getCccd().trim() : "";
                    if (cccd.isEmpty()) {
                        return "Bat buoc phai co CCCD";
                    }
                    if (!cccdSeen.add(cccd)) {
                        return "CCCD bi trung trong file Excel: " + cccd;
                    }
                    try {
                        findThiSinhForImportCached(dto.getCccd(), dto.getSoBaoDanh());
                    } catch (Exception e) {
                        return e.getMessage();
                    }
                    
                    String pt = dto.getPhuongThuc() != null && !dto.getPhuongThuc().trim().isEmpty() 
                                ? DiemThiXetTuyenDAO.normalizeMethod(dto.getPhuongThuc()) 
                                : defaultMethod;
                                
                    if (pt != null && !(DiemThiXetTuyenDAO.PT_THPT.equals(pt)
                            || DiemThiXetTuyenDAO.PT_VSAT.equals(pt)
                            || DiemThiXetTuyenDAO.PT_DGNL.equals(pt))) {
                        return "Phuong thuc khong hop le: " + pt + " (chi chap nhan THPT/VSAT/DGNL)";
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
        
        if (errors.isEmpty()) {
            System.out.println("[ImportService] Import diem thi hoan thanh, khong co loi");
        } else {
            System.out.println("[ImportService] Import diem thi co " + errors.size() + " loi");
        }
        return errors;
    }

    /**
     * Tim thi sinh theo CCCD
     */
    private ThiSinh findThiSinhByCccd(String cccd) {
        Session session = null;
        try {
            session = HibernateUtil.getSessionFactory().openSession();
            ThiSinh ts = session.createQuery("FROM ThiSinh t WHERE t.cccd = :cccd", ThiSinh.class)
                    .setParameter("cccd", cccd)
                    .uniqueResult();
            if (ts == null) {
                throw new IllegalArgumentException("Khong tim thay thi sinh voi CCCD: " + cccd);
            }
            return ts;
        } finally {
            if (session != null && session.isOpen()) {
                session.close();
            }
        }
    }

    /**
     * Tim thi sinh theo CCCD hoac So bao danh (uu tien CCCD)
     */
    private ThiSinh findThiSinhForImport(String cccdOrCode, String soBaoDanh) {
        String key = cccdOrCode != null ? cccdOrCode.trim() : "";
        String sbd = soBaoDanh != null ? soBaoDanh.trim() : "";

        if (key.isEmpty() && sbd.isEmpty()) {
            throw new IllegalArgumentException("Thieu ca CCCD va SBD de doi chieu thi sinh");
        }

        Session session = null;
        try {
            session = HibernateUtil.getSessionFactory().openSession();

            if (!key.isEmpty()) {
                ThiSinh byKey = session.createQuery(
                                "FROM ThiSinh t WHERE t.cccd = :key OR t.sobaodanh = :key", ThiSinh.class)
                        .setParameter("key", key)
                        .setMaxResults(1)
                        .uniqueResult();
                if (byKey != null) return byKey;
            }

            if (!sbd.isEmpty()) {
                ThiSinh bySbd = session.createQuery(
                                "FROM ThiSinh t WHERE t.sobaodanh = :sbd OR t.cccd = :sbd", ThiSinh.class)
                        .setParameter("sbd", sbd)
                        .setMaxResults(1)
                        .uniqueResult();
                if (bySbd != null) return bySbd;
            }

            throw new IllegalArgumentException(
                    "Khong tim thay thi sinh voi khoa: " + (!key.isEmpty() ? key : sbd) +
                    " (da thu khop theo ca CCCD va SBD)");
        } finally {
            if (session != null && session.isOpen()) {
                session.close();
            }
        }
    }

    private ThiSinh findThiSinhForImportCached(String cccdOrCode, String soBaoDanh) {
        String key = cccdOrCode != null ? cccdOrCode.trim() : "";
        if (thiSinhCache.containsKey(key)) {
            return thiSinhCache.get(key);
        }
        ThiSinh ts = findThiSinhForImport(cccdOrCode, soBaoDanh);
        thiSinhCache.put(key, ts);
        return ts;
    }

    private String emptyToNull(String value) {
        if (value == null) return null;
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private BigDecimal normalizeScore(BigDecimal value) {
        if (value == null) return null;
        return value.setScale(2, RoundingMode.HALF_UP);
    }

    private String validateMethodRules(DiemThiImportDTO dto, String method) {
        if (method == null) {
            return null;
        }


        Set<String> allowed = allowedScoreKeys(method);


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

        // int count = countScores(dto, allowed);
        // if (count < 3) {
        //     return "Phương thức THPT cần có ít nhất 3 môn.";
        // }
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

    /**
     * Kiem tra khoang hop le cua diem thi
     */
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
        if (value == null) return null;
        BigDecimal max = "NL1".equals(mon) ? new BigDecimal("1200") : new BigDecimal("10");
        if (value.compareTo(BigDecimal.ZERO) < 0 || value.compareTo(max) > 0) {
            return "Diem " + mon + " phai nam trong khoang 0 - " + max.stripTrailingZeros().toPlainString();
        }
        return null;
    }
    public List<String> importToHopMon(File file) {
        BaseImportService<com.tuyensinh.dto.ToHopMonImportDTO, com.tuyensinh.model.ToHopMon> baseService = new BaseImportService<>();
        return baseService.importFromExcel(
                file,
                com.tuyensinh.dto.ToHopMonImportDTO.class,
                dto -> {
                    com.tuyensinh.model.ToHopMon entity = new com.tuyensinh.model.ToHopMon();
                    entity.setMatohop(dto.getMatohop());
                    entity.setTentohop(dto.getTentohop());
                    entity.setMon1(dto.getMon1());
                    entity.setMon2(dto.getMon2());
                    entity.setMon3(dto.getMon3());
                    return entity;
                },
                entities -> {
                    Session session = null;
                    Transaction tx = null;
                    try {
                        session = HibernateUtil.getSessionFactory().openSession();
                        tx = session.beginTransaction();
                        int count = 0;
                        for (com.tuyensinh.model.ToHopMon entity : entities) {
                            // Check existence
                            com.tuyensinh.model.ToHopMon existing = session.createQuery(
                                "FROM ToHopMon t WHERE t.matohop = :ma", com.tuyensinh.model.ToHopMon.class)
                                .setParameter("ma", entity.getMatohop())
                                .uniqueResult();
                            
                            if (existing == null) {
                                session.persist(entity);
                            } else {
                                existing.setTentohop(entity.getTentohop());
                                existing.setMon1(entity.getMon1());
                                existing.setMon2(entity.getMon2());
                                existing.setMon3(entity.getMon3());
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
                            try { tx.rollback(); } catch (Exception ignored) {}
                        }
                        throw new RuntimeException("Lỗi lưu tổ hợp môn: " + e.getMessage());
                    } finally {
                        if (session != null && session.isOpen()) session.close();
                    }
                },
                dto -> {
                    if (dto.getMatohop() == null || dto.getMatohop().trim().isEmpty()) return "Mã tổ hợp không được trống";
                    if (dto.getMon1() == null || dto.getMon1().trim().isEmpty()) return "Môn 1 không được trống";
                    if (dto.getMon2() == null || dto.getMon2().trim().isEmpty()) return "Môn 2 không được trống";
                    if (dto.getMon3() == null || dto.getMon3().trim().isEmpty()) return "Môn 3 không được trống";
                    return null;
                }
        );
    }
}