package com.tuyensinh.service;

import com.tuyensinh.dao.NganhDAO;
import com.tuyensinh.dto.NganhDTO;
import com.tuyensinh.mapper.NganhMapper;
import com.tuyensinh.model.Nganh;
import com.tuyensinh.util.HibernateUtil;
import com.tuyensinh.util.SystemLogger;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import org.hibernate.Session;

public class NganhService {

    private final NganhDAO nganhDAO = new NganhDAO();

    /**
     * Lay toan bo danh sach nganh
     */
    public List<Nganh> getAll() { return nganhDAO.getAll(); }

    /**
     * Lay danh sach nganh cho ComboBox (chi lay truong can thiet)
     */
    public List<Nganh> getAllForComboBox() { return nganhDAO.getAllForComboBox(); }

    public CompletableFuture<Nganh> findByIdAsync(Serializable id) { return nganhDAO.findByIdAsync(id); }

    public Optional<Nganh> findByMaNganh(String maNganh) { return nganhDAO.findByMaNganh(maNganh); }
    /**
     * Lấy số lượng đăng ký thực tế từ bảng nguyện vọng.
     * Trả về Map<maNganh, count> — gọi 1 lần cho toàn bộ danh sách.
     */
    public CompletableFuture<Map<String, Long>> fetchDangKyCountMap() {
        return CompletableFuture.supplyAsync(() -> nganhDAO.countDangKyByMaNganh());
    }
    /**
     * Lay danh sach nganh dong bo, bao ve UI khoi loi DB
     */
    public List<Nganh> findAllSync() {
        try {
            return nganhDAO.findAllSync();
        } catch (Exception e) {
            System.err.println("[NganhService] Loi lay danh sach nganh: " + e.getMessage());
            return java.util.Collections.emptyList();
        }
    }

    /**
     * Tim kiem va phan trang danh sach nganh
     */
    public CompletableFuture<List<Nganh>> findPageWithFilters(
            String keyword, List<String> searchFields,
            Map<String, Object> filters, int pageIndex, int pageSize) {
        return nganhDAO.findPageWithFilters(keyword, searchFields,
                        toHibernateFilters(filters), pageIndex, pageSize)
                .thenApply(list -> applyPostFilters(list, filters));
    }

    /**
     * Dem tong so ban ghi nganh theo bo loc
     */
    public CompletableFuture<Long> countWithFiltersAsync(
            String keyword, List<String> searchFields, Map<String, Object> filters) {
        return nganhDAO.countWithFiltersAsync(keyword, searchFields,
                toHibernateFilters(filters));
    }

    /**
     * Them nganh moi (kiem tra trung ma nganh truoc khi luu)
     */
    public void save(Nganh nganh) {
        System.out.println("[NganhService] Them nganh moi: " + nganh.getManganh() + " - " + nganh.getTennganh());
        if (nganhDAO.existsByMaNganh(nganh.getManganh())) {
            throw new IllegalArgumentException(
                    "Ma nganh \"" + nganh.getManganh() + "\" da ton tai trong he thong.");
        }
        nganhDAO.save(nganh);
        System.out.println("[NganhService] Them nganh thanh cong: " + nganh.getManganh());
        SystemLogger.log(null, "System", "Thêm ngành: " + nganh.getManganh() + " - " + nganh.getTennganh(), true);
    }

    /**
     * Cap nhat thong tin nganh
     */
    public void update(Nganh nganh) { 
        System.out.println("[NganhService] Cap nhat nganh: " + nganh.getManganh());
        nganhDAO.update(nganh); 
        System.out.println("[NganhService] Cap nhat nganh thanh cong: " + nganh.getManganh());
        SystemLogger.log(null, "System", "Cập nhật ngành: " + nganh.getManganh() + " - " + nganh.getTennganh(), true);
    }

    /**
     * Xoa nganh don gian (chi goi khi khong con du lieu phu thuoc)
     */
    public CompletableFuture<Boolean> deleteByIdAsync(Serializable id) {
        System.out.println("[NganhService] Xoa nganh ID=" + id);
        SystemLogger.log(null, "System", "Xóa ngành ID=" + id, true);
        return nganhDAO.deleteByIdAsync(id);
    }

    /**
     * Kiem tra cac bang con phu thuoc truoc khi xoa.
     * Tra ve danh sach mo ta; rong = an toan xoa truc tiep.
     */
    public CompletableFuture<List<String>> checkDeleteDependencies(int idNganh, String maNganh) {
        return CompletableFuture.supplyAsync(() ->
                nganhDAO.checkDependencies(idNganh, maNganh)
        );
    }

    /**
     * Xoa nganh va toan bo du lieu con (cascade).
     * Chi goi sau khi user xac nhan dong y xoa het.
     */
    public CompletableFuture<Boolean> deleteCascadeAsync(int idNganh, String maNganh) {
        System.out.println("[NganhService] Xoa cascade nganh ID=" + idNganh + " ma=" + maNganh);
        SystemLogger.log(null, "System", "Xóa cascade ngành: " + maNganh + " (ID=" + idNganh + ")", true);
        return nganhDAO.deleteCascadeAsync(idNganh, maNganh);
    }

    /**
     * Import hang loat nganh tu file Excel (them moi hoac cap nhat neu da ton tai)
     */
    public CompletableFuture<Void> saveOrUpdateAll(List<Nganh> nganhList) {
        System.out.println("[NganhService] Import batch " + nganhList.size() + " nganh");
        return CompletableFuture.runAsync(() -> {
            int created = 0, updated = 0;
            for (Nganh n : nganhList) {
                if (nganhDAO.existsByMaNganh(n.getManganh())) {
                    nganhDAO.findByMaNganh(n.getManganh()).ifPresent(existing -> {
                        n.setId(existing.getId());
                        nganhDAO.update(n);
                    });
                    updated++;
                } else {
                    nganhDAO.save(n);
                    created++;
                }
            }
            System.out.println("[NganhService] Import batch hoan thanh: " + created + " tao moi, " + updated + " cap nhat");
            SystemLogger.log(null, "System", "Import batch ngành: " + created + " tạo mới, " + updated + " cập nhật", true);
        });
    }

    /**
     * Thong ke so luong dang ky theo nganh
     */
    public List<Object[]> getThongKeDangKy() { return nganhDAO.getThongKeDangKy(); }

    public NganhDTO toDTO(Nganh nganh)     { return NganhMapper.toDTO(nganh); }
    public Nganh    toEntity(NganhDTO dto) { return NganhMapper.toEntity(dto); }

    /**
     * Chuyen doi filter UI sang filter HQL
     */
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

    /**
     * Loc bo sung phia service (khoa, trang thai, chi tieu, phan tram)
     */
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
    /**
     * Lấy số lượng đăng ký thực tế từ bảng nguyện vọng.
     * Trả về Map<maNganh, count> — gọi 1 lần cho toàn bộ danh sách.
     */

    /**
     * Tinh tong so luong dang ky tu tat ca phuong thuc
     */
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

    /**
     * Xac dinh khoa dua tren tien to ma nganh
     */
    private String resolveKhoa(String maNganh) {
        if (maNganh == null) return "—";
        String m = maNganh.toUpperCase();
        if (m.startsWith("CNTT") || m.startsWith("CNPM") || m.startsWith("KTMT")) return "Công nghệ";
        if (m.startsWith("KTOAN") || m.startsWith("TAICHINH")
                || m.startsWith("QTKD") || m.startsWith("MARKETING")) return "Kinh tế";
        if (m.startsWith("NNANH") || m.startsWith("NNTRUNG")) return "Ngoại ngữ";
        return "—";
    }

    /**
     * Xac dinh trang thai tuyen sinh cua nganh
     */
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

    /**
     * Kiem tra nganh con chi tieu hay khong (dem so trung tuyen so voi chi tieu)
     */
    public boolean conChiTieu(Nganh nganh) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Long daTrungTuyen = session.createQuery(
                "SELECT count(k) FROM KetQuaXetTuyen k WHERE k.nganh.id = :idNganh", Long.class)
                .setParameter("idNganh", nganh.getId())
                .uniqueResult();
            return daTrungTuyen < nganh.getNChitieu();
        } catch (Exception e) {
            System.err.println("[NganhService] Loi kiem tra chi tieu nganh ID=" + nganh.getId() + ": " + e.getMessage());
            return false;
        }
    }

    /**
     * Giam chi tieu - khong can xu ly truc tiep vi dem dong qua bang KetQua
     */
    public void giamChiTieu(Nganh nganh) {
    }

}