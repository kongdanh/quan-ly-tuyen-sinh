package com.tuyensinh.dao;

import com.tuyensinh.model.NganhToHop;
import com.tuyensinh.model.NguyenVong;
import com.tuyensinh.util.HibernateUtil;
import org.hibernate.Session;

import java.util.Collections;
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

    public List<NguyenVong> findByDotTuyenSinh(Integer idDot) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            String hql = "SELECT nv FROM NguyenVong nv " +
                         "JOIN FETCH nv.thiSinh " +
                         "JOIN FETCH nv.nganh " +
                         "WHERE nv.dotTuyenSinh.id = :idDot " +
                         "ORDER BY nv.diemXettuyen DESC";
            return session.createQuery(hql, NguyenVong.class)
                    .setParameter("idDot", idDot)
                    .list();
        } catch (Exception e) {
            e.printStackTrace();
            return Collections.emptyList();
        }
    }

    /**
     * Tìm các tổ hợp ngành mà thí sinh đã đăng ký.
     */
    public List<NganhToHop> findNganhToHopByCccd(String cccd) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            String hql = "SELECT DISTINCT nth " +
                         "FROM NguyenVong nv " +
                         "JOIN nv.nganh n " +
                         "JOIN NganhToHop nth ON nth.nganh = n " +
                         "JOIN FETCH nth.nganh " +
                         "JOIN FETCH nth.toHopMon " +
                         "WHERE nv.thiSinh.cccd = :cccd";

            return session.createQuery(hql, NganhToHop.class)
                    .setParameter("cccd", cccd)
                    .list();
        } catch (Exception e) {
            System.err.println("[NguyenVongDAO] Lỗi findNganhToHopByCccd: " + e.getMessage());
            return Collections.emptyList();
        }
    }

    /**
     * Tìm các nguyện vọng theo ID hồ sơ tuyển sinh.
     */
    public List<NguyenVong> findByHoSoId(Integer idHoSo) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            String hql = "SELECT nv FROM NguyenVong nv WHERE nv.hoSoTuyenSinh.id = :idHoSo ORDER BY nv.nvTt ASC";
            return session.createQuery(hql, NguyenVong.class)
                    .setParameter("idHoSo", idHoSo)
                    .list();
        } catch (Exception e) {
            System.err.println("[NguyenVongDAO] Lỗi findByHoSoId: " + e.getMessage());
            return Collections.emptyList();
        }
    }
}