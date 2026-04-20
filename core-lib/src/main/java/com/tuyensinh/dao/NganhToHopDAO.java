package com.tuyensinh.dao;

import com.tuyensinh.model.NganhToHop;
import com.tuyensinh.util.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;

import java.util.Collections;
import java.util.List;

public class NganhToHopDAO extends GenericDAO<NganhToHop> {

    public NganhToHopDAO() {
        super(NganhToHop.class);
    }

    

    /**
     * Lấy danh sách Tổ hợp môn được phép xét tuyển cho 1 Ngành cụ thể
     */
    public List<NganhToHop> findByMaNganh(String maNganh) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            String hql = "SELECT nt FROM NganhToHop nt JOIN FETCH nt.nganh WHERE nt.nganh.manganh = :maNganh";
            return session.createQuery(hql, NganhToHop.class)
                    .setParameter("maNganh", maNganh)
                    .list();
        } catch (Exception e) {
            System.err.println("Lỗi truy vấn NganhToHopDAO: " + e.getMessage());
            return java.util.Collections.emptyList();
        }
    }

     public NganhToHop findByMaNganhAndMaToHop(String maNganh, String maToHop) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            String hql = "SELECT nt FROM NganhToHop nt " +
                    "JOIN FETCH nt.nganh " +
                    "JOIN FETCH nt.toHopMon " +
                    "WHERE nt.nganh.manganh = :maNganh " +
                    "AND nt.toHopMon.matohop = :maToHop";

            return session.createQuery(hql, NganhToHop.class)
                    .setParameter("maNganh", maNganh)
                    .setParameter("maToHop", maToHop)
                    .uniqueResult();

        } catch (Exception e) {
            System.err.println("Lỗi findByMaNganhAndMaToHop: " + e.getMessage());
            return null;
        }
    }

    public NganhToHop findByTbKeys(String tbKeys) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            String hql = "FROM NganhToHop nt WHERE nt.tbKeys = :tbKeys";

            return session.createQuery(hql, NganhToHop.class)
                    .setParameter("tbKeys", tbKeys)
                    .uniqueResult();

        } catch (Exception e) {
            System.err.println("Lỗi findByTbKeys: " + e.getMessage());
            return null;
        }
    }
    public void save(NganhToHop entity) {
        Transaction tx = null;

        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();

            session.saveOrUpdate(entity);

            tx.commit();
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            System.err.println("Lỗi save: " + e.getMessage());
        }
    }

    public void deleteById(Integer id) {
        Transaction tx = null;

        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();

            NganhToHop entity = session.get(NganhToHop.class, id);
            if (entity != null) {
                session.delete(entity);
            }

            tx.commit();
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            System.err.println("Lỗi deleteById: " + e.getMessage());
        }
    }

    public List<NganhToHop> findAllFull() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            String hql = "SELECT nt FROM NganhToHop nt " +
                    "JOIN FETCH nt.nganh " +
                    "JOIN FETCH nt.toHopMon";

            return session.createQuery(hql, NganhToHop.class).list();

        } catch (Exception e) {
            System.err.println("Lỗi findAllFull: " + e.getMessage());
            return Collections.emptyList();
        }
    }

    public List<NganhToHop> findWithFilters(
            String keyword,
            String tenNganh,
            String maToHop,
            int page,
            int pageSize
    ) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {

            StringBuilder hql = new StringBuilder(
                "SELECT nt FROM NganhToHop nt " +
                "JOIN FETCH nt.nganh n " +
                "JOIN FETCH nt.toHopMon th WHERE 1=1 "
            );

            if (keyword != null && !keyword.isEmpty()) {
                hql.append(" AND (LOWER(n.tennganh) LIKE :keyword OR LOWER(th.matohop) LIKE :keyword)");
            }

            if (tenNganh != null && !tenNganh.isEmpty()) {
                hql.append(" AND LOWER(n.tennganh) LIKE :tenNganh");
            }

            if (maToHop != null && !maToHop.isEmpty()) {
                hql.append(" AND LOWER(th.matohop) LIKE :maToHop");
            }

            var query = session.createQuery(hql.toString(), NganhToHop.class);

            if (keyword != null && !keyword.isEmpty()) {
                query.setParameter("keyword", "%" + keyword.toLowerCase() + "%");
            }

            if (tenNganh != null && !tenNganh.isEmpty()) {
                query.setParameter("tenNganh", "%" + tenNganh.toLowerCase() + "%");
            }

            if (maToHop != null && !maToHop.isEmpty()) {
                query.setParameter("maToHop", "%" + maToHop.toLowerCase() + "%");
            }

            return query
                    .setFirstResult(page * pageSize)
                    .setMaxResults(pageSize)
                    .list();

        } catch (Exception e) {
            System.err.println("Lỗi findWithFilters: " + e.getMessage());
            return Collections.emptyList();
        }
    }

    public Long countWithFilters(
            String keyword,
            String tenNganh,
            String maToHop
    ) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {

            StringBuilder hql = new StringBuilder(
                "SELECT COUNT(nt.id) FROM NganhToHop nt " +
                "JOIN nt.nganh n " +
                "JOIN nt.toHopMon th WHERE 1=1 "
            );

            if (keyword != null && !keyword.isEmpty()) {
                hql.append(" AND (LOWER(n.tennganh) LIKE :keyword OR LOWER(th.matohop) LIKE :keyword)");
            }

            if (tenNganh != null && !tenNganh.isEmpty()) {
                hql.append(" AND LOWER(n.tennganh) LIKE :tenNganh");
            }

            if (maToHop != null && !maToHop.isEmpty()) {
                hql.append(" AND LOWER(th.matohop) LIKE :maToHop");
            }

            var query = session.createQuery(hql.toString(), Long.class);

            if (keyword != null && !keyword.isEmpty()) {
                query.setParameter("keyword", "%" + keyword.toLowerCase() + "%");
            }

            if (tenNganh != null && !tenNganh.isEmpty()) {
                query.setParameter("tenNganh", "%" + tenNganh.toLowerCase() + "%");
            }

            if (maToHop != null && !maToHop.isEmpty()) {
                query.setParameter("maToHop", "%" + maToHop.toLowerCase() + "%");
            }

            return query.uniqueResult();

        } catch (Exception e) {
            System.err.println("Lỗi countWithFilters: " + e.getMessage());
            return 0L;
        }
    }
}