package com.tuyensinh.dao;

import com.tuyensinh.model.YeuCauCapNhat;
import com.tuyensinh.util.HibernateUtil;
import org.hibernate.Session;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public class YeuCauCapNhatDAO extends GenericDAO<YeuCauCapNhat> {

    public YeuCauCapNhatDAO() {
        super(YeuCauCapNhat.class);
    }

    /** Tìm tất cả yêu cầu đang PENDING — dùng cho DanhSachYeuCauDialog */
    public CompletableFuture<List<YeuCauCapNhat>> findPendingRequests() {
        return CompletableFuture.supplyAsync(() -> {
            try (Session session = HibernateUtil.getSessionFactory().openSession()) {
                return session.createQuery(
                    "FROM YeuCauCapNhat WHERE trangThai = 'PENDING' ORDER BY ngayTao DESC",
                    YeuCauCapNhat.class
                ).list();
            }
        });
    }

    /** Đếm số yêu cầu PENDING — dùng cho badge button */
    public CompletableFuture<Long> countPendingRequests() {
        return CompletableFuture.supplyAsync(() -> {
            try (Session session = HibernateUtil.getSessionFactory().openSession()) {
                return session.createQuery(
                    "SELECT COUNT(y) FROM YeuCauCapNhat y WHERE y.trangThai = 'PENDING'",
                    Long.class
                ).uniqueResult();
            }
        });
    }

    /** Tìm theo ID — dùng trong YeuCauCapNhatService */
    public Optional<YeuCauCapNhat> findById(int id) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return Optional.ofNullable(session.get(YeuCauCapNhat.class, id));
        }
    }

    // Lấy yêu cầu mới nhất của 1 thí sinh dựa vào CCCD
    public YeuCauCapNhat findLatestByCccd(String cccd) {
        try (org.hibernate.Session session = com.tuyensinh.util.HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery(
                "FROM YeuCauCapNhat WHERE cccd = :cccd ORDER BY ngayTao DESC", 
                YeuCauCapNhat.class)
                    .setParameter("cccd", cccd)
                    .setMaxResults(1)
                    .uniqueResult();
        } catch (Exception e) {
            System.err.println("Lỗi lấy yêu cầu mới nhất: " + e.getMessage());
            return null;
        }
    }

    // Lấy danh sách các yêu cầu đã có kết quả (ACCEPTED hoặc REJECTED)
    public java.util.List<YeuCauCapNhat> findNotificationsByCccd(String cccd) {
        try (org.hibernate.Session session = com.tuyensinh.util.HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery(
                "FROM YeuCauCapNhat WHERE cccd = :cccd AND trangThai != 'PENDING' ORDER BY id DESC", 
                YeuCauCapNhat.class)
                    .setParameter("cccd", cccd)
                    .list();
        } catch (Exception e) {
            System.err.println("Lỗi load thông báo: " + e.getMessage());
            return java.util.Collections.emptyList();
        }
    }

    // Đánh dấu tất cả thông báo là đã đọc
    public void markAllAsRead(String cccd) {
        try (org.hibernate.Session session = com.tuyensinh.util.HibernateUtil.getSessionFactory().openSession()) {
            session.beginTransaction();
            session.createQuery(
                "UPDATE YeuCauCapNhat SET isRead = true WHERE cccd = :cccd AND trangThai != 'PENDING' AND isRead = false")
                    .setParameter("cccd", cccd)
                    .executeUpdate();
            session.getTransaction().commit();
        } catch (Exception e) {
            System.err.println("Lỗi markAsRead: " + e.getMessage());
        }
    }

}