package com.tuyensinh.dao;

import com.tuyensinh.model.NhatKyHoatDong;
import com.tuyensinh.util.HibernateUtil;
import org.hibernate.Session;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class NhatKyHoatDongDAO extends GenericDAO<NhatKyHoatDong> {
    public NhatKyHoatDongDAO() {
        super(NhatKyHoatDong.class);
    }

    // Hàm lấy N dòng log mới nhất cho Dashboard
    public CompletableFuture<List<NhatKyHoatDong>> getLatestLogs(int limit) {
        return CompletableFuture.supplyAsync(() -> {
            try (Session session = HibernateUtil.getSessionFactory().openSession()) {
                return session.createQuery("FROM NhatKyHoatDong ORDER BY thoiGian DESC", NhatKyHoatDong.class)
                        .setMaxResults(limit)
                        .list();
            }
        });
    }
}