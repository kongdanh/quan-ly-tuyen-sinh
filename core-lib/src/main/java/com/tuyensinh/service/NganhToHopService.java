package com.tuyensinh.service;

import com.tuyensinh.dao.NganhToHopDAO;
import com.tuyensinh.model.NganhToHop;
import com.tuyensinh.util.SystemLogger;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class NganhToHopService {

    private final NganhToHopDAO dao;

    public NganhToHopService() {
        this.dao = new NganhToHopDAO();
    }

    /**
     * Tim kiem va phan trang to hop xet tuyen (ho tro loc theo ten nganh, ma to hop)
     */
    public CompletableFuture<List<NganhToHop>> findPageWithFilters(
            String keyword, 
            List<String> searchFields, 
            Map<String, Object> filters, 
            int page, 
            int pageSize) {
        
        return CompletableFuture.supplyAsync(() -> {
            String tenNganhFilter = (String) filters.getOrDefault("tennganh", "");
            String maToHopFilter = (String) filters.getOrDefault("matohop", "");
            String finalTenNganh = "Tất cả".equals(tenNganhFilter) ? "" : tenNganhFilter;
            String finalMaToHop = "Tất cả".equals(maToHopFilter) ? "" : maToHopFilter;
            return dao.findWithFilters(keyword, finalTenNganh, finalMaToHop, page, pageSize);
        });
    }

    /**
     * Dem tong so ban ghi to hop xet tuyen theo bo loc
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
     * Xoa to hop xet tuyen theo ID
     */
    public CompletableFuture<Boolean> deleteByIdAsync(Integer id) { 
        return CompletableFuture.supplyAsync(() -> {
            if (id == null) return false;
            System.out.println("[NganhToHopService] Xoa to hop xet tuyen ID=" + id);
            dao.deleteById(id);
            SystemLogger.log(null, "System", "Xóa tổ hợp xét tuyển ID=" + id, true);
            return true;
        });
    }

    /**
     * Tim to hop xet tuyen theo ID
     */
    public CompletableFuture<NganhToHop> findByIdAsync(int id) {
        return CompletableFuture.supplyAsync(() -> dao.findById(id));
    }

    /**
     * Lay toan bo danh sach to hop xet tuyen
     */
    public List<NganhToHop> getAll() {
        return dao.findAllFull();
    }

    /**
     * Them moi to hop xet tuyen cho nganh
     */
    public void save(NganhToHop entity) {
        if (entity == null || entity.getNganh() == null || entity.getToHopMon() == null) {
            throw new IllegalArgumentException("Du lieu nganh va to hop khong hop le");
        }
        System.out.println("[NganhToHopService] Them to hop xet tuyen cho nganh: " + entity.getNganh().getManganh());
        dao.save(entity);
        System.out.println("[NganhToHopService] Them to hop thanh cong");
        SystemLogger.log(null, "System", "Thêm tổ hợp xét tuyển cho ngành: " + entity.getNganh().getManganh(), true);
    }

    /**
     * Lay danh sach to hop mon theo ma nganh
     */
    public CompletableFuture<List<NganhToHop>> findByMaNganhAsync(String maNganh) {
        return CompletableFuture.supplyAsync(() -> dao.findByMaNganh(maNganh));
    }
}