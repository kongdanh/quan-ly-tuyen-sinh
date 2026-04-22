package com.tuyensinh.service;

import com.tuyensinh.dao.NganhDAO;
import com.tuyensinh.dto.NganhDTO;
import com.tuyensinh.mapper.NganhMapper;
import com.tuyensinh.model.Nganh;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public class NganhService {

    private final NganhDAO nganhDAO = new NganhDAO();

    // ================================================================
    // READ
    // ================================================================

    public List<Nganh> getAll()                        { return nganhDAO.getAll(); }
    public List<Nganh> getAllForComboBox()              { return nganhDAO.getAllForComboBox(); }
    public CompletableFuture<Nganh> findByIdAsync(Serializable id) { return nganhDAO.findByIdAsync(id); }
    public Optional<Nganh> findByMaNganh(String maNganh) { return nganhDAO.findByMaNganh(maNganh); }

    /**
     * Sử dụng Try-Catch để bảo vệ UI khỏi lỗi sập DB.
     * @return List<Nganh> (Trả về list rỗng nếu có lỗi)
     */
    public List<Nganh> findAllSync() {
        try {
            return nganhDAO.findAllSync();
        } catch (Exception e) {
            System.err.println("[NganhService] Lỗi lấy danh sách ngành: " + e.getMessage());
            return java.util.Collections.emptyList();
        }
    }

    // ================================================================
    // SEARCH + PAGINATION
    // ================================================================

    public CompletableFuture<List<Nganh>> findPageWithFilters(
            String keyword, List<String> searchFields,
            Map<String, Object> filters, int pageIndex, int pageSize) {
        return nganhDAO.findPageWithFilters(keyword, searchFields,
                        toHibernateFilters(filters), pageIndex, pageSize)
                .thenApply(list -> applyPostFilters(list, filters));
    }

    public CompletableFuture<Long> countWithFiltersAsync(
            String keyword, List<String> searchFields, Map<String, Object> filters) {
        return nganhDAO.countWithFiltersAsync(keyword, searchFields,
                toHibernateFilters(filters));
    }

    // ================================================================
    // CREATE / UPDATE
    // ================================================================

    public void save(Nganh nganh) {
        if (nganhDAO.existsByMaNganh(nganh.getManganh())) {
            throw new IllegalArgumentException(
                    "Mã ngành \"" + nganh.getManganh() + "\" đã tồn tại trong hệ thống.");
        }
        nganhDAO.save(nganh);
    }

    public void update(Nganh nganh) { nganhDAO.update(nganh); }

    // ================================================================
    // DELETE
    // ================================================================

    /** Xóa đơn giản - gọi khi checkDeleteDependencies trả về danh sách rỗng */
    public CompletableFuture<Boolean> deleteByIdAsync(Serializable id) {
        return nganhDAO.deleteByIdAsync(id);
    }

    /**
     * Kiểm tra các bảng con còn dữ liệu trước khi xóa.
     * Trả về danh sách mô tả; rỗng = an toàn xóa trực tiếp.
     * VD: ["Tổ hợp xét tuyển: 3 bản ghi", "Nguyện vọng đăng ký: 12 bản ghi"]
     */
    public CompletableFuture<List<String>> checkDeleteDependencies(int idNganh, String maNganh) {
        return CompletableFuture.supplyAsync(() ->
                nganhDAO.checkDependencies(idNganh, maNganh)
        );
    }

    /**
     * Xóa ngành VÀ toàn bộ dữ liệu con (cascade).
     * Chỉ gọi sau khi user đã xác nhận đồng ý xóa hết.
     */
    public CompletableFuture<Boolean> deleteCascadeAsync(int idNganh, String maNganh) {
        return nganhDAO.deleteCascadeAsync(idNganh, maNganh);
    }

    // ================================================================
    // BATCH IMPORT
    // ================================================================

    public CompletableFuture<Void> saveOrUpdateAll(List<Nganh> nganhList) {
        return CompletableFuture.runAsync(() -> {
            for (Nganh n : nganhList) {
                if (nganhDAO.existsByMaNganh(n.getManganh())) {
                    nganhDAO.findByMaNganh(n.getManganh()).ifPresent(existing -> {
                        n.setId(existing.getId());
                        nganhDAO.update(n);
                    });
                } else {
                    nganhDAO.save(n);
                }
            }
        });
    }

    // ================================================================
    // STATISTICS
    // ================================================================

    public List<Object[]> getThongKeDangKy() { return nganhDAO.getThongKeDangKy(); }

    // ================================================================
    // DTO CONVERSION
    // ================================================================

    public NganhDTO toDTO(Nganh nganh)     { return NganhMapper.toDTO(nganh); }
    public Nganh    toEntity(NganhDTO dto) { return NganhMapper.toEntity(dto); }

    // ================================================================
    // PRIVATE HELPERS
    // ================================================================

    private Map<String, Object> toHibernateFilters(Map<String, Object> uiFilters) {
        Map<String, Object> hqlFilters = new java.util.HashMap<>();
        if (uiFilters == null) return hqlFilters;
        for (Map.Entry<String, Object> e : uiFilters.entrySet()) {
            String key = e.getKey();
            if (key.equals("manganh") || key.equals("tennganh")
                    || key.equals("nTuyenthang") || key.equals("nDgnl")
                    || key.equals("nThpt") || key.equals("nVsat")) {
                hqlFilters.put(key, e.getValue());
            }
        }
        return hqlFilters;
    }

    private List<Nganh> applyPostFilters(List<Nganh> list, Map<String, Object> filters) {
        if (filters == null || filters.isEmpty()) return list;

        String fKhoa  = (String)  filters.getOrDefault("khoa",      null);
        String fTT    = (String)  filters.getOrDefault("trangThai", null);
        Integer ctMin = toInt(filters.get("nChitieuMin"));
        Integer ctMax = toInt(filters.get("nChitieuMax"));
        Integer ptMin = toInt(filters.get("phanTramMin"));
        Integer ptMax = toInt(filters.get("phanTramMax"));

        boolean filterKhoa  = fKhoa != null && !fKhoa.equals("Tất cả");
        boolean filterTT    = fTT   != null && !fTT.equals("Tất cả");
        boolean filterCtMin = ctMin != null;
        boolean filterCtMax = ctMax != null;
        boolean filterPtMin = ptMin != null;
        boolean filterPtMax = ptMax != null;

        if (!filterKhoa && !filterTT && !filterCtMin && !filterCtMax
                && !filterPtMin && !filterPtMax) return list;

        List<Nganh> result = new java.util.ArrayList<>();
        for (Nganh n : list) {
            if (filterKhoa && !resolveKhoa(n.getManganh()).equals(fKhoa)) continue;

            int chiTieu  = n.getNChitieu() != null ? n.getNChitieu() : 0;
            int dangKy   = tinhTongDangKy(n);
            int phanTram = chiTieu == 0 ? 0 : (int) Math.round(dangKy * 100.0 / chiTieu);
            String tt    = resolveTrangThai(n, phanTram);

            if (filterTT    && !tt.equals(fTT))   continue;
            if (filterCtMin && chiTieu < ctMin)    continue;
            if (filterCtMax && chiTieu > ctMax)    continue;
            if (filterPtMin && phanTram < ptMin)   continue;
            if (filterPtMax && phanTram > ptMax)   continue;

            result.add(n);
        }
        return result;
    }

    private int tinhTongDangKy(Nganh n) {
        int total = 0;
        if (n.getSlXtt()  != null) total += n.getSlXtt();
        if (n.getSlDgnl() != null) total += n.getSlDgnl();
        if (n.getSlVsat() != null) total += n.getSlVsat();
        if (n.getSlThpt() != null) {
            try { total += Integer.parseInt(n.getSlThpt()); } catch (NumberFormatException ignored) {}
        }
        return total;
    }

    private String resolveKhoa(String maNganh) {
        if (maNganh == null) return "—";
        String m = maNganh.toUpperCase();
        if (m.startsWith("CNTT") || m.startsWith("CNPM") || m.startsWith("KTMT")) return "Công nghệ";
        if (m.startsWith("KTOAN") || m.startsWith("TAICHINH")
                || m.startsWith("QTKD") || m.startsWith("MARKETING")) return "Kinh tế";
        if (m.startsWith("NNANH") || m.startsWith("NNTRUNG")) return "Ngoại ngữ";
        return "—";
    }

    private String resolveTrangThai(Nganh n, int phanTram) {
        if ("0".equals(n.getNTuyenthang()) && "0".equals(n.getNDgnl())
                && "0".equals(n.getNThpt()) && "0".equals(n.getNVsat()))
            return "Tạm dừng";
        return phanTram >= 100 ? "Đã đủ chỉ tiêu" : "Đang tuyển";
    }

    private Integer toInt(Object val) {
        if (val == null) return null;
        if (val instanceof Integer) return (Integer) val;
        try { return Integer.parseInt(val.toString()); }
        catch (NumberFormatException e) { return null; }
    }
}