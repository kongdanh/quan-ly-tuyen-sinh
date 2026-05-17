package com.tuyensinh.service;

import com.tuyensinh.dao.*;
import com.tuyensinh.model.*;
import com.tuyensinh.util.HibernateUtil;
import com.tuyensinh.util.SystemLogger;
import org.hibernate.Session;
import org.hibernate.Transaction;

import java.util.*;
import java.util.concurrent.CompletableFuture;

/**
 * Service quản lý nguyện vọng xét tuyển của thí sinh.
 */
public class NguyenVongService {

    private static final int MAX_NGUYEN_VONG = 20; // Giới hạn thực tế thường là 20

    private final NguyenVongDAO nvDAO = new NguyenVongDAO();
    private final NganhToHopDAO ntDAO = new NganhToHopDAO();
    private final NganhDAO nganhDAO = new NganhDAO();
    private final ThiSinhService thiSinhService = new ThiSinhService();
    private final DiemService diemService = new DiemService();
    private final DotTuyenSinhService dotService = new DotTuyenSinhService();

    // ── Queries ─────────────────────────────────────────────────────────────────

    public CompletableFuture<List<NguyenVong>> findPageWithFilters(String kw, List<String> searchFields, Map<String, Object> flt, int pi, int ps) {
        return nvDAO.findPageWithFilters(kw, searchFields, flt, pi, ps);
    }

    public List<NguyenVong> findByCccd(String cccd) {
        if (cccd == null || cccd.isBlank()) return Collections.emptyList();
        return nvDAO.findByCccd(cccd);
    }

    public List<NguyenVong> findByDotTuyenSinh(Integer idDot) {
        if (idDot == null) return Collections.emptyList();
        return nvDAO.findByDotTuyenSinh(idDot);
    }

    // ── Nghiệp vụ Web (Sử dụng KetQuaDangKy) ────────────────────────────────────

    public KetQuaDangKy dangKyNguyenVong(String cccd, String manganh, String matohop, int thuTu, String phuongThuc, String thm) {
        System.out.println("[NguyenVongService] dangKyNguyenVong: cccd=" + cccd + " nganh=" + manganh);
        
        DotTuyenSinh activeDot = dotService.getDotDangMo().orElse(null);
        if (activeDot == null) return KetQuaDangKy.thatBai("Hiện không có đợt tuyển sinh nào đang mở.");

        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Transaction tx = session.beginTransaction();
            
            if (isThuTuDaTonTai(session, cccd, thuTu)) {
                tx.rollback();
                return KetQuaDangKy.thatBai("Thứ tự nguyện vọng " + thuTu + " đã tồn tại.");
            }

            if (countNguyenVong(session, cccd) >= MAX_NGUYEN_VONG) {
                tx.rollback();
                return KetQuaDangKy.thatBai("Bạn đã đăng ký tối đa " + MAX_NGUYEN_VONG + " nguyện vọng.");
            }

            ThiSinh ts = findThiSinh(session, cccd);
            Nganh nganh = findNganh(session, manganh);
            if (ts == null || nganh == null) {
                tx.rollback();
                return KetQuaDangKy.thatBai("Thông tin thí sinh hoặc ngành không hợp lệ.");
            }

            NguyenVong nv = new NguyenVong();
            nv.setThiSinh(ts);
            nv.setNganh(nganh);
            nv.setNvTt(thuTu);
            nv.setTtPhuongthuc(phuongThuc);
            nv.setTtThm(thm);
            nv.setNvKetqua("CHO");
            nv.setDotTuyenSinh(activeDot);
            nv.setNvKeys(buildKey(cccd, manganh, thuTu));

            session.persist(nv);
            tx.commit();
            
            SystemLogger.log(null, cccd, "Đăng ký nguyện vọng thành công: " + manganh, true);
            return KetQuaDangKy.thanhCong("Đăng ký nguyện vọng thành công.");
        } catch (Exception e) {
            e.printStackTrace();
            return KetQuaDangKy.thatBai("Lỗi hệ thống: " + e.getMessage());
        }
    }

    public KetQuaDangKy huyNguyenVong(int nvId, String cccd) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Transaction tx = session.beginTransaction();
            NguyenVong nv = session.get(NguyenVong.class, nvId);
            if (nv == null || !nv.getThiSinh().getCccd().equals(cccd)) {
                tx.rollback();
                return KetQuaDangKy.thatBai("Không có quyền hủy nguyện vọng này.");
            }
            session.remove(nv);
            tx.commit();
            
            reorder(cccd);
            SystemLogger.log(null, cccd, "Hủy nguyện vọng ID=" + nvId, true);
            return KetQuaDangKy.thanhCong("Hủy nguyện vọng thành công.");
        } catch (Exception e) {
            return KetQuaDangKy.thatBai("Lỗi khi hủy: " + e.getMessage());
        }
    }

    // ── Nghiệp vụ Admin/Auto (Sử dụng SaveResult) ───────────────────────────────

    public SaveResult saveWish(String cccd, String manganh, Integer idnv, DiemThiXetTuyen diem) {
        DotTuyenSinh activeDot = dotService.getDotDangMo().orElse(null);
        if (activeDot == null) return SaveResult.FORBIDDEN;

        Map<String, Double> scoreMap = diemService.buildScoreMap(diem);
        List<NganhToHop> listChoPhep = ntDAO.findByMaNganh(manganh);

        NganhToHop toHopToiUu = null;
        double diemMax = -1.0;
        for (NganhToHop th : listChoPhep) {
            double d = diemService.tinhDiemXet(scoreMap, th);
            if (d > diemMax) {
                diemMax = d;
                toHopToiUu = th;
            }
        }

        if (toHopToiUu == null) return SaveResult.NOT_QUALIFIED;

        Nganh nganh = nganhDAO.findByMaNganh(manganh).orElse(null);
        String ttThm = toHopToiUu.getThMon1() + "-" + toHopToiUu.getThMon2() + "-" + toHopToiUu.getThMon3();

        if (idnv != null) {
            return updateWish(cccd, idnv, nganh, manganh, diemMax, ttThm, activeDot);
        } else {
            return insertWish(cccd, nganh, manganh, diemMax, ttThm, diem, activeDot);
        }
    }

    private SaveResult updateWish(String cccd, int idnv, Nganh nganh, String manganh, double diemMax, String ttThm, DotTuyenSinh activeDot) {
        NguyenVong nv = nvDAO.findById(idnv);
        if (nv == null || !nv.getThiSinh().getCccd().equals(cccd)) return SaveResult.FORBIDDEN;
        nv.setNganh(nganh);
        nv.setDiemXettuyen(diemMax);
        nv.setTtThm(ttThm);
        nv.setDotTuyenSinh(activeDot);
        nv.setNvKeys(buildKey(cccd, manganh, nv.getNvTt()));
        nvDAO.update(nv);
        return SaveResult.OK;
    }

    private SaveResult insertWish(String cccd, Nganh nganh, String manganh, double diemMax, String ttThm, DiemThiXetTuyen diem, DotTuyenSinh activeDot) {
        List<NguyenVong> current = nvDAO.findByCccd(cccd);
        if (current.size() >= MAX_NGUYEN_VONG) return SaveResult.MAX_REACHED;

        int thuTu = current.size() + 1;
        ThiSinh ts = thiSinhService.findByCccd(cccd).orElse(null);

        NguyenVong nv = new NguyenVong();
        nv.setThiSinh(ts);
        nv.setNganh(nganh);
        nv.setNvTt(thuTu);
        nv.setDiemXettuyen(diemMax);
        nv.setTtThm(ttThm);
        nv.setNvKetqua("CHO");
        nv.setTtPhuongthuc(diem.getDPhuongthuc());
        nv.setDotTuyenSinh(activeDot);
        nv.setNvKeys(buildKey(cccd, manganh, thuTu));
        nvDAO.save(nv);
        return SaveResult.OK;
    }

    // ── Reorder Logic ───────────────────────────────────────────────────────────

    public void reorderByIds(String[] ids, String cccd) {
        Set<Integer> validIds = new HashSet<>();
        for (NguyenVong v : nvDAO.findByCccd(cccd)) validIds.add(v.getId());

        for (int i = 0; i < ids.length; i++) {
            try {
                int id = Integer.parseInt(ids[i].trim());
                if (!validIds.contains(id)) continue;
                NguyenVong nv = nvDAO.findById(id);
                if (nv != null) {
                    nv.setNvTt(i + 1);
                    nv.setNvKeys(buildKey(cccd, nv.getNganh().getManganh(), i + 1));
                    nvDAO.update(nv);
                }
            } catch (Exception ignored) {}
        }
        SystemLogger.log(null, cccd, "Sắp xếp lại thứ tự nguyện vọng", true);
    }

    private void reorder(String cccd) {
        List<NguyenVong> list = nvDAO.findByCccd(cccd);
        for (int i = 0; i < list.size(); i++) {
            NguyenVong nv = list.get(i);
            nv.setNvTt(i + 1);
            nv.setNvKeys(buildKey(cccd, nv.getNganh().getManganh(), i + 1));
            nvDAO.update(nv);
        }
    }

    private String buildKey(String cccd, String manganh, int thuTu) {
        return cccd + "_" + manganh + "_" + thuTu;
    }

    // ── Private Query Helpers ───────────────────────────────────────────────────

    private ThiSinh findThiSinh(Session session, String cccd) {
        return session.createQuery("FROM ThiSinh ts WHERE ts.cccd = :cccd", ThiSinh.class)
                .setParameter("cccd", cccd).uniqueResult();
    }

    private Nganh findNganh(Session session, String manganh) {
        return session.createQuery("FROM Nganh n WHERE n.manganh = :manganh", Nganh.class)
                .setParameter("manganh", manganh).uniqueResult();
    }

    private boolean isThuTuDaTonTai(Session session, String cccd, int thuTu) {
        Long count = session.createQuery("SELECT COUNT(nv) FROM NguyenVong nv WHERE nv.thiSinh.cccd = :cccd AND nv.nvTt = :thutu", Long.class)
                .setParameter("cccd", cccd).setParameter("thutu", thuTu).uniqueResult();
        return count != null && count > 0;
    }

    private long countNguyenVong(Session session, String cccd) {
        Long count = session.createQuery("SELECT COUNT(nv) FROM NguyenVong nv WHERE nv.thiSinh.cccd = :cccd", Long.class)
                .setParameter("cccd", cccd).uniqueResult();
        return count != null ? count : 0L;
    }

    // ── Inner Classes & Enums ───────────────────────────────────────────────────

    public static final class KetQuaDangKy {
        private final boolean thanhCong;
        private final String thongDiep;
        private KetQuaDangKy(boolean tc, String msg) { this.thanhCong = tc; this.thongDiep = msg; }
        public static KetQuaDangKy thanhCong(String msg) { return new KetQuaDangKy(true, msg); }
        public static KetQuaDangKy thatBai(String msg) { return new KetQuaDangKy(false, msg); }
        public boolean isThanhCong() { return thanhCong; }
        public String getThongDiep() { return thongDiep; }
    }

    public enum SaveResult { OK, NOT_QUALIFIED, MAX_REACHED, FORBIDDEN }
}