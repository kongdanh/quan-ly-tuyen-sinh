package com.tuyensinh.service;

import com.tuyensinh.dao.ThiSinhDAO;
import com.tuyensinh.model.ThiSinh;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public class ThiSinhService {

    private final ThiSinhDAO thiSinhDAO = new ThiSinhDAO();

    public Optional<ThiSinh> findByCccd(String cccd) {
        return thiSinhDAO.findByCccd(cccd);
    }

    public List<ThiSinh> getSearchAndPaging(String keyword, int page, int pageSize) {
        return thiSinhDAO.getSearchAndPaging(keyword, page, pageSize);
    }

    public long countSearch(String keyword) {
        return thiSinhDAO.countSearch(keyword);
    }

    public boolean deleteById(int id) {
        return thiSinhDAO.deleteById(id);
    }

    public void save(ThiSinh entity) {
        thiSinhDAO.save(entity);
    }

    public void update(ThiSinh entity) {
        thiSinhDAO.update(entity);
    }

    public CompletableFuture<ThiSinh> findByIdAsync(Serializable id) {
        return thiSinhDAO.findByIdAsync(id);
    }

    public CompletableFuture<Boolean> deleteByIdAsync(Serializable id) {
        return thiSinhDAO.deleteByIdAsync(id);
    }

    public CompletableFuture<List<ThiSinh>> findPageWithFilters(
            String keyword,
            List<String> searchFields,
            Map<String, Object> filters,
            int pageIndex,
            int pageSize) {
        return thiSinhDAO.findPageWithFilters(keyword, searchFields, filters, pageIndex, pageSize);
    }

    public CompletableFuture<Long> countWithFiltersAsync(
            String keyword,
            List<String> searchFields,
            Map<String, Object> filters) {
        return thiSinhDAO.countWithFiltersAsync(keyword, searchFields, filters);
    }
}
