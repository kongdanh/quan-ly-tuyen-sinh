// core-lib/src/main/java/com/tuyensinh/service/NguyenVongService.java
package com.tuyensinh.service;

import com.tuyensinh.dao.*;
import com.tuyensinh.model.*;

import java.math.BigDecimal;
import java.util.*;

public class NguyenVongService {

    private static final int MAX_NGUYEN_VONG = 3;

    private final NguyenVongDAO  nvDAO       = new NguyenVongDAO();
    private final NganhToHopDAO  ntDAO       = new NganhToHopDAO();
    private final NganhDAO       nganhDAO    = new NganhDAO();
    private final ThiSinhService thiSinhService = new ThiSinhService();
    private final DiemService    diemService = new DiemService();

    // PUBLIC API
    /**
     * Thêm mới hoặc chỉnh sửa nguyện vọng.
     * Tự động tìm tổ hợp mang lại điểm xét cao nhất cho ngành đã chọn.
     *
     * @param cccd     CCCD thí sinh đang đăng nhập
     * @param manganh  Mã ngành muốn đăng ký
     * @param idnv     null = thêm mới, có giá trị = sửa nguyện vọng đó
     * @param diem     Điểm thi của thí sinh
     * @return SaveResult: OK, NOT_QUALIFIED, MAX_REACHED, FORBIDDEN
     */
    public SaveResult saveWish(String cccd, String manganh, Integer idnv, DiemThiXetTuyen diem) {        // 1. Tìm tổ hợp tối ưu cho ngành đã chọn
        Map<String, Double> scoreMap = diemService.buildScoreMap(diem);
        List<NganhToHop> listChoPhep = ntDAO.findByMaNganh(manganh);

        NganhToHop toHopToiUu = null;
        double diemMax = -1.0;
        for (NganhToHop th : listChoPhep) {
            double d = diemService.tinhDiemXet(scoreMap, th);
            if (d > diemMax) { diemMax = d; toHopToiUu = th; }
        }

        if (toHopToiUu == null) return SaveResult.NOT_QUALIFIED;

        Nganh  nganh = nganhDAO.findByMaNganh(manganh).orElse(null);
        String ttThm = toHopToiUu.getThMon1() + "-"
                     + toHopToiUu.getThMon2() + "-"
                     + toHopToiUu.getThMon3();

        if (idnv != null) {
            return updateWish(cccd, idnv, nganh, manganh, diemMax, ttThm);
        } else {
            return insertWish(cccd, nganh, manganh, diemMax, ttThm, diem);
        }
    }

    /**
     * Xóa nguyện vọng và tự sắp xếp lại thứ tự còn lại.
     * Có kiểm tra quyền sở hữu: chỉ xóa được NV của chính mình.
     *
     * @return true nếu xóa thành công
     */
    public boolean deleteWish(int idnv, String cccd) {
        NguyenVong nv = nvDAO.findById(idnv);
        if (nv == null || !nv.getThiSinh().getCccd().equals(cccd)) return false;
        nvDAO.delete(nv);
        reorder(cccd);
        return true;
    }

    /**
     * Cập nhật lại thứ tự nguyện vọng sau khi kéo thả.
     * Có kiểm tra quyền: chỉ cho reorder các ID thuộc về cccd.
     *
     * @param ids      Mảng ID theo thứ tự mới
     * @param cccd     CCCD thí sinh
     */
    public void reorderByIds(String[] ids, String cccd) {
        // Lấy tập ID hợp lệ của user để tránh bị thao túng
        Set<Integer> validIds = new HashSet<>();
        for (NguyenVong v : nvDAO.findByCccd(cccd)) validIds.add(v.getId());

        for (int i = 0; i < ids.length; i++) {
            try {
                int id = Integer.parseInt(ids[i].trim());
                if (!validIds.contains(id)) continue;
                NguyenVong nv = nvDAO.findById(id);
                if (nv == null) continue;
                nv.setNvTt(i + 1);
                nv.setNvKeys(buildKey(cccd, nv.getNganh().getManganh(), i + 1));
                nvDAO.update(nv);
            } catch (NumberFormatException ignored) {}
        }
    }

    // PRIVATE HELPERS

    /** Sửa nguyện vọng đã có */
    private SaveResult updateWish(String cccd, int idnv, Nganh nganh,
                                  String manganh, double diemMax, String ttThm) {
        NguyenVong nv = nvDAO.findById(idnv);
        // Kiểm tra quyền sở hữu
        if (nv == null || !nv.getThiSinh().getCccd().equals(cccd)) return SaveResult.FORBIDDEN;

        nv.setNganh(nganh);
        nv.setDiemXettuyen(BigDecimal.valueOf(diemMax));
        nv.setTtThm(ttThm);
        nv.setNvKeys(buildKey(cccd, manganh, nv.getNvTt()));
        nvDAO.update(nv);
        return SaveResult.OK;
    }

    /** Thêm mới nguyện vọng */
    private SaveResult insertWish(String cccd, Nganh nganh, String manganh,
                                  double diemMax, String ttThm, DiemThiXetTuyen diem) {
        List<NguyenVong> current = nvDAO.findByCccd(cccd);
        if (current.size() >= MAX_NGUYEN_VONG) return SaveResult.MAX_REACHED;

        int thuTu = current.size() + 1;
        ThiSinh ts = thiSinhService.findByCccd(cccd).orElse(null);

        NguyenVong nv = new NguyenVong();
        nv.setThiSinh(ts);
        nv.setNganh(nganh);
        nv.setNvTt(thuTu);
        nv.setDiemXettuyen(BigDecimal.valueOf(diemMax));
        nv.setTtThm(ttThm);
        nv.setNvKetqua("CHO");
        nv.setTtPhuongthuc(diem.getDPhuongthuc());
        nv.setNvKeys(buildKey(cccd, manganh, thuTu));
        nvDAO.save(nv);
        return SaveResult.OK;
    }

    /** Sắp xếp lại số thứ tự sau khi xóa */
    private void reorder(String cccd) {
        List<NguyenVong> list = nvDAO.findByCccd(cccd);
        for (int i = 0; i < list.size(); i++) {
            NguyenVong nv = list.get(i);
            nv.setNvTt(i + 1);
            nv.setNvKeys(buildKey(cccd, nv.getNganh().getManganh(), i + 1));
            nvDAO.update(nv);
        }
    }

    /** Tạo key duy nhất cho nguyện vọng */
    private String buildKey(String cccd, String manganh, int thuTu) {
        return cccd + "_" + manganh + "_" + thuTu;
    }

    public java.util.List<com.tuyensinh.model.NguyenVong> findByCccd(String cccd) {
        if (cccd == null || cccd.isBlank()) return java.util.Collections.emptyList();
        return new NguyenVongDAO().findByCccd(cccd);
    }

    // ENUM KẾT QUẢ
    public enum SaveResult {
        OK,
        NOT_QUALIFIED,
        MAX_REACHED,
        FORBIDDEN
    }
}