package com.tuyensinh.dao;

import com.tuyensinh.model.ThiSinh;
import com.tuyensinh.util.HibernateUtil;
import org.hibernate.Session;
import java.util.Optional;

public class ThiSinhDAO extends GenericDAO<ThiSinh> {

    public ThiSinhDAO() {
        super(ThiSinh.class);
    }

    public Optional<ThiSinh> findByCccd(String cccd) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            ThiSinh ts = session.createQuery(
                "FROM ThiSinh WHERE cccd = :cccd",
                ThiSinh.class
            )
            .setParameter("cccd", cccd)
            .uniqueResult();

            return Optional.ofNullable(ts);
        }
    }
}