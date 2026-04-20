package com.tuyensinh.dao;

import com.tuyensinh.model.NguyenVong;
import com.tuyensinh.util.HibernateUtil;
import org.hibernate.Session;
import java.util.List;

public class NguyenVongDAO extends GenericDAO<NguyenVong> {

    public NguyenVongDAO() {
        super(NguyenVong.class);
    }

    public List<NguyenVong> findByCccd(String cccd) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            String hql = "SELECT nv FROM NguyenVong nv JOIN FETCH nv.nganh WHERE nv.thiSinh.cccd = :cccd ORDER BY nv.nvTt ASC";
            return session.createQuery(hql, NguyenVong.class)
                    .setParameter("cccd", cccd)
                    .list();
        }
    }

    public List<Object[]> countByNganh() {
    try (Session session = HibernateUtil.getSessionFactory().openSession()) {

        String hql = """
            SELECT nv.nganh.manganh, COUNT(nv.id)
            FROM NguyenVong nv
            GROUP BY nv.nganh.manganh
        """;

        return session.createQuery(hql, Object[].class).list();
    }
}
}