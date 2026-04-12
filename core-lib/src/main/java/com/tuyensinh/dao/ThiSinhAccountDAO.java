package com.tuyensinh.dao;

import com.tuyensinh.model.ThiSinhAccount;
import com.tuyensinh.util.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;
import java.time.LocalDateTime;
import java.util.Optional;

public class ThiSinhAccountDAO extends GenericDAO<ThiSinhAccount> {
    public ThiSinhAccountDAO() {
        super(ThiSinhAccount.class);
    }

    public Optional<ThiSinhAccount> findByCccd(String cccd) {
        if (cccd == null || cccd.isBlank()) return Optional.empty();
        
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            String hql = "SELECT a FROM ThiSinhAccount a LEFT JOIN FETCH a.thiSinh ts WHERE ts.cccd = :cccd";
            ThiSinhAccount account = session.createQuery(hql, ThiSinhAccount.class)
                    .setParameter("cccd", cccd.trim())
                    .uniqueResult();
            return Optional.ofNullable(account);
        } catch (Exception e) {
            e.printStackTrace();
            return Optional.empty();
        }
    }
    
    public void updateLastLogin(Integer accountId) {
        Transaction tx = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();
            session.createMutationQuery("UPDATE ThiSinhAccount SET lanDangNhapCuoi = :now WHERE id = :id")
                .setParameter("now", LocalDateTime.now())
                .setParameter("id", accountId)
                .executeUpdate();
            tx.commit();
        } catch (Exception e) {
            if (tx != null && tx.isActive()) tx.rollback();
            e.printStackTrace(); 
        }
    }
}