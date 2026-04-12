package com.tuyensinh.dao;

import com.tuyensinh.model.Nganh;
import com.tuyensinh.util.HibernateUtil;
import org.hibernate.Session;

import java.util.Optional;

public class NganhDAO extends GenericDAO<Nganh> {

    public NganhDAO() {
        super(Nganh.class);
    }

    /**
     * Tìm kiếm Ngành học dựa vào Mã ngành (Ví dụ: "7480201")
     * * @param maNganh Mã ngành cần tìm
     * @return Optional<Nganh> chứa thông tin ngành nếu tìm thấy
     */
    public Optional<Nganh> findByMaNganh(String maNganh) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            String hql = "SELECT n FROM Nganh n WHERE n.manganh = :maNganh";
            Nganh nganh = session.createQuery(hql, Nganh.class)
                    .setParameter("maNganh", maNganh)
                    .uniqueResult();
            
            return Optional.ofNullable(nganh);
        } catch (Exception e) {
            System.err.println("Lỗi khi tìm ngành theo mã: " + e.getMessage());
            return Optional.empty();
        }
    }
}