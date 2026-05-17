package com.tuyensinh.dao;

import com.tuyensinh.model.DiemThiXetTuyen;
import com.tuyensinh.util.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.query.Query;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public class DiemThiXetTuyenDAO extends GenericDAO<DiemThiXetTuyen> {

    public static final String PT_THPT = "THPT";
    public static final String PT_VSAT = "VSAT";
    public static final String PT_DGNL = "DGNL";

    public DiemThiXetTuyenDAO() {
        super(DiemThiXetTuyen.class);
    }

    public Optional<DiemThiXetTuyen> findByCccd(String cccd) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            String hql = "SELECT d FROM DiemThiXetTuyen d WHERE d.cccd = :cccd";
            DiemThiXetTuyen diem = session.createQuery(hql, DiemThiXetTuyen.class)
                    .setParameter("cccd", cccd)
                    .uniqueResult();
            return Optional.ofNullable(diem);
        }
    }

    public CompletableFuture<Optional<DiemThiXetTuyen>> findByCccdAsync(String cccd) {
        return CompletableFuture.supplyAsync(() -> findByCccd(cccd));
    }

    public CompletableFuture<List<DiemThiXetTuyen>> findPageForAdmin(String keyword, Map<String, Object> filters, int page, int pageSize) {
        return CompletableFuture.supplyAsync(() -> {
            try (Session session = HibernateUtil.getSessionFactory().openSession()) {
                StringBuilder hql = new StringBuilder(
                    "SELECT d FROM DiemThiXetTuyen d LEFT JOIN FETCH d.thiSinh ts WHERE 1=1 "
                );

                boolean hasKeyword = keyword != null && !keyword.trim().isEmpty();
                boolean hasFilters = filters != null && !filters.isEmpty();

                if (hasKeyword) {
                    hql.append(" AND (d.cccd LIKE :kw OR d.sobaodanh LIKE :kw OR d.dPhuongthuc LIKE :kw) ");
                }

                if (hasFilters) {
                    Object phuongThuc = filters.get("dPhuongthuc");
                    if (phuongThuc != null) {
                        appendMethodFilter(hql, "dPhuongthuc");
                    }
                }

                hql.append(" ORDER BY d.id ASC ");

                Query<DiemThiXetTuyen> query = session.createQuery(hql.toString(), DiemThiXetTuyen.class);
                if (hasKeyword) {
                    query.setParameter("kw", "%" + keyword.trim() + "%");
                }
                if (hasFilters) {
                    Object phuongThuc = filters.get("dPhuongthuc");
                    if (phuongThuc != null) {
                        bindMethodFilter(query, phuongThuc);
                    }
                }

                query.setFirstResult((page - 1) * pageSize);
                query.setMaxResults(pageSize);
                return query.list();
            }
        });
    }

    public CompletableFuture<Long> countForAdmin(String keyword, Map<String, Object> filters) {
        return CompletableFuture.supplyAsync(() -> {
            try (Session session = HibernateUtil.getSessionFactory().openSession()) {
                StringBuilder hql = new StringBuilder(
                    "SELECT count(d) FROM DiemThiXetTuyen d LEFT JOIN d.thiSinh ts WHERE 1=1 "
                );

                boolean hasKeyword = keyword != null && !keyword.trim().isEmpty();
                boolean hasFilters = filters != null && !filters.isEmpty();

                if (hasKeyword) {
                    hql.append(" AND (d.cccd LIKE :kw OR d.sobaodanh LIKE :kw OR d.dPhuongthuc LIKE :kw) ");
                }

                if (hasFilters) {
                    Object phuongThuc = filters.get("dPhuongthuc");
                    if (phuongThuc != null) {
                        appendMethodFilter(hql, "dPhuongthuc");
                    }
                }

                Query<Long> query = session.createQuery(hql.toString(), Long.class);
                if (hasKeyword) {
                    query.setParameter("kw", "%" + keyword.trim() + "%");
                }
                if (hasFilters) {
                    Object phuongThuc = filters.get("dPhuongthuc");
                    if (phuongThuc != null) {
                        bindMethodFilter(query, phuongThuc);
                    }
                }

                return query.uniqueResult();
            }
        });
    }

    public CompletableFuture<DiemThiXetTuyen> findByIdForAdmin(Integer id) {
        return CompletableFuture.supplyAsync(() -> {
            try (Session session = HibernateUtil.getSessionFactory().openSession()) {
                return session.createQuery(
                    "SELECT d FROM DiemThiXetTuyen d LEFT JOIN FETCH d.thiSinh ts WHERE d.id = :id",
                        DiemThiXetTuyen.class
                    )
                    .setParameter("id", id)
                    .uniqueResult();
            }
        });
    }

    public CompletableFuture<List<DiemThiXetTuyen>> findAllForAdmin() {
        return CompletableFuture.supplyAsync(() -> {
            try (Session session = HibernateUtil.getSessionFactory().openSession()) {
                return session.createQuery(
                        "SELECT d FROM DiemThiXetTuyen d LEFT JOIN FETCH d.thiSinh ts ORDER BY d.id ASC",
                                DiemThiXetTuyen.class
                        )
                        .list();
            }
        });
    }

    public static String normalizeMethod(String raw) {
        if (raw == null) {
            return null;
        }
        String value = raw.trim().toUpperCase();
        if (value.isEmpty()) {
            return null;
        }
        if ("0".equals(value) || PT_THPT.equals(value)) {
            return PT_THPT;
        }
        if ("3".equals(value) || PT_VSAT.equals(value) || "SAT".equals(value)) {
            return PT_VSAT;
        }
        if ("4".equals(value) || PT_DGNL.equals(value)) {
            return PT_DGNL;
        }
        return value;
    }

    private void appendMethodFilter(StringBuilder hql, String paramBase) {
        hql.append(" AND (d.dPhuongthuc = :").append(paramBase).append("Norm ")
           .append("OR d.dPhuongthuc = :").append(paramBase).append("Legacy ")
           .append("OR ( :").append(paramBase).append("Norm = 'DGNL' AND d.nl1 IS NOT NULL ) ")
           .append("OR ( :").append(paramBase).append("Norm = 'VSAT' AND d.nk1 IS NOT NULL ) ")
           .append(") ");
    }

    private void bindMethodFilter(Query<?> query, Object rawMethod) {
        String normalized = normalizeMethod(String.valueOf(rawMethod));
        String legacy = switch (normalized) {
            case PT_THPT -> "0";
            case PT_VSAT -> "3";
            case PT_DGNL -> "4";
            default -> normalized;
        };
        query.setParameter("dPhuongthucNorm", normalized);
        query.setParameter("dPhuongthucLegacy", legacy);
    }

    /**
     * Override xóa trực tiếp bằng HQL DELETE để tránh lỗi constraint
     * do quan hệ OneToOne với ThiSinh.
     */
    @Override
    public CompletableFuture<Boolean> deleteByIdAsync(java.io.Serializable id) {
        return CompletableFuture.supplyAsync(() -> {
            org.hibernate.Transaction tx = null;
            try (Session session = HibernateUtil.getSessionFactory().openSession()) {
                tx = session.beginTransaction();
                int deleted = session.createMutationQuery(
                    "DELETE FROM DiemThiXetTuyen d WHERE d.id = :id"
                ).setParameter("id", id).executeUpdate();
                tx.commit();
                return deleted > 0;
            } catch (Exception e) {
                if (tx != null) tx.rollback();
                System.err.println("[DiemThiXetTuyenDAO] deleteByIdAsync lỗi: " + e.getMessage());
                e.printStackTrace();
                return false;
            }
        });
    }

}