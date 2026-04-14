package com.tuyensinh.dao;

import com.tuyensinh.model.User;
import com.tuyensinh.util.HibernateUtil;
import org.hibernate.Session;
import java.util.Optional;

public class UserDAO extends GenericDAO<User> {
    
    public UserDAO() {
        super(User.class);
    }

    /**
     * Tìm User bằng username và lấy luôn thông tin Nhóm Quyền
     */
    public Optional<User> findByUsername(String username) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            // JOIN FETCH để lấy luôn object nhomQuyen, tránh lỗi Lazy Initialization
            String hql = "SELECT u FROM User u JOIN FETCH u.nhomQuyen WHERE u.username = :username";
            User user = session.createQuery(hql, User.class)
                    .setParameter("username", username)
                    .uniqueResult();
            return Optional.ofNullable(user);
        } catch (Exception e) {
            System.err.println("Lỗi tìm User: " + e.getMessage());
            return Optional.empty();
        }
    }
}