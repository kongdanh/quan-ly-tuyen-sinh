package com.tuyensinh.service;

import com.tuyensinh.dao.NganhDAO;
import com.tuyensinh.model.Nganh;
import java.util.List;

public class NganhService {

    private final NganhDAO nganhDAO;

    public NganhService() {
        this.nganhDAO = new NganhDAO();
    }

    /**
     * Sử dụng Try-Catch để bảo vệ UI khỏi lỗi sập DB.
     * @return List<Nganh> (Trả về list rỗng nếu có lỗi)
     */
    public List<Nganh> findAllSync() {
        try {
            return nganhDAO.findAllSync(); 
        } catch (Exception e) {
            System.err.println("[NganhService] Lỗi lấy danh sách ngành: " + e.getMessage());
            return java.util.Collections.emptyList();
        }
    }
}