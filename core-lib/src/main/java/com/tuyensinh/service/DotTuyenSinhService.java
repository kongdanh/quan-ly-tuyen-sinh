package com.tuyensinh.service;

import com.tuyensinh.dao.DotTuyenSinhDAO;
import com.tuyensinh.model.DotTuyenSinh;
import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class DotTuyenSinhService {
    private final DotTuyenSinhDAO dao = new DotTuyenSinhDAO();

    public CompletableFuture<List<DotTuyenSinh>> findPageWithFilters(String keyword, List<String> searchFields, Map<String, Object> filters, int page, int size) {
        return dao.findPageWithFilters(keyword, searchFields, filters, page, size);
    }

    public CompletableFuture<Long> countWithFiltersAsync(String keyword, List<String> searchFields, Map<String, Object> filters) {
        return dao.countWithFiltersAsync(keyword, searchFields, filters);
    }

    public void save(DotTuyenSinh entity) { dao.save(entity); }
    public void update(DotTuyenSinh entity) { dao.update(entity); }
    public DotTuyenSinh findById(Serializable id) { return dao.findById(id); }
    public CompletableFuture<Boolean> deleteByIdAsync(Serializable id) { return dao.deleteByIdAsync(id); }
}