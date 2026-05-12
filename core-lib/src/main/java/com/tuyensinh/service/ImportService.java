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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ImportService {

    private final BaseImportService<ThiSinhImportDTO, ThiSinh> baseImportService;

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
                    ts.setHo(dto.getHo());
                    ts.setTen(dto.getTen());
                    String ngaySinh = dto.getNgaySinh(); 
                    ts.setNgaySinh(ngaySinh);
                    ts.setDienThoai(dto.getDienThoai());
                    ts.setGioiTinh(dto.getGioiTinh());
                    ts.setEmail(dto.getEmail());

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
                            session.persist(entity); 
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
                    if (dto.getTen() == null || dto.getTen().trim().isEmpty()) return "Bat buoc phai co Ten";
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

    /**
     * Import diem thi tu file Excel.
     * Neu thi sinh da co diem thi cap nhat, chua co thi tao moi.
     */
    public List<String> importDiemThi(File file) {
        System.out.println("[ImportService] Bat dau import diem thi tu file: " + file.getName());
        BaseImportService<DiemThiImportDTO, DiemThiXetTuyen> diemImportService = new BaseImportService<>();
        Set<String> cccdSeen = new HashSet<>();

        List<String> errors = diemImportService.importFromExcel(
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
                                            "FROM ThiSinh t WHERE t.cccd = :cccd", ThiSinh.class)
                                    .setParameter("cccd", cccd)
                                    .uniqueResult();

                            DiemThiXetTuyen existing = session.createQuery(
                                            "SELECT d FROM DiemThiXetTuyen d JOIN FETCH d.thiSinh ts WHERE ts.cccd = :cccd",
                                            DiemThiXetTuyen.class)
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
                        findThiSinhForImport(dto.getCccd(), dto.getSoBaoDanh());
                    } catch (Exception e) {
                        return e.getMessage();
                    }
                    String pt = DiemThiXetTuyenDAO.normalizeMethod(dto.getPhuongThuc());
                    if (pt != null && !(DiemThiXetTuyenDAO.PT_THPT.equals(pt)
                            || DiemThiXetTuyenDAO.PT_VSAT.equals(pt)
                            || DiemThiXetTuyenDAO.PT_DGNL.equals(pt))) {
                        return "Phuong thuc khong hop le: " + pt + " (chi chap nhan THPT/VSAT/DGNL)";
                    }
                    String scoreError = validateScoreRange(dto);
                    if (scoreError != null) {
                        return scoreError;
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

    private String emptyToNull(String value) {
        if (value == null) return null;
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private BigDecimal normalizeScore(BigDecimal value) {
        if (value == null) return null;
        return value.setScale(2, RoundingMode.HALF_UP);
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
}