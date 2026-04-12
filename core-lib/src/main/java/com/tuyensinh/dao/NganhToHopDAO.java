package com.tuyensinh.dao;

import com.tuyensinh.model.NganhToHop;
import com.tuyensinh.util.HibernateUtil;
import org.hibernate.Session;

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
}