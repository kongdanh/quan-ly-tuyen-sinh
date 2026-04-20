package com.tuyensinh.dao;

import com.tuyensinh.model.ToHopMon;
import com.tuyensinh.util.HibernateUtil;
import org.hibernate.Session;

import java.util.ArrayList;
import java.util.List;

public class ToHopMonDAO extends GenericDAO<ToHopMon> {

    public ToHopMonDAO() {
        super(ToHopMon.class);
    }

    /**
     * Lấy toàn bộ danh sách tổ hợp môn
     */
    public List<ToHopMon> getAll() {
        List<ToHopMon> list = new ArrayList<>();

        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            String hql = "FROM ToHopMon";
            list = session.createQuery(hql, ToHopMon.class).getResultList();
        } catch (Exception e) {
            System.err.println("Lỗi khi lấy danh sách tổ hợp môn: " + e.getMessage());
        }

        return list;
    }

    
}