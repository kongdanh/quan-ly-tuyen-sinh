package com.tuyensinh.service;

import com.tuyensinh.dto.ThiSinhImportDTO;
import com.tuyensinh.model.ThiSinh;
import com.tuyensinh.model.ThiSinhAccount;
import com.tuyensinh.util.HibernateUtil;
import com.tuyensinh.util.PasswordUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;

import java.io.File;
import java.util.List;

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
}