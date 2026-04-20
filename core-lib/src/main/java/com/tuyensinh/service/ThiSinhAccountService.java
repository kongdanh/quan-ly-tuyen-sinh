package com.tuyensinh.service;

import com.tuyensinh.dao.ThiSinhAccountDAO;
import com.tuyensinh.model.ThiSinhAccount;
import java.util.Optional;

public class ThiSinhAccountService {
    private final ThiSinhAccountDAO accDAO = new ThiSinhAccountDAO();

    public Optional<ThiSinhAccount> findByCccd(String cccd) {
        if (cccd == null || cccd.isBlank()) return Optional.empty();
        return accDAO.findByCccd(cccd);
    }

    public boolean update(ThiSinhAccount acc) {
        try {
            accDAO.update(acc);
            return true;
        } catch (Exception e) {
            System.err.println("[AccountService] Lỗi cập nhật tài khoản: " + e.getMessage());
            return false;
        }
    }
}