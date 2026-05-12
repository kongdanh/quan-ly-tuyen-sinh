package com.tuyensinh.service;

import com.tuyensinh.dao.ThiSinhDAO;
import com.tuyensinh.model.ThiSinh;
import com.tuyensinh.util.SystemLogger;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public class ThiSinhService {

    private final ThiSinhDAO thiSinhDAO = new ThiSinhDAO();

    /**
     * Tim thi sinh theo CCCD
     */
    public Optional<ThiSinh> findByCccd(String cccd) {
        return thiSinhDAO.findByCccd(cccd);
    }

    /**
     * Tim kiem va phan trang danh sach thi sinh
     */
    public List<ThiSinh> getSearchAndPaging(String keyword, int page, int pageSize) {
        return thiSinhDAO.getSearchAndPaging(keyword, page, pageSize);
    }

    /**
     * Dem so luong thi sinh theo tu khoa tim kiem
     */
    public long countSearch(String keyword) {
        return thiSinhDAO.countSearch(keyword);
    }

    /**
     * Xoa thi sinh theo ID
     */
    public boolean deleteById(int id) {
        System.out.println("[ThiSinhService] Xoa thi sinh ID=" + id);
        boolean result = thiSinhDAO.deleteById(id);
        if (result) {
            System.out.println("[ThiSinhService] Xoa thi sinh thanh cong ID=" + id);
            SystemLogger.log(null, "System", "Xóa thí sinh ID=" + id, true);
        } else {
            System.out.println("[ThiSinhService] Xoa thi sinh that bai ID=" + id);
            SystemLogger.log(null, "System", "Xóa thí sinh thất bại ID=" + id, false);
        }
        return result;
    }

    /**
     * Them thi sinh moi
     */
    public void save(ThiSinh entity) {
        System.out.println("[ThiSinhService] Them thi sinh: " + entity.getHo() + " " + entity.getTen());
        thiSinhDAO.save(entity);
        System.out.println("[ThiSinhService] Them thi sinh thanh cong, CCCD=" + entity.getCccd());
        SystemLogger.log(null, "System", "Thêm thí sinh: " + entity.getHo() + " " + entity.getTen() + " (CCCD: " + entity.getCccd() + ")", true);
    }

    /**
     * Cap nhat thong tin thi sinh
     */
    public void update(ThiSinh entity) {
        System.out.println("[ThiSinhService] Cap nhat thi sinh CCCD=" + entity.getCccd());
        thiSinhDAO.update(entity);
        System.out.println("[ThiSinhService] Cap nhat thi sinh thanh cong");
        SystemLogger.log(null, "System", "Cập nhật thí sinh: " + entity.getHo() + " " + entity.getTen() + " (CCCD: " + entity.getCccd() + ")", true);
    }

    public CompletableFuture<ThiSinh> findByIdAsync(Serializable id) {
        return thiSinhDAO.findByIdAsync(id);
    }

    /**
     * Xoa thi sinh theo ID (async)
     */
    public CompletableFuture<Boolean> deleteByIdAsync(Serializable id) {
        System.out.println("[ThiSinhService] Xoa thi sinh async ID=" + id);
        SystemLogger.log(null, "System", "Xóa thí sinh ID=" + id, true);
        return thiSinhDAO.deleteByIdAsync(id);
    }

    /**
     * Phan trang danh sach thi sinh voi bo loc
     */
    public CompletableFuture<List<ThiSinh>> findPageWithFilters(
            String keyword,
            List<String> searchFields,
            Map<String, Object> filters,
            int pageIndex,
            int pageSize) {
        return thiSinhDAO.findPageWithFilters(keyword, searchFields, filters, pageIndex, pageSize);
    }

    /**
     * Dem tong so ban ghi thi sinh theo bo loc
     */
    public CompletableFuture<Long> countWithFiltersAsync(
            String keyword,
            List<String> searchFields,
            Map<String, Object> filters) {
        return thiSinhDAO.countWithFiltersAsync(keyword, searchFields, filters);
    }
}
