package com.tuyensinh.dao;

import com.tuyensinh.model.ToHopMon;

import com.tuyensinh.util.HibernateUtil;
import org.hibernate.Session;
import java.util.Collections;
import java.util.List;

public class ToHopMonDAO extends GenericDAO<ToHopMon> {

    public ToHopMonDAO() {
        super(ToHopMon.class);
    }

    public List<ToHopMon> findNotInNganh(String maNganh) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            String hql = "SELECT t FROM ToHopMon t WHERE t.matohop NOT IN " +
                         "(SELECT nt.toHopMon.matohop FROM NganhToHop nt WHERE nt.nganh.manganh = :maNganh)";
            return session.createQuery(hql, ToHopMon.class)
                    .setParameter("maNganh", maNganh)
                    .list();
        } catch (Exception e) {
            System.err.println("Lỗi truy vấn ToHopMonDAO.findNotInNganh: " + e.getMessage());
            return Collections.emptyList();
        }
    }
}