package com.tuyensinh.service;

import com.tuyensinh.dao.ToHopMonDAO;
import com.tuyensinh.model.ToHopMon;

import java.util.List;

public class ToHopMonService {

    private final ToHopMonDAO toHopMonDAO = new ToHopMonDAO();

    /**
     * Lay toan bo danh sach to hop mon
     */
    public List<ToHopMon> getAll() {
        return toHopMonDAO.findAllSync();
    }

    /**
     * Lay danh sach to hop mon chua duoc gan cho nganh
     */
    public List<ToHopMon> getAvailableForNganh(String maNganh) {
        if (maNganh == null || maNganh.isEmpty()) {
            return getAll();
        }
        return toHopMonDAO.findNotInNganh(maNganh);
    }
}