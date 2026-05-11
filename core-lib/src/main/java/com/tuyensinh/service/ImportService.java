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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ImportService {

    private final BaseImportService<ThiSinhImportDTO, ThiSinh> baseImportService;

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