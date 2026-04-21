package com.tuyensinh.service;

import com.tuyensinh.dao.UserDAO;
import com.tuyensinh.model.User;
import com.tuyensinh.util.HibernateUtil;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import org.hibernate.Session;

public class UserService {
    private final UserDAO userDAO = new UserDAO();

    public List<User> findAllSync() {
        return userDAO.findAllSync();
    }

    public void save(User user) {
        userDAO.save(user);
    }

    public void update(User user) {
        userDAO.update(user);
    }

    public void delete(User user) {
        userDAO.delete(user);
    }

    public CompletableFuture<List<User>> findPageWithFilters(String keyword, List<String> searchFields, Map<String, Object> filters, int pageIndex, int pageSize) {
        return userDAO.findPageWithFilters(keyword, searchFields, filters, pageIndex, pageSize);
    }

    public CompletableFuture<Long> countWithFiltersAsync(String keyword, List<String> searchFields, Map<String, Object> filters) {
        return userDAO.countWithFiltersAsync(keyword, searchFields, filters);
    }

    public User findById(Serializable id) {
        return userDAO.findById(id);
    }

    public CompletableFuture<Boolean> deleteByIdAsync(Serializable id) {
        return userDAO.deleteByIdAsync(id);
    }

    public boolean isUsernameExists(String username) {
    try (Session session = HibernateUtil.getSessionFactory().openSession()) {
        Long count = session.createQuery("SELECT count(u) FROM User u WHERE u.username = :un", Long.class)
                            .setParameter("un", username).uniqueResult();
        return count > 0;
    }
    }

}