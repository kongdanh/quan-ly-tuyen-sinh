package com.tuyensinh.dao;

import com.tuyensinh.model.ToHopMon;

import com.tuyensinh.util.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.query.Query;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class ToHopMonDAO extends GenericDAO<ToHopMon> {

    public ToHopMonDAO() {
        super(ToHopMon.class);
    }

    public List<ToHopMon> findNotInNganh(String maNganh) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            String hql = "SELECT t FROM ToHopMon t WHERE t.matohop NOT IN " +
                         "(SELECT nt.toHopMon.matohop FROM NganhToHop nt WHERE nt.nganh.manganh = :maNganh)";
            return session.createQuery(hql, ToHopMon.class)
                    .setParameter("maNganh", maNganh)
                    .list();
        } catch (Exception e) {
            System.err.println("Lỗi truy vấn ToHopMonDAO.findNotInNganh: " + e.getMessage());
            return Collections.emptyList();
        }
    }

    /**
     * Lấy danh sách tất cả mã môn học duy nhất (từ mon1, mon2, mon3) để hiển thị bộ lọc.
     */
    public List<String> findDistinctMonHoc() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            String hql = "SELECT DISTINCT e.mon1 FROM ToHopMon e " +
                         "UNION SELECT DISTINCT e.mon2 FROM ToHopMon e " +
                         "UNION SELECT DISTINCT e.mon3 FROM ToHopMon e " +
                         "ORDER BY 1";
            // HQL không hỗ trợ UNION — dùng native SQL
            @SuppressWarnings("unchecked")
            List<String> result = session.createNativeQuery(
                "SELECT DISTINCT mon FROM (" +
                "  SELECT mon1 AS mon FROM xt_tohop_monthi " +
                "  UNION SELECT mon2 FROM xt_tohop_monthi " +
                "  UNION SELECT mon3 FROM xt_tohop_monthi" +
                ") t ORDER BY mon", String.class).list();
            return result;
        } catch (Exception e) {
            System.err.println("Lỗi truy vấn findDistinctMonHoc: " + e.getMessage());
            return Collections.emptyList();
        }
    }

    /**
     * Phân trang + tìm kiếm + lọc theo môn học.
     * Khi có filter "monHoc" thì kiểm tra mon1 = :mon OR mon2 = :mon OR mon3 = :mon.
     */
    public CompletableFuture<List<ToHopMon>> findPageWithMonFilter(
            String keyword, List<String> searchFields,
            Map<String, Object> filters, int page, int pageSize) {

        return CompletableFuture.supplyAsync(() -> {
            try (Session session = HibernateUtil.getSessionFactory().openSession()) {
                StringBuilder hql = new StringBuilder("FROM ToHopMon e WHERE 1=1 ");

                boolean hasSearch = keyword != null && !keyword.trim().isEmpty()
                        && searchFields != null && !searchFields.isEmpty();
                
                Object monFilterObj = filters != null ? filters.get("monHocs") : null;
                List<String> monFilters = null;
                if (monFilterObj instanceof List) {
                    @SuppressWarnings("unchecked")
                    List<String> temp = (List<String>) monFilterObj;
                    monFilters = temp;
                }
                boolean hasMonFilter = monFilters != null && !monFilters.isEmpty();

                if (hasSearch) {
                    hql.append(" AND (");
                    for (int i = 0; i < searchFields.size(); i++) {
                        hql.append("e.").append(searchFields.get(i)).append(" LIKE :kw");
                        if (i < searchFields.size() - 1) hql.append(" OR ");
                    }
                    hql.append(") ");
                }

                if (hasMonFilter) {
                    for (int j = 0; j < monFilters.size(); j++) {
                        hql.append(" AND (e.mon1 = :mon").append(j)
                           .append(" OR e.mon2 = :mon").append(j)
                           .append(" OR e.mon3 = :mon").append(j).append(")");
                    }
                }

                System.out.println("[ToHopMonDAO] HQL: " + hql);
                Query<ToHopMon> query = session.createQuery(hql.toString(), ToHopMon.class);
                if (hasSearch) query.setParameter("kw", "%" + keyword.trim() + "%");
                if (hasMonFilter) {
                    for (int j = 0; j < monFilters.size(); j++) {
                        query.setParameter("mon" + j, monFilters.get(j));
                    }
                }

                query.setFirstResult((page - 1) * pageSize);
                query.setMaxResults(pageSize);
                return query.list();
            } catch (Exception e) {
                System.err.println("[ToHopMonDAO] findPageWithMonFilter error: " + e.getMessage());
                throw e;
            }
        });
    }

    /**
     * Đếm tổng record theo bộ lọc môn học.
     */
    public CompletableFuture<Long> countWithMonFilter(
            String keyword, List<String> searchFields, Map<String, Object> filters) {

        return CompletableFuture.supplyAsync(() -> {
            try (Session session = HibernateUtil.getSessionFactory().openSession()) {
                StringBuilder hql = new StringBuilder("SELECT count(e) FROM ToHopMon e WHERE 1=1 ");

                boolean hasSearch = keyword != null && !keyword.trim().isEmpty()
                        && searchFields != null && !searchFields.isEmpty();

                Object monFilterObj = filters != null ? filters.get("monHocs") : null;
                List<String> monFilters = null;
                if (monFilterObj instanceof List) {
                    @SuppressWarnings("unchecked")
                    List<String> temp = (List<String>) monFilterObj;
                    monFilters = temp;
                }
                boolean hasMonFilter = monFilters != null && !monFilters.isEmpty();

                if (hasSearch) {
                    hql.append(" AND (");
                    for (int i = 0; i < searchFields.size(); i++) {
                        hql.append("e.").append(searchFields.get(i)).append(" LIKE :kw");
                        if (i < searchFields.size() - 1) hql.append(" OR ");
                    }
                    hql.append(") ");
                }

                if (hasMonFilter) {
                    for (int j = 0; j < monFilters.size(); j++) {
                        hql.append(" AND (e.mon1 = :mon").append(j)
                           .append(" OR e.mon2 = :mon").append(j)
                           .append(" OR e.mon3 = :mon").append(j).append(")");
                    }
                }

                System.out.println("[ToHopMonDAO] COUNT HQL: " + hql);
                Query<Long> query = session.createQuery(hql.toString(), Long.class);
                if (hasSearch) query.setParameter("kw", "%" + keyword.trim() + "%");
                if (hasMonFilter) {
                    for (int j = 0; j < monFilters.size(); j++) {
                        query.setParameter("mon" + j, monFilters.get(j));
                    }
                }

                Long count = query.uniqueResult();
                return count != null ? count : 0L;
            } catch (Exception e) {
                System.err.println("[ToHopMonDAO] countWithMonFilter error: " + e.getMessage());
                throw e;
            }
        });
    }
}