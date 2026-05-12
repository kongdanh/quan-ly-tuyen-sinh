package com.tuyensinh.service;

import com.tuyensinh.dao.UserDAO;
import com.tuyensinh.model.User;
import com.tuyensinh.util.HibernateUtil;
import com.tuyensinh.util.SystemLogger;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import org.hibernate.Session;

public class UserService {
    private final UserDAO userDAO = new UserDAO();

    /**
     * Lay toan bo danh sach user (dong bo)
     */
    public List<User> findAllSync() {
        return userDAO.findAllSync();
    }

    /**
     * Them user moi
     */
    public void save(User user) {
        System.out.println("[UserService] Them user: " + user.getUsername());
        userDAO.save(user);
        System.out.println("[UserService] Them user thanh cong: " + user.getUsername());
        SystemLogger.log(null, "System", "Thêm user: " + user.getUsername() + " - " + user.getHoTen(), true);
    }

    /**
     * Cap nhat thong tin user
     */
    public void update(User user) {
        System.out.println("[UserService] Cap nhat user: " + user.getUsername());
        userDAO.update(user);
        System.out.println("[UserService] Cap nhat user thanh cong: " + user.getUsername());
        SystemLogger.log(null, "System", "Cập nhật user: " + user.getUsername() + " - " + user.getHoTen(), true);
    }

    /**
     * Xoa user
     */
    public void delete(User user) {
        System.out.println("[UserService] Xoa user: " + user.getUsername());
        userDAO.delete(user);
        System.out.println("[UserService] Xoa user thanh cong: " + user.getUsername());
        SystemLogger.log(null, "System", "Xóa user: " + user.getUsername() + " - " + user.getHoTen(), true);
    }

    /**
     * Phan trang danh sach user voi bo loc
     */
    public CompletableFuture<List<User>> findPageWithFilters(String keyword, List<String> searchFields, Map<String, Object> filters, int pageIndex, int pageSize) {
        return userDAO.findPageWithFilters(keyword, searchFields, filters, pageIndex, pageSize);
    }

    /**
     * Dem tong so ban ghi user theo bo loc
     */
    public CompletableFuture<Long> countWithFiltersAsync(String keyword, List<String> searchFields, Map<String, Object> filters) {
        return userDAO.countWithFiltersAsync(keyword, searchFields, filters);
    }

    public User findById(Serializable id) {
        return userDAO.findById(id);
    }

    /**
     * Xoa user theo ID (async)
     */
    public CompletableFuture<Boolean> deleteByIdAsync(Serializable id) {
        System.out.println("[UserService] Xoa user async ID=" + id);
        SystemLogger.log(null, "System", "Xóa user ID=" + id, true);
        return userDAO.deleteByIdAsync(id);
    }

    /**
     * Kiem tra username da ton tai trong he thong chua
     */
    public boolean isUsernameExists(String username) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Long count = session.createQuery("SELECT count(u) FROM User u WHERE u.username = :un", Long.class)
                                .setParameter("un", username).uniqueResult();
            return count > 0;
        }
    }

}