package com.tuyensinh.service;

import com.tuyensinh.dao.NhatKyHoatDongDAO;
import com.tuyensinh.model.NhatKyHoatDong;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public class NhatKyHoatDongService {
    
    private final NhatKyHoatDongDAO nhatKyDAO = new NhatKyHoatDongDAO();

    /**
     * Lay danh sach log moi nhat (dung cho Dashboard)
     */
    public CompletableFuture<List<NhatKyHoatDong>> getLatestLogs(int limit) {
        return nhatKyDAO.getLatestLogs(limit);
    }
}