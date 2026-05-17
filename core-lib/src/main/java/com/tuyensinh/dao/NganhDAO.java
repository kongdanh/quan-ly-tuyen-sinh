package com.tuyensinh.dao;

import com.tuyensinh.model.Nganh;
import com.tuyensinh.util.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public class NganhDAO extends GenericDAO<Nganh> {

    public NganhDAO() {
        super(Nganh.class);
    }

    // ================================================================
    // BASIC QUERIES
    // ================================================================

    public List<Nganh> getAll() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery("FROM Nganh n ORDER BY n.manganh", Nganh.class).list();
        } catch (Exception e) {
            System.err.println("[NganhDAO] getAll: " + e.getMessage());
            return new ArrayList<>();
        }
    }
    /**
     * Đếm số nguyện vọng đang ký thực tế từ bảng xt_nguyenvong.
     * Trả về Map<maNganh, soLuong> cho toàn bộ danh sách.
     */

    public Optional<Nganh> findByMaNganh(String maNganh) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Nganh result = session.createQuery(
                            "FROM Nganh n WHERE n.manganh = :ma", Nganh.class)
                    .setParameter("ma", maNganh).uniqueResult();
            return Optional.ofNullable(result);
        } catch (Exception e) {
            System.err.println("[NganhDAO] findByMaNganh: " + e.getMessage());
            return Optional.empty();
        }
    }
    /**
     * Đếm số nguyện vọng đang ký thực tế từ bảng xt_nguyenvong.
     * Trả về Map<maNganh, soLuong> cho toàn bộ danh sách.
     */
    public Map<String, Long> countDangKyByMaNganh() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            @SuppressWarnings("unchecked")
            List<Object[]> rows = session.createNativeQuery(
                            "SELECT nv_manganh, COUNT(*) FROM xt_nguyenvongxettuyen GROUP BY nv_manganh")
                    .getResultList();
            Map<String, Long> result = new java.util.HashMap<>();
            for (Object[] row : rows) {
                if (row[0] != null)
                    result.put(row[0].toString(), ((Number) row[1]).longValue());
            }
            return result;
        } catch (Exception e) {
            System.err.println("[NganhDAO] countDangKyByMaNganh: " + e.getMessage());
            return java.util.Collections.emptyMap();
        }
    }
    public boolean existsByMaNganh(String maNganh) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Long count = session.createQuery(
                            "SELECT COUNT(n) FROM Nganh n WHERE n.manganh = :ma", Long.class)
                    .setParameter("ma", maNganh).uniqueResult();
            return count != null && count > 0;
        } catch (Exception e) {
            System.err.println("[NganhDAO] existsByMaNganh: " + e.getMessage());
            return false;
        }
    }

    // ================================================================
    // SEARCH + PAGINATION
    // ================================================================

    public CompletableFuture<List<Nganh>> searchPage(
            String keyword, List<String> searchFields,
            Map<String, Object> filters, int page, int pageSize) {
        return findPageWithFilters(keyword, searchFields, filters, page, pageSize);
    }

    public CompletableFuture<Long> searchCount(
            String keyword, List<String> searchFields, Map<String, Object> filters) {
        return countWithFiltersAsync(keyword, searchFields, filters);
    }

    // ================================================================
    // STATISTICS
    // ================================================================

    public List<Object[]> getThongKeDangKy() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            String hql = "SELECT n.manganh, n.tennganh, n.nChitieu, COUNT(nv.id) " +
                    "FROM Nganh n LEFT JOIN NguyenVong nv ON nv.nganh.id = n.id " +
                    "GROUP BY n.id, n.manganh, n.tennganh, n.nChitieu ORDER BY n.manganh";
            return session.createQuery(hql, Object[].class).list();
        } catch (Exception e) {
            System.err.println("[NganhDAO] getThongKeDangKy: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    public List<Nganh> getAllForComboBox() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery("FROM Nganh n ORDER BY n.tennganh", Nganh.class).list();
        } catch (Exception e) {
            System.err.println("[NganhDAO] getAllForComboBox: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    // ================================================================
    // KIEM TRA RANG BUOC TRUOC KHI XOA
    // ================================================================

    /**
     * Dem ban ghi trong xt_nganh_tohop co FK -> xt_nganh(manganh).
     * Table nay gay ra loi foreign key khi xoa nganh.
     */
    public long countNganhToHop(String maNganh) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Object result = session.createNativeQuery(
                            "SELECT COUNT(*) FROM xt_nganh_tohop WHERE manganh = :ma")
                    .setParameter("ma", maNganh)
                    .getSingleResult();
            return result instanceof Number ? ((Number) result).longValue() : 0L;
        } catch (Exception e) {
            System.err.println("[NganhDAO] countNganhToHop: " + e.getMessage());
            return 0;
        }
    }

    /**
     * Dem nguyen vong dang ky nganh nay.
     * Thu FK qua manganh truoc, neu loi thi thu qua idnganh.
     */
    public long countNguyenVong(int idNganh, String maNganh) {
        // Thu theo cot nv_manganh truoc
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Object result = session.createNativeQuery(
                            "SELECT COUNT(*) FROM xt_nguyenvongxettuyen WHERE nv_manganh = :ma")
                    .setParameter("ma", maNganh)
                    .getSingleResult();
            return result instanceof Number ? ((Number) result).longValue() : 0L;
        } catch (Exception e1) {
            // Thu theo cot idnganh
            try (Session session = HibernateUtil.getSessionFactory().openSession()) {
                Object result = session.createNativeQuery(
                                "SELECT COUNT(*) FROM xt_nguyenvongxettuyen WHERE idnganh = :id")
                        .setParameter("id", idNganh)
                        .getSingleResult();
                return result instanceof Number ? ((Number) result).longValue() : 0L;
            } catch (Exception e2) {
                System.err.println("[NganhDAO] countNguyenVong: " + e2.getMessage());
                return 0;
            }
        }
    }

    /**
     * Kiem tra tong hop tat ca rang buoc FK truoc khi xoa.
     * Tra ve danh sach mo ta cac bang con con du lieu.
     * VD: ["To hop xet tuyen: 3 ban ghi", "Nguyen vong dang ky: 12 ban ghi"]
     * Neu danh sach rong -> co the xoa an toan khong can cascade.
     */
    public List<String> checkDependencies(int idNganh, String maNganh) {
        List<String> deps = new ArrayList<>();

        long soToHop = countNganhToHop(maNganh);
        if (soToHop > 0)
            deps.add("T\u1ed5 h\u1ee3p x\u00e9t tuy\u1ec3n: " + soToHop + " b\u1ea3n ghi");

        long soNV = countNguyenVong(idNganh, maNganh);
        if (soNV > 0)
            deps.add("Nguy\u1ec7n v\u1ecdng \u0111\u0103ng k\u00fd: " + soNV + " b\u1ea3n ghi");

        return deps;
    }

    // ================================================================
    // XOA CASCADE
    // ================================================================

    /**
     * Lay danh sach ten cot cua 1 bang trong DB hien tai.
     * Tra ve List rong neu bang khong ton tai.
     */
    private List<String> getTableColumns(Session session, String tableName) {
        try {
            // Lay ten database hien tai
            String dbName = (String) session
                    .createNativeQuery("SELECT DATABASE()").getSingleResult();
            @SuppressWarnings("unchecked")
            List<String> cols = session.createNativeQuery(
                            "SELECT COLUMN_NAME FROM INFORMATION_SCHEMA.COLUMNS " +
                                    "WHERE TABLE_SCHEMA = :db AND TABLE_NAME = :tbl")
                    .setParameter("db", dbName)
                    .setParameter("tbl", tableName)
                    .getResultList();
            return cols;
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    /**
     * Xoa nganh va toan bo du lieu con lien quan trong 1 transaction.
     * Kiem tra tung bang ton tai truoc khi xoa de tranh loi table doesn't exist.
     * Thu tu: xoa cac bang con FK -> xoa xt_nganh.
     * Chi goi sau khi user da xac nhan dong y xoa het.
     */
    public CompletableFuture<Boolean> deleteCascadeAsync(int idNganh, String maNganh) {
        return CompletableFuture.supplyAsync(() -> {
            Transaction tx = null;
            try (Session session = HibernateUtil.getSessionFactory().openSession()) {
                tx = session.beginTransaction();

                // 1. Xoa xt_nganh_tohop neu ton tai
                List<String> toHopCols = getTableColumns(session, "xt_nganh_tohop");
                if (!toHopCols.isEmpty()) {
                    session.createNativeQuery(
                                    "DELETE FROM xt_nganh_tohop WHERE manganh = :ma")
                            .setParameter("ma", maNganh)
                            .executeUpdate();
                }

                // 2. Xoa cac bang con co FK -> xt_nganh neu chung ton tai
                //    Lay danh sach bang/cot co FK tro ve xt_nganh tu INFORMATION_SCHEMA
                try {
                    String dbName = (String) session
                            .createNativeQuery("SELECT DATABASE()").getSingleResult();
                    @SuppressWarnings("unchecked")
                    List<Object[]> fkRefs = session.createNativeQuery(
                                    "SELECT kcu.TABLE_NAME, kcu.COLUMN_NAME " +
                                            "FROM INFORMATION_SCHEMA.KEY_COLUMN_USAGE kcu " +
                                            "JOIN INFORMATION_SCHEMA.REFERENTIAL_CONSTRAINTS rc " +
                                            "  ON rc.CONSTRAINT_NAME = kcu.CONSTRAINT_NAME " +
                                            "  AND rc.CONSTRAINT_SCHEMA = kcu.TABLE_SCHEMA " +
                                            "WHERE kcu.TABLE_SCHEMA = :db " +
                                            "  AND kcu.REFERENCED_TABLE_NAME = 'xt_nganh' " +
                                            "  AND kcu.TABLE_NAME != 'xt_nganh_tohop'")
                            .setParameter("db", dbName)
                            .getResultList();

                    for (Object[] ref : fkRefs) {
                        String childTable = (String) ref[0];
                        String childCol   = (String) ref[1];
                        // Xoa theo manganh neu cot la text, theo idnganh neu cot la int
                        try {
                            // Thu xoa theo manganh truoc
                            session.createNativeQuery(
                                            "DELETE FROM " + childTable + " WHERE " + childCol + " = :ma")
                                    .setParameter("ma", maNganh)
                                    .executeUpdate();
                        } catch (Exception e) {
                            // Neu loi (kieu du lieu khong khop) -> xoa theo id
                            session.createNativeQuery(
                                            "DELETE FROM " + childTable + " WHERE " + childCol + " = :id")
                                    .setParameter("id", idNganh)
                                    .executeUpdate();
                        }
                    }
                } catch (Exception e) {
                    System.err.println("[NganhDAO] Warn: could not query FK refs: " + e.getMessage());
                    // Tiep tuc xoa nganh chinh du co loi o buoc nay
                }

                // 3. Xoa chinh xt_nganh
                session.createNativeQuery(
                                "DELETE FROM xt_nganh WHERE idnganh = :id")
                        .setParameter("id", idNganh)
                        .executeUpdate();

                tx.commit();
                return true;

            } catch (Exception e) {
                if (tx != null) { try { tx.rollback(); } catch (Exception ignored) {} }
                throw new RuntimeException("L\u1ed7i x\u00f3a cascade: " + e.getMessage(), e);
            }
        });
    }
}