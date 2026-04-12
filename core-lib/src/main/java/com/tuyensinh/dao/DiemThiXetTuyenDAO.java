package com.tuyensinh.dao;

import com.tuyensinh.model.DiemThiXetTuyen;
import com.tuyensinh.util.HibernateUtil;
import org.hibernate.Session;
import java.util.Optional;

public class DiemThiXetTuyenDAO extends GenericDAO<DiemThiXetTuyen> {

    public DiemThiXetTuyenDAO() {
        super(DiemThiXetTuyen.class);
    }

    public Optional<DiemThiXetTuyen> findByCccd(String cccd) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            String hql = "SELECT d FROM DiemThiXetTuyen d WHERE d.thiSinh.cccd = :cccd";
            DiemThiXetTuyen diem = session.createQuery(hql, DiemThiXetTuyen.class)
                    .setParameter("cccd", cccd)
                    .uniqueResult();
            return Optional.ofNullable(diem);
        }
    }
}