package com.tuyensinh.service;

import com.tuyensinh.dao.NhomQuyenDAO;
import com.tuyensinh.model.NhomQuyen;
import com.tuyensinh.util.HibernateUtil;
import com.tuyensinh.util.SystemLogger;

import java.util.List;

import org.hibernate.Session;

public class NhomQuyenService {
    private final NhomQuyenDAO nhomQuyenDAO = new NhomQuyenDAO();

    /**
     * Lay toan bo danh sach nhom quyen
     */
    public List<NhomQuyen> findAllSync() {
        return nhomQuyenDAO.findAllSync();
    }

    /**
     * Tim nhom quyen theo ID
     */
    public NhomQuyen findById(Integer id) {
        if (id == null) return null;
        return nhomQuyenDAO.findById(id); 
    }

    /**
     * Them nhom quyen moi
     */
    public void save(NhomQuyen nq) { 
        System.out.println("[NhomQuyenService] Them nhom quyen: " + nq.getTenNhom());
        nhomQuyenDAO.save(nq); 
        System.out.println("[NhomQuyenService] Them nhom quyen thanh cong: " + nq.getMaNhom());
        SystemLogger.log(null, "System", "Thêm nhóm quyền: " + nq.getTenNhom(), true);
    }

    /**
     * Cap nhat nhom quyen
     */
    public void update(NhomQuyen nq) { 
        System.out.println("[NhomQuyenService] Cap nhat nhom quyen: " + nq.getMaNhom());
        nhomQuyenDAO.update(nq); 
        System.out.println("[NhomQuyenService] Cap nhat nhom quyen thanh cong: " + nq.getMaNhom());
        SystemLogger.log(null, "System", "Cập nhật nhóm quyền: " + nq.getTenNhom(), true);
    }

    /**
     * Kiem tra ma nhom quyen da ton tai chua
     */
    public boolean isMaNhomExists(String maNhom) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Long count = session.createQuery("SELECT count(n) FROM NhomQuyen n WHERE n.maNhom = :ma", Long.class)
                                .setParameter("ma", maNhom).uniqueResult();
            return count > 0;
        }
    }
}