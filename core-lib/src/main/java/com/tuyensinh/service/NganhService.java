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
                    "M\u00e3 ng\u00e0nh \"" + nganh.getManganh() + "\" \u0111\u00e3 t\u1ed3n t\u1ea1i trong h\u1ec7 th\u1ed1ng.");
        }
        nganhDAO.save(nganh);
    }

    public void update(Nganh nganh) { nganhDAO.update(nganh); }

    // ================================================================
    // DELETE
    // ================================================================

    /** Xoa don gian - goi khi checkDeleteDependencies tra ve danh sach rong */
    public CompletableFuture<Boolean> deleteByIdAsync(Serializable id) {
        return nganhDAO.deleteByIdAsync(id);
    }

    /**
     * Kiem tra cac bang con con du lieu truoc khi xoa.
     * Tra ve danh sach mo ta; rong = an toan xoa truc tiep.
     * VD: ["To hop xet tuyen: 3 ban ghi", "Nguyen vong dang ky: 12 ban ghi"]
     */
    public CompletableFuture<List<String>> checkDeleteDependencies(int idNganh, String maNganh) {
        return CompletableFuture.supplyAsync(() ->
                nganhDAO.checkDependencies(idNganh, maNganh)
        );
    }

    /**
     * Xoa nganh VA toan bo du lieu con (cascade).
     * Chi goi sau khi user da xac nhan dong y xoa het.
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

        boolean filterKhoa  = fKhoa != null && !fKhoa.equals("T\u1ea5t c\u1ea3");
        boolean filterTT    = fTT   != null && !fTT.equals("T\u1ea5t c\u1ea3");
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
        if (maNganh == null) return "\u2014";
        String m = maNganh.toUpperCase();
        if (m.startsWith("CNTT") || m.startsWith("CNPM") || m.startsWith("KTMT")) return "C\u00f4ng ngh\u1ec7";
        if (m.startsWith("KTOAN") || m.startsWith("TAICHINH")
                || m.startsWith("QTKD") || m.startsWith("MARKETING")) return "Kinh t\u1ebf";
        if (m.startsWith("NNANH") || m.startsWith("NNTRUNG")) return "Ngo\u1ea1i ng\u1eef";
        return "\u2014";
    }

    private String resolveTrangThai(Nganh n, int phanTram) {
        if ("0".equals(n.getNTuyenthang()) && "0".equals(n.getNDgnl())
                && "0".equals(n.getNThpt()) && "0".equals(n.getNVsat()))
            return "T\u1ea1m d\u1eebng";
        return phanTram >= 100 ? "\u0110\u00e3 \u0111\u1ee7 ch\u1ec9 ti\u00eau" : "\u0110ang tuy\u1ec3n";
    }

    private Integer toInt(Object val) {
        if (val == null) return null;
        if (val instanceof Integer) return (Integer) val;
        try { return Integer.parseInt(val.toString()); }
        catch (NumberFormatException e) { return null; }
    }
}