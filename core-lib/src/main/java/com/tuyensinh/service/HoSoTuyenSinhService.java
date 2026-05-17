package com.tuyensinh.service;

import com.tuyensinh.dao.HoSoTuyenSinhDAO;
import com.tuyensinh.model.HoSoTuyenSinh;
import com.tuyensinh.util.SystemLogger;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class HoSoTuyenSinhService {
    private final HoSoTuyenSinhDAO dao = new HoSoTuyenSinhDAO();
    private final DiemService diemService = new DiemService();

    public HoSoTuyenSinh findById(Serializable id) { return dao.findById(id); }
    
    /**
     * Luu ho so tuyen sinh moi
     */
    public void save(HoSoTuyenSinh entity) { 
        System.out.println("[HoSoTuyenSinhService] Luu ho so moi: " + entity.getMaHoSo());
        dao.save(entity); 
        SystemLogger.log(null, "System", "Tạo hồ sơ tuyển sinh: " + entity.getMaHoSo(), true);
    }
    
    /**
     * Cap nhat ho so. Neu trang thai HOP_LE thi tu dong tao bang diem rong.
     */
    public void update(HoSoTuyenSinh entity) { 
        System.out.println("[HoSoTuyenSinhService] Cap nhat ho so: " + entity.getMaHoSo() + " trang thai: " + entity.getTrangThai());
        dao.update(entity); 

        if ("HOP_LE".equals(entity.getTrangThai())) {
            System.out.println("[HoSoTuyenSinhService] Ho so HOP_LE, tao bang diem rong");
            diemService.taoDiemRong(entity.getThiSinh());
        }

        SystemLogger.log(null, "System", "Cập nhật hồ sơ: " + entity.getMaHoSo() + " (Trạng thái: " + entity.getTrangThai() + ")", true);
    }
    
    /**
     * Xoa ho so tuyen sinh theo ID
     */
    public CompletableFuture<Boolean> deleteByIdAsync(Serializable id) { 
        System.out.println("[HoSoTuyenSinhService] Xoa ho so ID=" + id);
        SystemLogger.log(null, "System", "Xóa hồ sơ tuyển sinh ID=" + id, true);
        return dao.deleteByIdAsync(id); 
    }
    
    /**
     * Phan trang danh sach ho so tuyen sinh
     */
    public CompletableFuture<List<HoSoTuyenSinh>> findPageWithFilters(String keyword, List<String> searchFields, Map<String, Object> filters, int pageIndex, int pageSize) {
        return dao.findPageWithFilters(keyword, searchFields, filters, pageIndex, pageSize);
    }
    
    /**
     * Dem tong so ban ghi ho so theo bo loc
     */
    public CompletableFuture<Long> countWithFiltersAsync(String keyword, List<String> searchFields, Map<String, Object> filters) {
        return dao.countWithFiltersAsync(keyword, searchFields, filters);
    }
}