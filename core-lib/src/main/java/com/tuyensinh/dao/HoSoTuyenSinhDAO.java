package com.tuyensinh.dao;

import com.tuyensinh.model.HoSoTuyenSinh;
import com.tuyensinh.util.HibernateUtil;
import org.hibernate.Session;

import java.util.Optional;

public class HoSoTuyenSinhDAO extends GenericDAO<HoSoTuyenSinh> {
    public HoSoTuyenSinhDAO() {
        super(HoSoTuyenSinh.class);
    }

    /**
     * Tìm hồ sơ tuyển sinh theo ID ThiSinh và ID DotTuyenSinh
     */
    public Optional<HoSoTuyenSinh> findByThiSinhAndDot(Integer idThiSinh, Integer idDot) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            String hql = "SELECT hs FROM HoSoTuyenSinh hs WHERE hs.thiSinh.id = :idThiSinh AND hs.dotTuyenSinh.id = :idDot";
            return Optional.ofNullable(
                session.createQuery(hql, HoSoTuyenSinh.class)
                    .setParameter("idThiSinh", idThiSinh)
                    .setParameter("idDot", idDot)
                    .uniqueResult()
            );
        } catch (Exception e) {
            System.err.println("[HoSoTuyenSinhDAO] Lỗi findByThiSinhAndDot: " + e.getMessage());
            return Optional.empty();
        }
    }
}