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
     * Lấy toàn bộ danh sách ngành
     * @return List<Nganh>
     */
    public List<Nganh> getAll() {
        return nganhDAO.getAll();
    }
}