package com.tuyensinh.service;

import com.tuyensinh.dao.NganhToHopDAO;
import com.tuyensinh.model.NganhToHop;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class NganhToHopService {

    private final NganhToHopDAO dao;

    public NganhToHopService() {
        this.dao = new NganhToHopDAO();
    }

    /**
     * Tìm kiếm và phân trang Async (Dùng cho BaseTablePanel)
     * Hỗ trợ search theo keyword chung và filter riêng cho Tên ngành, Tổ hợp
     */
    public CompletableFuture<List<NganhToHop>> findPageWithFilters(
            String keyword, 
            List<String> searchFields, 
            Map<String, Object> filters, 
            int page, 
            int pageSize) {
        
        return CompletableFuture.supplyAsync(() -> {
            // Lấy giá trị từ 2 thanh search tùy chỉnh trong setupExtras
            String tenNganhFilter = (String) filters.getOrDefault("tennganh", "");
            String maToHopFilter = (String) filters.getOrDefault("matohop", "");

            // Nếu filters là "Tất cả", ta coi như rỗng để DAO không lọc
            String finalTenNganh = "Tất cả".equals(tenNganhFilter) ? "" : tenNganhFilter;
            String finalMaToHop = "Tất cả".equals(maToHopFilter) ? "" : maToHopFilter;

            return dao.findWithFilters(keyword, finalTenNganh, finalMaToHop, page, pageSize);
        });
    }

    /**
     * Đếm tổng số lượng bản ghi dựa trên bộ lọc (Async)
     */
    public CompletableFuture<Long> countWithFiltersAsync(
            String keyword, 
            List<String> searchFields, 
            Map<String, Object> filters) {
        
        return CompletableFuture.supplyAsync(() -> {
            String tenNganhFilter = (String) filters.getOrDefault("tennganh", "");
            String maToHopFilter = (String) filters.getOrDefault("matohop", "");
            
            String finalTenNganh = "Tất cả".equals(tenNganhFilter) ? "" : tenNganhFilter;
            String finalMaToHop = "Tất cả".equals(maToHopFilter) ? "" : maToHopFilter;

            return dao.countWithFilters(keyword, finalTenNganh, finalMaToHop);
        });
    }

    /**
     * Xóa theo ID (Async)
     */
    public CompletableFuture<Boolean> deleteByIdAsync(Integer id) { 
        return CompletableFuture.supplyAsync(() -> {
            if (id == null) return false;
            dao.deleteById(id);
            return true;
        });
    }

    /**
     * Tìm theo ID (Async) - Dùng cho showEditDialog
     */
    public CompletableFuture<NganhToHop> findByIdAsync(int id) {
        return CompletableFuture.supplyAsync(() -> dao.findById(id));
    }

    // --- Giữ lại các hàm cũ để phục vụ logic nghiệp vụ khác ---

    public List<NganhToHop> getAll() {
        return dao.findAllFull();
    }

    public void save(NganhToHop entity) {
        if (entity == null || entity.getNganh() == null || entity.getToHopMon() == null) {
            throw new IllegalArgumentException("Dữ liệu ngành và tổ hợp không hợp lệ");
        }
        dao.save(entity);
    }
}