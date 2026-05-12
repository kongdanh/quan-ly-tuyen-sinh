package com.tuyensinh.service;

import com.tuyensinh.dao.DotTuyenSinhDAO;
import com.tuyensinh.model.DiemChuanDot;
import com.tuyensinh.model.DotTuyenSinh;
import com.tuyensinh.model.NganhToHop;
import com.tuyensinh.util.HibernateUtil;
import com.tuyensinh.util.SystemLogger;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public class DotTuyenSinhService {
    private final DotTuyenSinhDAO dao = new DotTuyenSinhDAO();

    /**
     * Tim kiem va phan trang danh sach dot tuyen sinh
     */
    public CompletableFuture<List<DotTuyenSinh>> findPageWithFilters(String keyword, List<String> searchFields, Map<String, Object> filters, int page, int size) {
        return dao.findPageWithFilters(keyword, searchFields, filters, page, size);
    }

    /**
     * Dem tong so ban ghi dot tuyen sinh theo bo loc
     */
    public CompletableFuture<Long> countWithFiltersAsync(String keyword, List<String> searchFields, Map<String, Object> filters) {
        return dao.countWithFiltersAsync(keyword, searchFields, filters);
    }

    /**
     * Kiem tra cong dang ky co dang mo hay khong
     */
    public boolean isCongDangKyMo() {
        return getDotDangMo().isPresent();
    }

    /**
     * Tat trang thai tat ca dot dang ACTIVE de dam bao chi co 1 dot hoat dong
     */
    private void deactiveAllOtherDots() {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            String hql = "UPDATE DotTuyenSinh SET trangThai = 'INACTIVE' WHERE trangThai = 'ACTIVE'";
            session.createMutationQuery(hql).executeUpdate(); 
            transaction.commit();
            System.out.println("[DotTuyenSinhService] Da tat toan bo dot ACTIVE cu");
        } catch (Exception e) {
            if (transaction != null) transaction.rollback();
            System.err.println("[DotTuyenSinhService] Loi deactive dot: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Tu dong khoa cac dot da het han (ngay ket thuc < hien tai)
     */
    public void checkAndLockExpiredDots() {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            String hql = "UPDATE DotTuyenSinh SET trangThai = 'INACTIVE' WHERE trangThai = 'ACTIVE' AND ngayKetThuc < :now";
            int updated = session.createMutationQuery(hql)
                   .setParameter("now", LocalDateTime.now())
                   .executeUpdate();
            transaction.commit();
            if (updated > 0) {
                System.out.println("[DotTuyenSinhService] Tu dong khoa " + updated + " dot het han");
                SystemLogger.log(null, "System", "Tự động khóa " + updated + " đợt tuyển sinh hết hạn", true);
            }
        } catch (Exception e) {
            if (transaction != null) transaction.rollback();
            System.err.println("[DotTuyenSinhService] Loi auto-lock dot het han: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Luu dot tuyen sinh moi va tu dong tao bang diem chuan mac dinh
     */
    public void save(DotTuyenSinh entity) { 
        System.out.println("[DotTuyenSinhService] Tao dot tuyen sinh moi: " + entity.getTenDot());
        if ("ACTIVE".equals(entity.getTrangThai())) deactiveAllOtherDots();
        dao.save(entity); 
        
        khoiTaoDiemChuanMacDinh(entity);
        
        System.out.println("[DotTuyenSinhService] Luu dot tuyen sinh thanh cong, ID=" + entity.getId());
        SystemLogger.log(null, "System", "Tạo đợt tuyển sinh: " + entity.getTenDot(), true);
    }
    
    /**
     * Cap nhat thong tin dot tuyen sinh
     */
    public void update(DotTuyenSinh entity) { 
        System.out.println("[DotTuyenSinhService] Cap nhat dot tuyen sinh ID=" + entity.getId());
        if ("ACTIVE".equals(entity.getTrangThai())) deactiveAllOtherDots();
        dao.update(entity); 
        System.out.println("[DotTuyenSinhService] Cap nhat dot tuyen sinh thanh cong, ID=" + entity.getId());
        SystemLogger.log(null, "System", "Cập nhật đợt tuyển sinh: " + entity.getTenDot(), true);
    }
    
    public DotTuyenSinh findById(Serializable id) { return dao.findById(id); }

    /**
     * Xoa dot tuyen sinh theo ID
     */
    public CompletableFuture<Boolean> deleteByIdAsync(Serializable id) { 
        System.out.println("[DotTuyenSinhService] Xoa dot tuyen sinh ID=" + id);
        SystemLogger.log(null, "System", "Xóa đợt tuyển sinh ID=" + id, true);
        return dao.deleteByIdAsync(id); 
    }

    /**
     * Lay dot tuyen sinh dang mo (ACTIVE va trong khoang thoi gian hop le)
     */
    public Optional<DotTuyenSinh> getDotDangMo() {
        checkAndLockExpiredDots();
        
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            LocalDateTime now = LocalDateTime.now();
            String hql = "FROM DotTuyenSinh d WHERE d.trangThai = 'ACTIVE' AND d.ngayBatDau <= :now AND d.ngayKetThuc >= :now";
            Query<DotTuyenSinh> query = session.createQuery(hql, DotTuyenSinh.class);
            query.setParameter("now", now);
            query.setMaxResults(1);
            return query.uniqueResultOptional();
        } catch (Exception e) {
            System.err.println("[DotTuyenSinhService] Loi lay dot dang mo: " + e.getMessage());
            e.printStackTrace();
            return Optional.empty();
        }
    }

    /**
     * Khoi tao diem chuan cho dot (ke thua diem tu dot truoc cung phuong thuc)
     */
    private void khoiTaoDiemChuanChoDot(DotTuyenSinh dot) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Transaction tx = session.beginTransaction();
            
            List<NganhToHop> listToHop = session.createQuery("FROM NganhToHop", NganhToHop.class).getResultList();
            
            BigDecimal diemMacDinh = dot.getMaPhuongThuc() != null && dot.getMaPhuongThuc().equals("DGNL") 
                                     ? new BigDecimal("100.0") 
                                     : new BigDecimal("15.0");

            for (NganhToHop th : listToHop) {
                DiemChuanDot dc = new DiemChuanDot();
                dc.setDotTuyenSinh(dot);
                dc.setNganhToHop(th);
                
                BigDecimal diemKeThua = timDiemChuanDotTruoc(th.getId(), dot.getMaPhuongThuc());
                dc.setDiemChuan(diemKeThua != null ? diemKeThua : diemMacDinh);
                
                session.persist(dc);
            }
            
            tx.commit();
            System.out.println("[DotTuyenSinhService] Khoi tao diem chuan ke thua cho Dot ID=" + dot.getId() + " (" + listToHop.size() + " ban ghi)");
        }
    }

    /**
     * Tim diem chuan cua dot truoc do cung phuong thuc de ke thua
     */
    private BigDecimal timDiemChuanDotTruoc(Integer idToHop, String phuongThuc) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            String hql = "SELECT dc.diemChuan FROM DiemChuanDot dc " +
                         "WHERE dc.nganhToHop.id = :idTh AND dc.dotTuyenSinh.maPhuongThuc = :pt " +
                         "ORDER BY dc.dotTuyenSinh.id DESC";
            return session.createQuery(hql, BigDecimal.class)
                          .setParameter("idTh", idToHop)
                          .setParameter("pt", phuongThuc)
                          .setMaxResults(1)
                          .uniqueResultOptional()
                          .orElse(null);
        }
    }

    /**
     * Khoi tao diem chuan mac dinh cho dot tuyen sinh moi (diem san theo phuong thuc)
     */
    private void khoiTaoDiemChuanMacDinh(DotTuyenSinh dot) {
        try (org.hibernate.Session session = com.tuyensinh.util.HibernateUtil.getSessionFactory().openSession()) {
            org.hibernate.Transaction tx = session.beginTransaction();
            
            java.util.List<com.tuyensinh.model.NganhToHop> list = 
                session.createQuery("FROM NganhToHop", com.tuyensinh.model.NganhToHop.class).getResultList();
            
            java.math.BigDecimal defaultScore = dot.getMaPhuongThuc() != null && dot.getMaPhuongThuc().equals("DGNL") 
                                                ? new java.math.BigDecimal("100.0") 
                                                : new java.math.BigDecimal("15.0");

            System.out.println("[DotTuyenSinhService] Khoi tao " + list.size() + " DiemChuanDot cho Dot ID=" + dot.getId());
            
            for (com.tuyensinh.model.NganhToHop th : list) {
                com.tuyensinh.model.DiemChuanDot dc = new com.tuyensinh.model.DiemChuanDot();
                dc.setDotTuyenSinh(dot);
                dc.setNganhToHop(th);
                dc.setDiemChuan(defaultScore);
                session.persist(dc);
            }
            tx.commit();
            System.out.println("[DotTuyenSinhService] Hoan thanh khoi tao DiemChuanDot cho Dot ID=" + dot.getId());
        } catch (Exception e) {
            System.err.println("[DotTuyenSinhService] Loi khoi tao DiemChuanDot: " + e.getMessage());
            e.printStackTrace();
        }
    }

}