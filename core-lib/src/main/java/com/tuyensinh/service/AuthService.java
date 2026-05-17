package com.tuyensinh.service;

import com.tuyensinh.dao.ThiSinhAccountDAO;
import com.tuyensinh.dto.ThiSinhSessionDTO;
import com.tuyensinh.model.ThiSinh;
import com.tuyensinh.model.ThiSinhAccount;
import com.tuyensinh.model.User;
import com.tuyensinh.dao.UserDAO;
import com.tuyensinh.util.SystemLogger;
import com.tuyensinh.util.PasswordUtil;

import java.util.Optional;

public class AuthService {

    private static AuthService instance;
    private final ThiSinhAccountDAO accountDAO = new ThiSinhAccountDAO();
    private final UserDAO userDAO = new UserDAO();
    private final com.tuyensinh.dao.ThiSinhDAO thiSinhDAO = new com.tuyensinh.dao.ThiSinhDAO();
    private final com.tuyensinh.dao.HoSoTuyenSinhDAO hoSoDAO = new com.tuyensinh.dao.HoSoTuyenSinhDAO();
    private final com.tuyensinh.service.DotTuyenSinhService dotService = new com.tuyensinh.service.DotTuyenSinhService();
    
    public AuthService() {}

    public static synchronized AuthService getInstance() {
        if (instance == null) instance = new AuthService();
        return instance;
    }

    /**
     * Dang nhap thi sinh bang CCCD va mat khau.
     * Kiem tra: tai khoan ton tai -> trang thai hoat dong -> mat khau dung -> tra ve SessionDTO
     */
    public ThiSinhSessionDTO loginThiSinh(String cccd, String password) {
        if (cccd == null || password == null || cccd.isBlank()) {
            return null;
        }

        try {
            Optional<ThiSinhAccount> accountOpt = accountDAO.findByCccd(cccd.trim());
            if (accountOpt.isEmpty()) {
                System.out.println("[AuthService] Khong tim thay Account co CCCD: " + cccd);
                SystemLogger.log(null, null, "Đăng nhập thất bại - Không tìm thấy tài khoản CCCD: " + cccd, false);
                return null;
            }

            ThiSinhAccount account = accountOpt.get();

            if (!"HOAT_DONG".equals(account.getTrangThai())) {
                System.out.println("[AuthService] Tai khoan bi khoa, CCCD: " + cccd);
                SystemLogger.log(null, null, "Đăng nhập thất bại - Tài khoản bị khóa, CCCD: " + cccd, false);
                return null;
            }
            
            if (!PasswordUtil.verify(password, account.getPasswordHash())) {
                System.out.println("[AuthService] Sai mat khau cho CCCD: " + cccd);
                SystemLogger.log(null, null, "Đăng nhập thất bại - Sai mật khẩu, CCCD: " + cccd, false);
                return null;
            }

            ThiSinh ts = account.getThiSinh();
            
            if (ts == null) {
                System.out.println("[AuthService] Tai khoan mo coi, khong co thong tin thi sinh, CCCD: " + cccd);
                return null;
            }

            accountDAO.updateLastLogin(account.getId());
            System.out.println("[AuthService] Dang nhap thanh cong: " + ts.getHo() + " " + ts.getTen());
            SystemLogger.log(null, ts.getHo() + " " + ts.getTen(), "Đăng nhập hệ thống (Thí sinh)", true);

            return ThiSinhSessionDTO.builder()
                    .idThiSinh(ts.getId())
                    .cccd(ts.getCccd())
                    .hoTen((ts.getHo() != null ? ts.getHo().trim() : "") + " " + (ts.getTen() != null ? ts.getTen().trim() : ""))
                    .email(ts.getEmail())
                    .gioiTinh(ts.getGioiTinh())
                    .ngaySinh(ts.getNgaySinh())
                    .build();

        } catch (Exception e) {
            System.err.println("[AuthService] Loi he thong khi dang nhap: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Dang nhap Admin/Giang vien bang username va mat khau.
     * Kiem tra: user ton tai -> trang thai hoat dong -> mat khau dung -> ghi log -> tra ve User
     */
    public User loginAdmin(String username, String password) {
        if (username == null || password == null || username.isBlank()) {
            return null;
        }

        try {
            java.util.Optional<com.tuyensinh.model.User> userOpt = userDAO.findByUsername(username.trim());
            if (userOpt.isEmpty()) {
                System.out.println("[AuthService] Khong tim thay Admin/GV: " + username);
                SystemLogger.log(null, username, "Đăng nhập thất bại - Không tìm thấy tài khoản", false);
                return null;
            }

            User adminUser = userOpt.get();

            if (!"HOAT_DONG".equals(adminUser.getTrangThai())) {
                System.out.println("[AuthService] Tai khoan Admin/GV bi khoa: " + username);
                SystemLogger.log(adminUser.getId(), adminUser.getUsername(), "Đăng nhập thất bại - Tài khoản bị khóa", false);
                return null;
            }

            if (!PasswordUtil.verify(password, adminUser.getPasswordHash())) {
                System.out.println("[AuthService] Sai mat khau Admin: " + username);
                SystemLogger.log(adminUser.getId(), adminUser.getUsername(), "Đăng nhập thất bại - Sai mật khẩu", false);
                return null;
            }

            System.out.println("[AuthService] Admin/GV dang nhap thanh cong: " + adminUser.getHoTen());
            SystemLogger.log(adminUser.getId(), adminUser.getUsername(), "Đăng nhập hệ thống (Admin)", true);
            return adminUser;

        } catch (Exception e) {
            System.err.println("[AuthService] Loi he thong khi dang nhap Admin: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Kiem tra CCCD da ton tai trong he thong chua
     */
    public boolean checkExists(String cccd, String email) {
        return accountDAO.findByCccd(cccd).isPresent(); 
    }

    /**
     * Dang ky thi sinh moi: luu ThiSinh + ThiSinhAccount + HoSoTuyenSinh (neu co dot dang mo)
     */
    public boolean registerNewCandidate(ThiSinh ts, String plainPassword) {
        org.hibernate.Transaction tx = null;
        try (org.hibernate.Session session = com.tuyensinh.util.HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();
            
            session.persist(ts);
            
            com.tuyensinh.model.ThiSinhAccount acc = new com.tuyensinh.model.ThiSinhAccount();
            acc.setThiSinh(ts);
            acc.setPasswordHash(PasswordUtil.hash(plainPassword));
            acc.setTrangThai("HOAT_DONG");
            session.persist(acc);
            
            com.tuyensinh.model.DotTuyenSinh dotHienTai = dotService.getDotDangMo().orElse(null);
            if (dotHienTai != null) {
                com.tuyensinh.model.HoSoTuyenSinh hoSo = new com.tuyensinh.model.HoSoTuyenSinh();
                hoSo.setThiSinh(ts);
                hoSo.setDotTuyenSinh(dotHienTai);
                hoSo.setMaHoSo("HS26-" + String.format("%05d", ts.getId()));
                hoSo.setTrangThai("CHO_XET");
                session.persist(hoSo);
            }
            
            tx.commit();
            System.out.println("[AuthService] Dang ky thi sinh thanh cong: " + ts.getHo() + " " + ts.getTen());
            SystemLogger.log(null, ts.getHo() + " " + ts.getTen(), "Đăng ký tài khoản thí sinh mới", true);
            return true;
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            System.err.println("[AuthService] Loi tao tai khoan: " + e.getMessage());
            SystemLogger.log(null, null, "Lỗi đăng ký tài khoản: " + e.getMessage(), false);
            return false;
        }
    }

}