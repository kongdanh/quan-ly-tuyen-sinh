package com.tuyensinh.util;

import com.tuyensinh.dao.NhatKyHoatDongDAO;
import com.tuyensinh.model.NhatKyHoatDong;

import java.util.concurrent.CompletableFuture;

public class SystemLogger {
    private static final NhatKyHoatDongDAO dao = new NhatKyHoatDongDAO();
    private SystemLogger() {}

    /**
     * Hàm ghi log đa năng cho toàn bộ hệ thống (Admin, Web, Auth...)
     */
    public static void log(Integer userId, String username, String action, boolean isSuccess) {
        CompletableFuture.runAsync(() -> {
            NhatKyHoatDong log = new NhatKyHoatDong();
            log.setUserId(userId);
            // Nếu không có tên thì mặc định là Hệ thống
            log.setUsername(username != null && !username.isBlank() ? username : "System");
            String safeAction = action != null && action.length() > 250 ? action.substring(0, 250) + "..." : action;
            log.setHanhDong(safeAction);
            log.setTrangThai(isSuccess ? "SUCCESS" : "FAILED");
            
            try {
                dao.save(log);
            } catch (Exception e) {
                System.err.println("[SystemLogger] Lỗi không thể ghi log: " + e.getMessage());
            }
        });
    }
}