package com.tuyensinh.service;

import com.tuyensinh.dao.ThiSinhAccountDAO;
import com.tuyensinh.dto.ThiSinhSessionDTO;
import com.tuyensinh.model.ThiSinh;
import com.tuyensinh.model.ThiSinhAccount;
import com.tuyensinh.util.PasswordUtil;
import java.util.Optional;

public class AuthService {

    private static AuthService instance;
    private final ThiSinhAccountDAO accountDAO = new ThiSinhAccountDAO();

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
            // 1. Tìm Account (Hàm findByCccd trong DAO đã FETCH sẵn ThiSinh rồi)
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

            // 4. Lấy thông tin Thí sinh TRỰC TIẾP từ Object Account (Nhờ @OneToOne)
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
}