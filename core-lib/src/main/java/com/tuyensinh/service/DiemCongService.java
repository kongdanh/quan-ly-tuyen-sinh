package com.tuyensinh.service;

import com.tuyensinh.dao.DiemCongDAO;
import com.tuyensinh.model.DiemCong;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class DiemCongService {

    private final DiemCongDAO diemCongDAO = new DiemCongDAO();

    public CompletableFuture<List<DiemCong>> findPageWithFilters(
            String keyword,
            List<String> searchFields,
            Map<String, Object> filters,
            int pageIndex,
            int pageSize) {
        return diemCongDAO.findPageWithFilters(keyword, searchFields, filters, pageIndex, pageSize);
    }

    public CompletableFuture<Long> countWithFiltersAsync(
            String keyword,
            List<String> searchFields,
            Map<String, Object> filters) {
        return diemCongDAO.countWithFiltersAsync(keyword, searchFields, filters);
    }

    public void save(DiemCong entity) {
        diemCongDAO.save(entity);
    }

    public void update(DiemCong entity) {
        diemCongDAO.update(entity);
    }

    public CompletableFuture<Boolean> deleteByIdAsync(Serializable id) {
        return diemCongDAO.deleteByIdAsync(id);
    }

    public CompletableFuture<Void> saveOrUpdateAsync(DiemCong entity) {
        return CompletableFuture.runAsync(() -> {
            if (entity.getId() == null) {
                diemCongDAO.save(entity);
            } else {
                diemCongDAO.update(entity);
            }
        });
    }

    public CompletableFuture<DiemCong> findByIdAsync(Serializable id) {
        return diemCongDAO.findByIdAsync(id);
    }
}
