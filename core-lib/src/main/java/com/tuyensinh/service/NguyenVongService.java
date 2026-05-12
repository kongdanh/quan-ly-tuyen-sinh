package com.tuyensinh.service;

import com.tuyensinh.dao.*;
import com.tuyensinh.model.*;
import com.tuyensinh.util.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;

import java.math.BigDecimal;
import java.util.*;

public class NguyenVongService {

    private static final int MAX_NGUYEN_VONG = 100;

    private final NguyenVongDAO nvDAO = new NguyenVongDAO();
    private final NganhToHopDAO ntDAO = new NganhToHopDAO();
    private final NganhDAO nganhDAO = new NganhDAO();
    private final ThiSinhService thiSinhService = new ThiSinhService();
    private final DiemService diemService = new DiemService();

    public KetQuaDangKy dangKyNguyenVong(String cccd,
            String manganh,
            String matohop,
            int thuTu,
            String phuongThuc,
            String thm) {

        System.out.println("[DangKy] Bắt đầu: CCCD=" + cccd + " ngành=" + manganh + " tổhợp=" + matohop + " thuTu="
                + thuTu + " pt=" + phuongThuc);

        try {
            String validErr = validate(cccd, manganh, matohop);
            if (validErr != null) {
                System.err.println("[DangKy] Validation thất bại: " + validErr);
                return KetQuaDangKy.thatBai(validErr);
            }
        } catch (Exception e) {
            System.err.println("[DangKy] Lỗi khi validation CCCD=" + cccd + ": " + e.getMessage());
            e.printStackTrace();
            return KetQuaDangKy.thatBai("Lỗi hệ thống khi kiểm tra dữ liệu: " + e.getMessage());
        }

        Session session = null;
        Transaction tx = null;

        try {
            session = HibernateUtil.getSessionFactory().openSession();
            tx = session.beginTransaction();

            if (isThuTuDaTonTai(session, cccd, thuTu)) {
                tx.rollback();
                String msg = "Thứ tự nguyện vọng " + thuTu + " đã tồn tại cho CCCD=" + cccd;
                System.err.println("[DangKy] " + msg);
                return KetQuaDangKy.thatBai(msg);
            }

            long soNvHienTai = countNguyenVong(session, cccd);
            if (soNvHienTai >= MAX_NGUYEN_VONG) {
                tx.rollback();
                String msg = "Thí sinh CCCD=" + cccd + " đã đăng ký tối đa "
                        + MAX_NGUYEN_VONG + " nguyện vọng.";
                System.err.println("[DangKy] " + msg);
                return KetQuaDangKy.thatBai(msg);
            }

            ThiSinh thiSinh = findThiSinh(session, cccd);
            Nganh nganh = findNganh(session, manganh);

            if (thiSinh == null || nganh == null) {
                tx.rollback();
                return KetQuaDangKy.thatBai("Không tải được entity ThiSinh hoặc Nganh.");
            }

            String nvKeys = cccd + "_" + manganh + "_" + thuTu + "_" + phuongThuc;

            NguyenVong nv = new NguyenVong();
            nv.setThiSinh(thiSinh);
            nv.setNganh(nganh);
            nv.setNvTt(thuTu);
            nv.setTtPhuongthuc(phuongThuc);
            nv.setTtThm(thm);
            nv.setNvKetqua("CHUA_XET");
            nv.setNvKeys(nvKeys);

            session.persist(nv);
            System.out.println("[DangKy] Đã tạo NguyenVong id=" + nv.getId() + " CCCD=" + cccd + " ngành=" + manganh
                    + " tổhợp=" + matohop + " thuTu=" + thuTu);

            int updatedRows = session.createNativeMutationQuery(
                    "UPDATE xt_nganh SET sl_dadangky = sl_dadangky + 1 " +
                            "WHERE manganh = :manganh")
                    .setParameter("manganh", manganh)
                    .executeUpdate();

            if (updatedRows == 0) {
                tx.rollback();
                System.err.println("[DangKy] Không cập nhật sl_dadangky cho ngành " + manganh);
                return KetQuaDangKy.thatBai("Không cập nhật được sl_dadangky cho ngành " + manganh);
            }

            tx.commit();
            System.out.println(
                    "[DangKy] Thành công: CCCD=" + cccd + " ngành=" + manganh + " thuTu=" + thuTu + " sl_dadangky+1");
            return KetQuaDangKy.thanhCong(
                    "Đăng ký nguyện vọng thành công: ngành=" + manganh + ", thứ tự=" + thuTu);

        } catch (Exception e) {
            if (tx != null && tx.isActive()) {
                try {
                    tx.rollback();
                    System.err.println("[DangKy] Đã rollback transaction: " + e.getMessage());
                } catch (Exception rb) {
                    System.err.println("[DangKy] Rollback thất bại: " + rb.getMessage());
                }
            }
            System.err.println("[DangKy] Lỗi khi đăng ký CCCD=" + cccd + ": " + e.getMessage());
            e.printStackTrace();
            return KetQuaDangKy.thatBai("Lỗi hệ thống: " + e.getMessage());

        } finally {
            if (session != null && session.isOpen())
                session.close();
        }
    }

    public KetQuaDangKy huyNguyenVong(int nvId) {
        System.out.println("[Huy] Bắt đầu huỷ nguyện vọng id=" + nvId);

        Session session = null;
        Transaction tx = null;

        try {
            session = HibernateUtil.getSessionFactory().openSession();
            tx = session.beginTransaction();

            NguyenVong nv = session.get(NguyenVong.class, nvId);
            if (nv == null) {
                tx.rollback();
                return KetQuaDangKy.thatBai("Không tìm thấy nguyện vọng id=" + nvId);
            }

            String manganh = nv.getNganh().getManganh();
            String cccd = nv.getThiSinh().getCccd();

            session.remove(nv);

            session.createNativeMutationQuery(
                    "UPDATE xt_nganh SET sl_dadangky = GREATEST(sl_dadangky - 1, 0) " +
                            "WHERE manganh = :manganh")
                    .setParameter("manganh", manganh)
                    .executeUpdate();

            tx.commit();
            System.out.println("[Huy] Thành công: id=" + nvId + " CCCD=" + cccd + " ngành=" + manganh);
            return KetQuaDangKy.thanhCong("Huỷ nguyện vọng thành công.");

        } catch (Exception e) {
            if (tx != null && tx.isActive()) {
                try {
                    tx.rollback();
                } catch (Exception rb) {
                    System.err.println("[Huy] Rollback thất bại: " + rb.getMessage());
                }
            }
            System.err.println("[Huy] Lỗi khi huỷ nguyện vọng id=" + nvId + ": " + e.getMessage());
            return KetQuaDangKy.thatBai("Lỗi hệ thống: " + e.getMessage());

        } finally {
            if (session != null && session.isOpen())
                session.close();
        }
    }

    public SaveResult saveWish(String cccd, String manganh, Integer idnv, DiemThiXetTuyen diem) {
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

        if (toHopToiUu == null)
            return SaveResult.NOT_QUALIFIED;

        Nganh nganh = nganhDAO.findByMaNganh(manganh).orElse(null);
        String ttThm = toHopToiUu.getThMon1() + "-"
                + toHopToiUu.getThMon2() + "-"
                + toHopToiUu.getThMon3();

        if (idnv != null) {
            return updateWish(cccd, idnv, nganh, manganh, diemMax, ttThm);
        } else {
            return insertWish(cccd, nganh, manganh, diemMax, ttThm, diem);
        }
    }

    public boolean deleteWish(int idnv, String cccd) {
        NguyenVong nv = nvDAO.findById(idnv);
        if (nv == null || !nv.getThiSinh().getCccd().equals(cccd)) {
            System.err.println("[DeleteWish] Không tìm thấy hoặc không có quyền: idnv=" + idnv + " cccd=" + cccd);
            return false;
        }
        nvDAO.delete(nv);
        reorder(cccd);
        System.out.println("[DeleteWish] Đã xoá nguyện vọng id=" + idnv + " CCCD=" + cccd);
        return true;
    }

    public void reorderByIds(String[] ids, String cccd) {
        Set<Integer> validIds = new HashSet<>();
        for (NguyenVong v : nvDAO.findByCccd(cccd))
            validIds.add(v.getId());

        for (int i = 0; i < ids.length; i++) {
            try {
                int id = Integer.parseInt(ids[i].trim());
                if (!validIds.contains(id))
                    continue;
                NguyenVong nv = nvDAO.findById(id);
                if (nv == null)
                    continue;
                nv.setNvTt(i + 1);
                nv.setNvKeys(buildKey(cccd, nv.getNganh().getManganh(), i + 1));
                nvDAO.update(nv);
            } catch (NumberFormatException ignored) {
                System.err.println("[Reorder] ID không hợp lệ: " + ids[i]);
            }
        }
        System.out.println("[Reorder] Đã sắp xếp lại " + ids.length + " nguyện vọng của CCCD=" + cccd);
    }

    public List<NguyenVong> findByCccd(String cccd) {
        if (cccd == null || cccd.isBlank())
            return Collections.emptyList();
        return nvDAO.findByCccd(cccd);
    }

    private String validate(String cccd, String manganh, String matohop) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            ThiSinh thiSinh = findThiSinh(session, cccd);
            if (thiSinh == null) {
                return "Thí sinh không tồn tại: CCCD=" + cccd;
            }

            Nganh nganh = findNganh(session, manganh);
            if (nganh == null) {
                return "Ngành không tồn tại: manganh=" + manganh;
            }

            if (!isTohopOfNganh(session, manganh, matohop)) {
                return "Tổ hợp '" + matohop + "' không thuộc ngành '" + manganh
                        + "'. Vui lòng chọn tổ hợp được ngành cho phép.";
            }

            return null;
        }
    }

    private ThiSinh findThiSinh(Session session, String cccd) {
        List<ThiSinh> result = session.createQuery(
                "FROM ThiSinh ts WHERE ts.cccd = :cccd", ThiSinh.class)
                .setParameter("cccd", cccd)
                .setMaxResults(1)
                .list();
        return result.isEmpty() ? null : result.get(0);
    }

    private Nganh findNganh(Session session, String manganh) {
        List<Nganh> result = session.createQuery(
                "FROM Nganh n WHERE n.manganh = :manganh", Nganh.class)
                .setParameter("manganh", manganh)
                .setMaxResults(1)
                .list();
        return result.isEmpty() ? null : result.get(0);
    }

    private boolean isTohopOfNganh(Session session, String manganh, String matohop) {
        Long count = session.createQuery(
                "SELECT COUNT(nth) FROM NganhToHop nth " +
                        "WHERE nth.nganh.manganh = :manganh AND nth.toHopMon.matohop = :matohop",
                Long.class)
                .setParameter("manganh", manganh)
                .setParameter("matohop", matohop)
                .uniqueResult();
        return count != null && count > 0;
    }

    private boolean isThuTuDaTonTai(Session session, String cccd, int thuTu) {
        Long count = session.createQuery(
                "SELECT COUNT(nv) FROM NguyenVong nv " +
                        "WHERE nv.thiSinh.cccd = :cccd AND nv.nvTt = :thutu",
                Long.class)
                .setParameter("cccd", cccd)
                .setParameter("thutu", thuTu)
                .uniqueResult();
        return count != null && count > 0;
    }

    private long countNguyenVong(Session session, String cccd) {
        Long count = session.createQuery(
                "SELECT COUNT(nv) FROM NguyenVong nv WHERE nv.thiSinh.cccd = :cccd",
                Long.class)
                .setParameter("cccd", cccd)
                .uniqueResult();
        return count != null ? count : 0L;
    }

    private SaveResult updateWish(String cccd, int idnv, Nganh nganh,
            String manganh, double diemMax, String ttThm) {
        NguyenVong nv = nvDAO.findById(idnv);
        if (nv == null || !nv.getThiSinh().getCccd().equals(cccd)) {
            return SaveResult.FORBIDDEN;
        }
        nv.setNganh(nganh);
        nv.setDiemXettuyen(BigDecimal.valueOf(diemMax));
        nv.setTtThm(ttThm);
        nv.setNvKeys(buildKey(cccd, manganh, nv.getNvTt()));
        nvDAO.update(nv);
        return SaveResult.OK;
    }

    private SaveResult insertWish(String cccd, Nganh nganh, String manganh,
            double diemMax, String ttThm, DiemThiXetTuyen diem) {
        List<NguyenVong> current = nvDAO.findByCccd(cccd);
        if (current.size() >= MAX_NGUYEN_VONG)
            return SaveResult.MAX_REACHED;

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

    public static final class KetQuaDangKy {
        private final boolean thanhCong;
        private final String thongDiep;

        private KetQuaDangKy(boolean thanhCong, String thongDiep) {
            this.thanhCong = thanhCong;
            this.thongDiep = thongDiep;
        }

        public static KetQuaDangKy thanhCong(String msg) {
            return new KetQuaDangKy(true, msg);
        }

        public static KetQuaDangKy thatBai(String msg) {
            return new KetQuaDangKy(false, msg);
        }

        public boolean isThanhCong() {
            return thanhCong;
        }

        public String getThongDiep() {
            return thongDiep;
        }

        @Override
        public String toString() {
            return (thanhCong ? "[OK] " : "[FAIL] ") + thongDiep;
        }
    }

    public enum SaveResult {
        OK,
        NOT_QUALIFIED,
        MAX_REACHED,
        FORBIDDEN
    }
}