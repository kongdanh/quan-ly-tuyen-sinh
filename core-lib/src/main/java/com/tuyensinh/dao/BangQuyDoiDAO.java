package com.tuyensinh.dao;

import java.util.List;

import com.tuyensinh.model.BangQuyDoi;

public class BangQuyDoiDAO extends GenericDAO<BangQuyDoi> {

    public BangQuyDoiDAO() {
        super(BangQuyDoi.class);
    }

    // Thêm hàm này vào BangQuyDoiDAO.java
    public List<BangQuyDoi> getBangQuyDoiByType(String type) {
        try (org.hibernate.Session session = com.tuyensinh.util.HibernateUtil.getSessionFactory().openSession()) {
            // Lấy danh sách phân vị theo phương thức (Ví dụ: DGNL) và sắp xếp tăng dần theo điểm gốc (dDiema)
            String hql = "FROM BangQuyDoi WHERE dPhuongthuc = :type ORDER BY dDiema ASC";
            return session.createQuery(hql, BangQuyDoi.class)
                          .setParameter("type", type)
                          .list();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    // Thêm vào BangQuyDoiDAO.java
    public List<BangQuyDoi> getBangQuyDoiByTypeAndMon(String type, String mon) {
        try (org.hibernate.Session session = com.tuyensinh.util.HibernateUtil.getSessionFactory().openSession()) {
            String hql = "FROM BangQuyDoi WHERE dPhuongthuc = :type AND dMon = :mon ORDER BY dDiema ASC";
            return session.createQuery(hql, BangQuyDoi.class)
                          .setParameter("type", type)
                          .setParameter("mon", mon)
                          .list();
        } catch (Exception e) {
            e.printStackTrace();
            return java.util.Collections.emptyList();
        }
    }

    // Lấy danh sách tổ hợp môn của một Ngành cụ thể
    public java.util.List<com.tuyensinh.model.NganhToHop> findByMaNganh(String maNganh) {
        try (org.hibernate.Session session = com.tuyensinh.util.HibernateUtil.getSessionFactory().openSession()) {
            String hql = "SELECT nt FROM NganhToHop nt JOIN FETCH nt.toHopMon WHERE nt.nganh.manganh = :maNganh";
            return session.createQuery(hql, com.tuyensinh.model.NganhToHop.class)
                          .setParameter("maNganh", maNganh)
                          .list();
        } catch (Exception e) {
            e.printStackTrace();
            return java.util.Collections.emptyList();
        }
    }

}
