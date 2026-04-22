package com.tuyensinh.service;

import com.tuyensinh.dao.HoSoTuyenSinhDAO;
import com.tuyensinh.model.HoSoTuyenSinh;
import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class HoSoTuyenSinhService {
    private final HoSoTuyenSinhDAO dao = new HoSoTuyenSinhDAO();

    public CompletableFuture<List<HoSoTuyenSinh>> findPageWithFilters(String keyword, List<String> searchFields, Map<String, Object> filters, int page, int size) {
        return dao.findPageWithFilters(keyword, searchFields, filters, page, size);
    }

    public CompletableFuture<Long> countWithFiltersAsync(String keyword, List<String> searchFields, Map<String, Object> filters) {
        return dao.countWithFiltersAsync(keyword, searchFields, filters);
    }

    public void save(HoSoTuyenSinh entity) { dao.save(entity); }
    public void update(HoSoTuyenSinh entity) { dao.update(entity); }
    public HoSoTuyenSinh findById(Serializable id) { return dao.findById(id); }
    public CompletableFuture<Boolean> deleteByIdAsync(Serializable id) { return dao.deleteByIdAsync(id); }
}