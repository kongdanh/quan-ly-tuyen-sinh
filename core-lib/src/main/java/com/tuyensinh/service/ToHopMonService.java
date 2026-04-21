package com.tuyensinh.service;

import com.tuyensinh.dao.ToHopMonDAO;
import com.tuyensinh.model.ToHopMon;

import java.util.List;

public class ToHopMonService {

    private final ToHopMonDAO toHopMonDAO = new ToHopMonDAO();

    /**
     * Lấy toàn bộ danh sách tổ hợp môn
     */
    public List<ToHopMon> getAll() {
        return toHopMonDAO.findAllSync();
    }
}