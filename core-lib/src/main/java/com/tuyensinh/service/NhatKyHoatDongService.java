package com.tuyensinh.service;

import com.tuyensinh.dao.NhatKyHoatDongDAO;
import com.tuyensinh.model.NhatKyHoatDong;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public class NhatKyHoatDongService {
    
    private final NhatKyHoatDongDAO nhatKyDAO = new NhatKyHoatDongDAO();

    // =====================================================================
    // LẤY DANH SÁCH LOG MỚI NHẤT (CHO DASHBOARD)
    // =====================================================================
    public CompletableFuture<List<NhatKyHoatDong>> getLatestLogs(int limit) {
        // Có thể thêm các logic kiểm tra phân quyền (nếu cần) ở đây trước khi gọi DAO
        // sẽ được update sau..... 
        return nhatKyDAO.getLatestLogs(limit);
    }
}