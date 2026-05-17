package com.tuyensinh.service;

import com.tuyensinh.dao.DiemChuanDotDAO;
import com.tuyensinh.model.DiemChuanDot;
import com.tuyensinh.model.DotTuyenSinh;
import com.tuyensinh.model.NganhToHop;
import com.tuyensinh.util.HibernateUtil;
import com.tuyensinh.util.SystemLogger;
import org.hibernate.Session;
import org.hibernate.Transaction;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class DiemChuanDotService {
    private final DiemChuanDotDAO dao = new DiemChuanDotDAO();

    /**
     * Phân trang và lọc danh sách điểm chuẩn theo đợt tuyển sinh
     */
    public CompletableFuture<List<DiemChuanDot>> findPage(String kw, Map<String, Object> filters, int page, int size) {
        return dao.findPageWithFilters(kw, List.of("nganhToHop.nganh.manganh"), filters, page, size);
    }

    /**
     * Đếm tổng bản ghi điểm chuẩn theo bộ lọc
     */
    public CompletableFuture<Long> countAsync(String kw, Map<String, Object> filters) {
        return dao.countWithFiltersAsync(kw, List.of("nganhToHop.nganh.manganh"), filters);
    }

    /**
     * Cập nhật hàng loạt điểm chuẩn cho danh sách DiemChuanDot
     */
    public void updateBatch(List<DiemChuanDot> list) {
        System.out.println("[DiemChuanDotService] Cap nhat batch " + list.size() + " ban ghi diem chuan");
        for (DiemChuanDot dc : list) {
            dao.update(dc);
        }
        System.out.println("[DiemChuanDotService] Hoan thanh cap nhat batch diem chuan");
        SystemLogger.log(null, "System", "Cập nhật hàng loạt điểm chuẩn (" + list.size() + " bản ghi)", true);
    }

    /**
     * Khoi tao DiemChuanDot cho dot tuyen sinh neu chua co ban ghi nao.
     * Synchronized de tranh race condition khi nhieu thread goi dong thoi.
     * @return true neu da tao du lieu moi
     */
    public synchronized boolean initializeForDotIfEmpty(DotTuyenSinh dot) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Long count = session.createQuery(
                "SELECT count(dc) FROM DiemChuanDot dc WHERE dc.dotTuyenSinh.id = :dotId", Long.class)
                .setParameter("dotId", dot.getId())
                .uniqueResult();

            if (count != null && count > 0) {
                System.out.println("[DiemChuanDotService] Dot ID=" + dot.getId() + " da co " + count + " ban ghi, bo qua khoi tao");
                return false;
            }

            Transaction tx = session.beginTransaction();
            List<NganhToHop> listToHop = session.createQuery("FROM NganhToHop", NganhToHop.class).getResultList();

            BigDecimal defaultScore = "DGNL".equals(dot.getMaPhuongThuc())
                    ? new BigDecimal("100.0")
                    : new BigDecimal("15.0");

            System.out.println("[DiemChuanDotService] Auto-init " + listToHop.size() + " DiemChuanDot cho Dot ID=" + dot.getId());

            for (NganhToHop th : listToHop) {
                DiemChuanDot dc = new DiemChuanDot();
                dc.setDotTuyenSinh(dot);
                dc.setNganhToHop(th);
                dc.setDiemChuan(defaultScore);
                session.persist(dc);
            }

            tx.commit();
            System.out.println("[DiemChuanDotService] Hoan thanh auto-init cho Dot ID=" + dot.getId());
            SystemLogger.log(null, "System", "Khởi tạo điểm chuẩn mặc định cho Đợt ID=" + dot.getId() + " (" + listToHop.size() + " bản ghi)", true);
            return true;
        } catch (Exception e) {
            System.err.println("[DiemChuanDotService] Loi auto-init: " + e.getMessage());
            e.printStackTrace();
            SystemLogger.log(null, "System", "Lỗi khởi tạo điểm chuẩn cho Đợt ID=" + dot.getId() + ": " + e.getMessage(), false);
            return false;
        }
    }
}