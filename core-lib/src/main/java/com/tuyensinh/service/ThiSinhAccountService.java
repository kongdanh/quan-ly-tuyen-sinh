package com.tuyensinh.service;

import com.tuyensinh.dao.ThiSinhAccountDAO;
import com.tuyensinh.model.ThiSinhAccount;
import com.tuyensinh.util.SystemLogger;

import java.util.Optional;

public class ThiSinhAccountService {
    private final ThiSinhAccountDAO accDAO = new ThiSinhAccountDAO();

    /**
     * Tim tai khoan thi sinh theo CCCD
     */
    public Optional<ThiSinhAccount> findByCccd(String cccd) {
        if (cccd == null || cccd.isBlank()) return Optional.empty();
        return accDAO.findByCccd(cccd);
    }

    /**
     * Cap nhat thong tin tai khoan thi sinh
     */
    public boolean update(ThiSinhAccount acc) {
        try {
            System.out.println("[ThiSinhAccountService] Cap nhat tai khoan thi sinh ID=" + acc.getId());
            accDAO.update(acc);
            System.out.println("[ThiSinhAccountService] Cap nhat tai khoan thanh cong");
            SystemLogger.log(null, "System", "Cập nhật tài khoản thí sinh ID=" + acc.getId(), true);
            return true;
        } catch (Exception e) {
            System.err.println("[ThiSinhAccountService] Loi cap nhat tai khoan: " + e.getMessage());
            SystemLogger.log(null, "System", "Lỗi cập nhật tài khoản thí sinh ID=" + acc.getId() + ": " + e.getMessage(), false);
            return false;
        }
    }
}