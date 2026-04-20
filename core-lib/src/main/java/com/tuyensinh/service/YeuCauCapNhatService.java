package com.tuyensinh.service;

import com.tuyensinh.dao.ThiSinhDAO;
import com.tuyensinh.dao.YeuCauCapNhatDAO;
import com.tuyensinh.model.ThiSinh;
import com.tuyensinh.model.YeuCauCapNhat;
import com.tuyensinh.util.SystemLogger;

import java.util.List;
import java.util.Optional;

public class YeuCauCapNhatService {

    private static YeuCauCapNhatService instance;
    private final YeuCauCapNhatDAO yeuCauDAO = new YeuCauCapNhatDAO();
    private final ThiSinhDAO thiSinhDAO = new ThiSinhDAO();

    public YeuCauCapNhatService() {}

    public static synchronized YeuCauCapNhatService getInstance() {
        if (instance == null) instance = new YeuCauCapNhatService();
        return instance;
    }

    public java.util.concurrent.CompletableFuture<java.util.List<YeuCauCapNhat>> layDanhSachChoDuyet() {
        return yeuCauDAO.findPendingRequests();
    }

    // =====================================================================
    // THISINH: tạo yc mới
    // =====================================================================
    public boolean taoYeuCau(YeuCauCapNhat yeuCau, Integer idThiSinh, String tenThiSinh) {
        try {
            yeuCau.setTrangThai("PENDING");
            yeuCauDAO.save(yeuCau);

            // Ghi log Thí sinh
            SystemLogger.log(
                idThiSinh, 
                tenThiSinh, 
                "Gửi yêu cầu cập nhật thông tin (Chờ duyệt)", 
                true
            );
            return true;
        } catch (Exception e) {
            System.err.println("[YeuCauService] Lỗi tạo yêu cầu: " + e.getMessage());
            SystemLogger.log(idThiSinh, tenThiSinh, "Gửi yêu cầu cập nhật thông tin", false);
            return false;
        }
    }

    // =====================================================================
    // ADMIN: duyệt yêu cầu
    // =====================================================================
    public boolean duyetYeuCau(int idYeuCau, Integer adminId, String adminUsername) {
        try {
            Optional<YeuCauCapNhat> optYeuCau = yeuCauDAO.findById(idYeuCau);
            if (optYeuCau.isEmpty()) return false;

            YeuCauCapNhat yeuCau = optYeuCau.get();
            if (!"PENDING".equals(yeuCau.getTrangThai())) return false; // Chỉ duyệt đơn đang chờ

            // 1. Tìm hồ sơ gốc của Thí sinh bằng CCCD
            ThiSinh ts = thiSinhDAO.findByCccd(yeuCau.getCccd()).orElse(null);
            if (ts == null) return false;

            // 2. Cập nhật dữ liệu thật vào bảng ThiSinh
            if (yeuCau.getDienThoai() != null) ts.setDienThoai(yeuCau.getDienThoai());
            if (yeuCau.getEmail() != null) ts.setEmail(yeuCau.getEmail());
            if (yeuCau.getNoiSinh() != null) ts.setNoiSinh(yeuCau.getNoiSinh());
            if (yeuCau.getKhuVuc() != null) ts.setKhuVuc(yeuCau.getKhuVuc());
            if (yeuCau.getDoiTuong() != null) ts.setDoiTuong(yeuCau.getDoiTuong());
            
            thiSinhDAO.update(ts);

            // 3. Đổi trạng thái bảng Yêu cầu
            yeuCau.setTrangThai("ACCEPTED");
            yeuCau.setNote("Hồ sơ hợp lệ, đã duyệt cập nhật.");
            yeuCauDAO.update(yeuCau);

            // 4. Ghi log Admin
            SystemLogger.log(
                adminId, 
                adminUsername, 
                "Duyệt yêu cầu cập nhật của TS: " + yeuCau.getCccd(), 
                true
            );
            return true;

        } catch (Exception e) {
            System.err.println("[YeuCauService] Lỗi duyệt yêu cầu: " + e.getMessage());
            return false;
        }
    }

    // =====================================================================
    // 3. ADMIN: từ chối yêu cầu
    // =====================================================================
    public boolean tuChoiYeuCau(int idYeuCau, String lyDoTuChoi, Integer adminId, String adminUsername) {
        try {
            Optional<YeuCauCapNhat> optYeuCau = yeuCauDAO.findById(idYeuCau);
            if (optYeuCau.isEmpty()) return false;

            YeuCauCapNhat yeuCau = optYeuCau.get();
            if (!"PENDING".equals(yeuCau.getTrangThai())) return false;

            // 1. Chỉ cập nhật trạng thái bảng Yêu cầu, KHÔNG đụng vào bảng ThiSinh gốc
            yeuCau.setTrangThai("REJECTED");
            yeuCau.setNote(lyDoTuChoi != null ? lyDoTuChoi : "Hồ sơ không hợp lệ.");
            yeuCauDAO.update(yeuCau);

            // 2. Ghi log Admin
            SystemLogger.log(
                adminId, 
                adminUsername, 
                "Từ chối yêu cầu cập nhật của TS: " + yeuCau.getCccd() + " (Lý do: " + lyDoTuChoi + ")", 
                true
            );
            return true;

        } catch (Exception e) {
            System.err.println("[YeuCauService] Lỗi từ chối yêu cầu: " + e.getMessage());
            return false;
        }
    }

    // =====================================================================
    // 4. ADMIN: đếm số lượng yêu cầu đang chờ duyệt
    // =====================================================================
    public java.util.concurrent.CompletableFuture<Long> demYeuCauChoDuyet() {
        return yeuCauDAO.countPendingRequests();
    }

    // =====================================================================
    // 5. ADMIN: lấy thông tin chi tiết của yêu cầu
    // =====================================================================
    public java.util.concurrent.CompletableFuture<ThiSinh> layThongTinGoc(String cccd) {
        return java.util.concurrent.CompletableFuture.supplyAsync(() -> {
            return thiSinhDAO.findByCccd(cccd).orElse(new ThiSinh());
        });
    }

    // =====================================================================
    // THISINH: functions
    // =====================================================================

    // 1. Lấy Yêu cầu mới nhất để hiển thị khung to ở Profile
    public YeuCauCapNhat layYeuCauMoiNhat(String cccd) {
        if (cccd == null || cccd.isBlank()) return null;
        return yeuCauDAO.findLatestByCccd(cccd);
    }

    // 2. Lấy danh sách Thông báo (Các yêu cầu đã có kết quả) để gắn lên Navbar
    public List<YeuCauCapNhat> layDanhSachThongBao(String cccd) {
        if (cccd == null || cccd.isBlank()) return java.util.Collections.emptyList();
        return yeuCauDAO.findNotificationsByCccd(cccd);
    }

    // 3. Đánh dấu tất cả thông báo đã đọc
    public boolean danhDauDaDoc(String cccd) {
        if (cccd == null || cccd.isBlank()) return false;
        try {
            yeuCauDAO.markAllAsRead(cccd);
            return true;
        } catch (Exception e) {
            System.err.println("[Service] Lỗi markAllAsRead: " + e.getMessage());
            return false;
        }
    }

    // =====================================================================
    // LƯU YÊU CẦU MỚI TỪ THÍ SINH
    // =====================================================================
    public void save(YeuCauCapNhat yc) {
        if (yc != null) {
            yeuCauDAO.save(yc);
        }
    }

}