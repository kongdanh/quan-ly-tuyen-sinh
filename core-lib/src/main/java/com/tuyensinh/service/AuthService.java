package com.tuyensinh.service;

import com.tuyensinh.dao.ThiSinhAccountDAO;
import com.tuyensinh.dto.ThiSinhSessionDTO;
import com.tuyensinh.model.ThiSinh;
import com.tuyensinh.model.ThiSinhAccount;
import com.tuyensinh.model.User;
import com.tuyensinh.dao.UserDAO;

import com.tuyensinh.util.PasswordUtil;
import java.util.Optional;

public class AuthService {

    private static AuthService instance;
    private final ThiSinhAccountDAO accountDAO = new ThiSinhAccountDAO();
    private final UserDAO userDAO = new UserDAO();

    private AuthService() {}

    public static synchronized AuthService getInstance() {
        if (instance == null) instance = new AuthService();
        return instance;
    }

    public ThiSinhSessionDTO loginThiSinh(String cccd, String password) {
        if (cccd == null || password == null || cccd.isBlank()) {
            return null;
        }

        try {
            // 1. Tìm Account (Hàm findByCccd trong DAO)
            Optional<ThiSinhAccount> accountOpt = accountDAO.findByCccd(cccd.trim());
            if (accountOpt.isEmpty()) {
                System.out.println("[AuthService] Không tìm thấy Account có CCCD: " + cccd);
                return null;
            }

            ThiSinhAccount account = accountOpt.get();

            // 2. Kiểm tra trạng thái
            if (!"HOAT_DONG".equals(account.getTrangThai())) {
                System.out.println("[AuthService] Tài khoản bị khóa!");
                return null;
            }
            
            // 3. Kiểm tra Mật khẩu
            if (!PasswordUtil.verify(password, account.getPasswordHash())) {
                System.out.println("[AuthService] Sai mật khẩu cho CCCD: " + cccd);
                return null;
            }

            // 4. Lấy thông tin Thí sinh
            ThiSinh ts = account.getThiSinh();
            
            if (ts == null) {
                System.out.println("[AuthService] Lỗi: Tài khoản mồ côi, không có thông tin thí sinh!");
                return null;
            }

            // 5. Cập nhật thời gian đăng nhập cuối
            accountDAO.updateLastLogin(account.getId());
            System.out.println("[AuthService] Đăng nhập thành công: " + ts.getHo() + " " + ts.getTen());

            // 6. Trả về Session DTO
            return ThiSinhSessionDTO.builder()
                    .idThiSinh(ts.getId())
                    .cccd(ts.getCccd())
                    .hoTen((ts.getHo() != null ? ts.getHo().trim() : "") + " " + (ts.getTen() != null ? ts.getTen().trim() : ""))
                    .email(ts.getEmail())
                    .gioiTinh(ts.getGioiTinh())
                    .ngaySinh(ts.getNgaySinh())
                    .build();

        } catch (Exception e) {
            System.err.println("[AuthService] Lỗi hệ thống khi đăng nhập: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    public User loginAdmin(String username, String password) {
        if (username == null || password == null || username.isBlank()) {
            return null;
        }

        try {
            // 1. Tìm User theo username
            java.util.Optional<com.tuyensinh.model.User> userOpt = userDAO.findByUsername(username.trim());
            if (userOpt.isEmpty()) {
                System.out.println("[AuthService] Không tìm thấy Admin/GV: " + username);
                return null;
            }

            User adminUser = userOpt.get();

            // 2. Kiểm tra trạng thái
            if (!"HOAT_DONG".equals(adminUser.getTrangThai())) {
                System.out.println("[AuthService] Tài khoản Admin/GV bị khóa!");
                return null;
            }

            // 3. Kiểm tra mật khẩu (Dùng BCrypt)
            if (!com.tuyensinh.util.PasswordUtil.verify(password, adminUser.getPasswordHash())) {
                System.out.println("[AuthService] Sai mật khẩu Admin!");
                return null;
            }

            System.out.println("[AuthService] Admin/GV đăng nhập thành công: " + adminUser.getHoTen());
            return adminUser;

        } catch (Exception e) {
            System.err.println("[AuthService] Lỗi hệ thống khi đăng nhập Admin: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
}