package com.tuyensinh.dao;

import com.tuyensinh.model.ThiSinh;
import com.tuyensinh.util.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;

import java.util.Optional;
import java.util.List;
import org.hibernate.query.Query;

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

    public List<ThiSinh> getSearchAndPaging(String keyword, int page, int pageSize) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            String hql = "FROM ThiSinh ts WHERE ts.ho LIKE :kw OR ts.ten LIKE :kw OR ts.cccd LIKE :kw OR ts.sobaodanh LIKE :kw";
            Query<ThiSinh> query = session.createQuery(hql, ThiSinh.class);
            query.setParameter("kw", "%" + keyword + "%");
            query.setFirstResult((page - 1) * pageSize);
            query.setMaxResults(pageSize);
            return query.list();
        }
    }

    public long countSearch(String keyword) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            String hql = "SELECT count(ts) FROM ThiSinh ts WHERE ts.ho LIKE :kw OR ts.ten LIKE :kw OR ts.cccd LIKE :kw OR ts.sobaodanh LIKE :kw";
            Query<Long> query = session.createQuery(hql, Long.class);
            query.setParameter("kw", "%" + keyword + "%");
            return query.uniqueResult();
        }
    }

    public boolean deleteById(int id) {
        Transaction tx = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();
            ThiSinh ts = session.get(ThiSinh.class, id);
            if (ts != null) {
                session.remove(ts);
                tx.commit();
                return true;
            }
            return false;
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            return false;
        }
    }

}