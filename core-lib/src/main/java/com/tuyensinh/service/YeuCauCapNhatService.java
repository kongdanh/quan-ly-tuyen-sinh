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

    /**
     * Lay danh sach yeu cau dang cho duyet (PENDING)
     */
    public java.util.concurrent.CompletableFuture<java.util.List<YeuCauCapNhat>> layDanhSachChoDuyet() {
        return yeuCauDAO.findPendingRequests();
    }

    /**
     * Thi sinh tao yeu cau cap nhat thong tin ca nhan
     */
    public boolean taoYeuCau(YeuCauCapNhat yeuCau, Integer idThiSinh, String tenThiSinh) {
        try {
            System.out.println("[YeuCauCapNhatService] Thi sinh " + tenThiSinh + " gui yeu cau cap nhat");
            yeuCau.setTrangThai("PENDING");
            yeuCauDAO.save(yeuCau);
            SystemLogger.log(idThiSinh, tenThiSinh, "Gửi yêu cầu cập nhật thông tin (Chờ duyệt)", true);
            System.out.println("[YeuCauCapNhatService] Tao yeu cau thanh cong");
            return true;
        } catch (Exception e) {
            System.err.println("[YeuCauCapNhatService] Loi tao yeu cau: " + e.getMessage());
            SystemLogger.log(idThiSinh, tenThiSinh, "Gửi yêu cầu cập nhật thông tin", false);
            return false;
        }
    }

    /**
     * Admin duyet yeu cau cap nhat: cap nhat du lieu that vao bang ThiSinh
     */
    public boolean duyetYeuCau(int idYeuCau, Integer adminId, String adminUsername) {
        try {
            Optional<YeuCauCapNhat> optYeuCau = yeuCauDAO.findById(idYeuCau);
            if (optYeuCau.isEmpty()) return false;

            YeuCauCapNhat yeuCau = optYeuCau.get();
            if (!"PENDING".equals(yeuCau.getTrangThai())) return false;

            ThiSinh ts = thiSinhDAO.findByCccd(yeuCau.getCccd()).orElse(null);
            if (ts == null) return false;

            System.out.println("[YeuCauCapNhatService] Admin " + adminUsername + " duyet yeu cau ID=" + idYeuCau);

            if (yeuCau.getDienThoai() != null) ts.setDienThoai(yeuCau.getDienThoai());
            if (yeuCau.getEmail() != null) ts.setEmail(yeuCau.getEmail());
            if (yeuCau.getNoiSinh() != null) ts.setNoiSinh(yeuCau.getNoiSinh());
            if (yeuCau.getKhuVuc() != null) ts.setKhuVuc(yeuCau.getKhuVuc());
            if (yeuCau.getDoiTuong() != null) ts.setDoiTuong(yeuCau.getDoiTuong());
            
            thiSinhDAO.update(ts);

            yeuCau.setTrangThai("ACCEPTED");
            yeuCau.setNote("Ho so hop le, da duyet cap nhat.");
            yeuCauDAO.update(yeuCau);

            System.out.println("[YeuCauCapNhatService] Duyet yeu cau thanh cong ID=" + idYeuCau);
            SystemLogger.log(adminId, adminUsername, "Duyệt yêu cầu cập nhật của TS: " + yeuCau.getCccd(), true);
            return true;

        } catch (Exception e) {
            System.err.println("[YeuCauCapNhatService] Loi duyet yeu cau: " + e.getMessage());
            SystemLogger.log(adminId, adminUsername, "Lỗi duyệt yêu cầu ID=" + idYeuCau + ": " + e.getMessage(), false);
            return false;
        }
    }

    /**
     * Admin tu choi yeu cau cap nhat (chi doi trang thai, khong thay doi du lieu goc)
     */
    public boolean tuChoiYeuCau(int idYeuCau, String lyDoTuChoi, Integer adminId, String adminUsername) {
        try {
            Optional<YeuCauCapNhat> optYeuCau = yeuCauDAO.findById(idYeuCau);
            if (optYeuCau.isEmpty()) return false;

            YeuCauCapNhat yeuCau = optYeuCau.get();
            if (!"PENDING".equals(yeuCau.getTrangThai())) return false;

            System.out.println("[YeuCauCapNhatService] Admin " + adminUsername + " tu choi yeu cau ID=" + idYeuCau);

            yeuCau.setTrangThai("REJECTED");
            yeuCau.setNote(lyDoTuChoi != null ? lyDoTuChoi : "Ho so khong hop le.");
            yeuCauDAO.update(yeuCau);

            System.out.println("[YeuCauCapNhatService] Tu choi yeu cau thanh cong ID=" + idYeuCau);
            SystemLogger.log(adminId, adminUsername, "Từ chối yêu cầu cập nhật của TS: " + yeuCau.getCccd() + " (Lý do: " + lyDoTuChoi + ")", true);
            return true;

        } catch (Exception e) {
            System.err.println("[YeuCauCapNhatService] Loi tu choi yeu cau: " + e.getMessage());
            return false;
        }
    }

    /**
     * Dem so luong yeu cau dang cho duyet
     */
    public java.util.concurrent.CompletableFuture<Long> demYeuCauChoDuyet() {
        return yeuCauDAO.countPendingRequests();
    }

    /**
     * Lay thong tin goc cua thi sinh de so sanh voi yeu cau cap nhat
     */
    public java.util.concurrent.CompletableFuture<ThiSinh> layThongTinGoc(String cccd) {
        return java.util.concurrent.CompletableFuture.supplyAsync(() -> {
            return thiSinhDAO.findByCccd(cccd).orElse(new ThiSinh());
        });
    }

    /**
     * Lay yeu cau moi nhat cua thi sinh (hien thi o trang Profile)
     */
    public YeuCauCapNhat layYeuCauMoiNhat(String cccd) {
        if (cccd == null || cccd.isBlank()) return null;
        return yeuCauDAO.findLatestByCccd(cccd);
    }

    /**
     * Lay danh sach thong bao (cac yeu cau da co ket qua) cho Navbar
     */
    public List<YeuCauCapNhat> layDanhSachThongBao(String cccd) {
        if (cccd == null || cccd.isBlank()) return java.util.Collections.emptyList();
        return yeuCauDAO.findNotificationsByCccd(cccd);
    }

    /**
     * Danh dau tat ca thong bao da doc
     */
    public boolean danhDauDaDoc(String cccd) {
        if (cccd == null || cccd.isBlank()) return false;
        try {
            yeuCauDAO.markAllAsRead(cccd);
            System.out.println("[YeuCauCapNhatService] Danh dau da doc tat ca thong bao cho CCCD=" + cccd);
            return true;
        } catch (Exception e) {
            System.err.println("[YeuCauCapNhatService] Loi markAllAsRead: " + e.getMessage());
            return false;
        }
    }

    /**
     * Luu yeu cau cap nhat moi tu thi sinh
     */
    public void save(YeuCauCapNhat yc) {
        if (yc != null) {
            System.out.println("[YeuCauCapNhatService] Luu yeu cau cap nhat moi");
            yeuCauDAO.save(yc);
        }
    }

}