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
}