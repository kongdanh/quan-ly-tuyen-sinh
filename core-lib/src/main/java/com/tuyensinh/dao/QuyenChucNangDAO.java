package com.tuyensinh.dao;

import com.tuyensinh.model.QuyenChucNang;
import com.tuyensinh.util.HibernateUtil;
import org.hibernate.Session;
import java.util.List;

public class QuyenChucNangDAO extends GenericDAO<QuyenChucNang> {

    public QuyenChucNangDAO() {
        super(QuyenChucNang.class);
    }

    public List<String> getMaChucNangCoXem(Integer idNhom) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            String sql = "SELECT ma_chuc_nang FROM xt_quyen_chuc_nang WHERE id_nhom = :idNhom AND co_xem = 1";
            return session.createNativeQuery(sql, String.class)
                    .setParameter("idNhom", idNhom)
                    .list();
        } catch (Exception e) {
            e.printStackTrace();
            return java.util.Collections.emptyList();
        }
    }

    /**
     * Lấy toàn bộ chi tiết phân quyền
     */
    public List<QuyenChucNang> findByNhomQuyenId(Integer idNhom) {
        if (idNhom == null) return java.util.Collections.emptyList();
        
        try (org.hibernate.Session session = com.tuyensinh.util.HibernateUtil.getSessionFactory().openSession()) {
            String hql = "FROM QuyenChucNang q WHERE q.nhomQuyen.id = :idNhom";
            return session.createQuery(hql, QuyenChucNang.class)
                    .setParameter("idNhom", idNhom)
                    .list();
        } catch (Exception e) {
            System.err.println("[QuyenChucNangDAO] Lỗi findByNhomQuyenId: " + e.getMessage());
            return java.util.Collections.emptyList();
        }
    }

}