package com.tuyensinh.service;

import com.tuyensinh.dao.*;
import com.tuyensinh.model.*;
import com.tuyensinh.util.SystemLogger;

import java.util.*;
import java.util.concurrent.CompletableFuture;

public class NguyenVongService {

    private static final int MAX_NGUYEN_VONG = 3;

    private final NguyenVongDAO  nvDAO       = new NguyenVongDAO();
    private final NganhToHopDAO  ntDAO       = new NganhToHopDAO();
    private final NganhDAO       nganhDAO    = new NganhDAO();
    private final ThiSinhService thiSinhService = new ThiSinhService();
    private final DiemService    diemService = new DiemService();
    private final DotTuyenSinhService dotService = new DotTuyenSinhService();

    /**
     * Phan trang danh sach nguyen vong
     */
    public CompletableFuture<List<NguyenVong>> findPageWithFilters(String kw, List<String> searchFields, Map<String, Object> flt, int pi, int ps) {
        return nvDAO.findPageWithFilters(kw, searchFields, flt, pi, ps);
    }

    /**
     * Them moi hoac chinh sua nguyen vong.
     * Tu dong tim to hop mang lai diem xet cao nhat cho nganh da chon.
     * @param cccd     CCCD thi sinh dang dang nhap
     * @param manganh  Ma nganh muon dang ky
     * @param idnv     null = them moi, co gia tri = sua nguyen vong do
     * @param diem     Diem thi cua thi sinh
     * @return SaveResult: OK, NOT_QUALIFIED, MAX_REACHED, FORBIDDEN
     */
    public SaveResult saveWish(String cccd, String manganh, Integer idnv, DiemThiXetTuyen diem) {
        System.out.println("[NguyenVongService] saveWish: cccd=" + cccd + " nganh=" + manganh + " idnv=" + idnv);
        
        DotTuyenSinh activeDot = dotService.getDotDangMo().orElse(null);
        if (activeDot == null) {
            System.out.println("[NguyenVongService] Khong co dot tuyen sinh dang mo");
            return SaveResult.FORBIDDEN;
        }

        Map<String, Double> scoreMap = diemService.buildScoreMap(diem);
        List<NganhToHop> listChoPhep = ntDAO.findByMaNganh(manganh);

        NganhToHop toHopToiUu = null;
        double diemMax = -1.0;
        for (NganhToHop th : listChoPhep) {
            double d = diemService.tinhDiemXet(scoreMap, th);
            if (d > diemMax) { diemMax = d; toHopToiUu = th; }
        }

        if (toHopToiUu == null) {
            System.out.println("[NguyenVongService] Thi sinh khong du dieu kien xet tuyen nganh: " + manganh);
            return SaveResult.NOT_QUALIFIED;
        }

        Nganh  nganh = nganhDAO.findByMaNganh(manganh).orElse(null);
        String ttThm = toHopToiUu.getThMon1() + "-"
                     + toHopToiUu.getThMon2() + "-"
                     + toHopToiUu.getThMon3();

        SaveResult result;
        if (idnv != null) {
            result = updateWish(cccd, idnv, nganh, manganh, diemMax, ttThm, activeDot);
        } else {
            result = insertWish(cccd, nganh, manganh, diemMax, ttThm, diem, activeDot);
        }
        
        if (result == SaveResult.OK) {
            String action = idnv != null ? "Cap nhat" : "Dang ky";
            System.out.println("[NguyenVongService] " + action + " nguyen vong thanh cong: " + manganh);
            SystemLogger.log(null, cccd, action + " nguyện vọng ngành: " + manganh, true);
        }
        
        return result;
    }

    /**
     * Xoa nguyen vong va tu dong sap xep lai thu tu con lai.
     * Kiem tra quyen so huu: chi xoa duoc NV cua chinh minh.
     */
    public boolean deleteWish(int idnv, String cccd) {
        System.out.println("[NguyenVongService] Xoa nguyen vong ID=" + idnv + " cccd=" + cccd);
        NguyenVong nv = nvDAO.findById(idnv);
        if (nv == null || !nv.getThiSinh().getCccd().equals(cccd)) {
            System.out.println("[NguyenVongService] Khong co quyen xoa nguyen vong ID=" + idnv);
            return false;
        }
        nvDAO.delete(nv);
        reorder(cccd);
        System.out.println("[NguyenVongService] Xoa nguyen vong thanh cong ID=" + idnv);
        SystemLogger.log(null, cccd, "Xóa nguyện vọng ID=" + idnv, true);
        return true;
    }

    /**
     * Cap nhat thu tu nguyen vong sau khi keo tha.
     * Kiem tra quyen: chi cho reorder cac ID thuoc ve cccd.
     */
    public void reorderByIds(String[] ids, String cccd) {
        System.out.println("[NguyenVongService] Sap xep lai thu tu nguyen vong cho cccd=" + cccd);
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
        System.out.println("[NguyenVongService] Sap xep lai thanh cong");
        SystemLogger.log(null, cccd, "Sắp xếp lại thứ tự nguyện vọng", true);
    }

    /**
     * Sua nguyen vong da co (kiem tra quyen so huu)
     */
    private SaveResult updateWish(String cccd, int idnv, Nganh nganh,
                                  String manganh, double diemMax, String ttThm, DotTuyenSinh activeDot) {
        NguyenVong nv = nvDAO.findById(idnv);
        if (nv == null || !nv.getThiSinh().getCccd().equals(cccd)) return SaveResult.FORBIDDEN;

        nv.setNganh(nganh);
        nv.setDiemXettuyen(Double.valueOf(diemMax));
        nv.setTtThm(ttThm);
        nv.setDotTuyenSinh(activeDot);
        nv.setNvKeys(buildKey(cccd, manganh, nv.getNvTt()));
        nvDAO.update(nv);
        return SaveResult.OK;
    }

    /**
     * Them moi nguyen vong (kiem tra so luong toi da)
     */
    private SaveResult insertWish(String cccd, Nganh nganh, String manganh,
                                  double diemMax, String ttThm, DiemThiXetTuyen diem, DotTuyenSinh activeDot) {
        List<NguyenVong> current = nvDAO.findByCccd(cccd);
        if (current.size() >= MAX_NGUYEN_VONG) return SaveResult.MAX_REACHED;

        int thuTu = current.size() + 1;
        ThiSinh ts = thiSinhService.findByCccd(cccd).orElse(null);

        NguyenVong nv = new NguyenVong();
        nv.setThiSinh(ts);
        nv.setNganh(nganh);
        nv.setNvTt(thuTu);
        nv.setDiemXettuyen(Double.valueOf(diemMax));
        nv.setTtThm(ttThm);
        nv.setNvKetqua("CHO");
        nv.setTtPhuongthuc(diem.getDPhuongthuc());
        nv.setDotTuyenSinh(activeDot);
        nv.setNvKeys(buildKey(cccd, manganh, thuTu));
        nvDAO.save(nv);
        return SaveResult.OK;
    }

    /**
     * Sap xep lai so thu tu sau khi xoa
     */
    private void reorder(String cccd) {
        List<NguyenVong> list = nvDAO.findByCccd(cccd);
        for (int i = 0; i < list.size(); i++) {
            NguyenVong nv = list.get(i);
            nv.setNvTt(i + 1);
            nv.setNvKeys(buildKey(cccd, nv.getNganh().getManganh(), i + 1));
            nvDAO.update(nv);
        }
    }

    /**
     * Tao key duy nhat cho nguyen vong (cccd_manganh_thutu)
     */
    private String buildKey(String cccd, String manganh, int thuTu) {
        return cccd + "_" + manganh + "_" + thuTu;
    }

    public java.util.List<com.tuyensinh.model.NguyenVong> findByCccd(String cccd) {
        if (cccd == null || cccd.isBlank()) return java.util.Collections.emptyList();
        return nvDAO.findByCccd(cccd);
    }

    public java.util.List<com.tuyensinh.model.NguyenVong> findByDotTuyenSinh(Integer idDot) {
        if (idDot == null) return java.util.Collections.emptyList();
        return nvDAO.findByDotTuyenSinh(idDot);
    }

    public enum SaveResult {
        OK,
        NOT_QUALIFIED,
        MAX_REACHED,
        FORBIDDEN
    }
}