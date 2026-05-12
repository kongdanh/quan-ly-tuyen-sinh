package com.tuyensinh.dao;

import com.tuyensinh.model.DiemCong;
import com.tuyensinh.util.HibernateUtil;

public class DiemCongDAO extends GenericDAO<DiemCong>{

    public DiemCongDAO(Class<DiemCong> entityClass) {
        super(entityClass);
    }

    public DiemCongDAO() {
        super(DiemCong.class);
    }

    @Override
    public java.util.concurrent.CompletableFuture<java.util.List<DiemCong>> findPageWithFilters(String keyword, java.util.List<String> searchFields, java.util.Map<String, Object> filters, int pageIndex, int pageSize) {
        return java.util.concurrent.CompletableFuture.supplyAsync(() -> {
            try (org.hibernate.Session session = HibernateUtil.getSessionFactory().openSession()) {
                StringBuilder hql = new StringBuilder("SELECT e FROM DiemCong e LEFT JOIN FETCH e.thiSinh WHERE 1=1 ");
                
                boolean hasSearch = keyword != null && !keyword.trim().isEmpty() && searchFields != null && !searchFields.isEmpty();
                boolean hasFilters = filters != null && !filters.isEmpty();

                if (hasSearch) {
                    hql.append(" AND (");
                    for (int i = 0; i < searchFields.size(); i++) {
                        hql.append("e.").append(searchFields.get(i)).append(" LIKE :kw");
                        if (i < searchFields.size() - 1) hql.append(" OR ");
                    }
                    hql.append(") ");
                }

                if (hasFilters) {
                    for (String key : filters.keySet()) {
                        String paramName = key.replace(".", "_");
                        hql.append(" AND e.").append(key).append(" = :").append(paramName);
                    }
                }

                org.hibernate.query.Query<DiemCong> query = session.createQuery(hql.toString(), DiemCong.class);

                if (hasSearch) {
                    query.setParameter("kw", "%" + keyword.trim() + "%");
                }
                if (hasFilters) {
                    for (java.util.Map.Entry<String, Object> entry : filters.entrySet()) {
                        String paramName = entry.getKey().replace(".", "_");
                        query.setParameter(paramName, entry.getValue());
                    }
                }
                
                query.setFirstResult((pageIndex - 1) * pageSize);
                query.setMaxResults(pageSize);
                return query.list();
            } catch (Exception e) {
                System.err.println("[DiemCongDAO] findPageWithFilters exception: " + e.getMessage());
                throw e;
            }
        }, java.util.concurrent.Executors.newCachedThreadPool());
    }


    @Override
    public java.util.concurrent.CompletableFuture<Long> countWithFiltersAsync(String keyword, java.util.List<String> searchFields, java.util.Map<String, Object> filters) {
        return java.util.concurrent.CompletableFuture.supplyAsync(() -> {
            try (org.hibernate.Session session = HibernateUtil.getSessionFactory().openSession()) {
                StringBuilder hql = new StringBuilder("SELECT COUNT(e) FROM DiemCong e WHERE 1=1 ");
                
                boolean hasSearch = keyword != null && !keyword.trim().isEmpty() && searchFields != null && !searchFields.isEmpty();
                boolean hasFilters = filters != null && !filters.isEmpty();

                if (hasSearch) {
                    hql.append(" AND (");
                    for (int i = 0; i < searchFields.size(); i++) {
                        String field = searchFields.get(i);
                        if (field.startsWith("thiSinh.")) {
                            hql.append("e.").append(field).append(" LIKE :kw");
                        } else {
                            hql.append("e.").append(field).append(" LIKE :kw");
                        }
                        if (i < searchFields.size() - 1) hql.append(" OR ");
                    }
                    hql.append(") ");
                }

                if (hasFilters) {
                    for (String key : filters.keySet()) {
                        String paramName = key.replace(".", "_");
                        hql.append(" AND e.").append(key).append(" = :").append(paramName);
                    }
                }

                org.hibernate.query.Query<Long> query = session.createQuery(hql.toString(), Long.class);
                if (hasSearch) query.setParameter("kw", "%" + keyword.trim() + "%");
                if (hasFilters) {
                    for (java.util.Map.Entry<String, Object> entry : filters.entrySet()) {
                        query.setParameter(entry.getKey().replace(".", "_"), entry.getValue());
                    }
                }
                return query.uniqueResult();
            }
        });
    }

    public static void main(String[] args) {
        System.out.println("Start Async "+ DiemCong.class);
        GenericDAO<DiemCong> dao = new DiemCongDAO();
//        dao.findAll().thenAccept(diemCong -> {
//            if (diemCong !=null) {
//                System.out.println("Thành công! Data: " + diemCong);
//                for (DiemCong dc : diemCong) {
//                    System.out.println(dc.getId());
//                }
//            }
//            else
//                System.out.println("Không tìm thấy data trong "+ DiemCong.class+ ".");
//        }).exceptionally(throwable -> {
//            System.err.println("Lỏ rồi " + throwable.getMessage());
//            return null;
//        }).whenComplete((result, throwable) -> {
//            if (throwable != null) {
//                System.err.println("Lỏ rồi: " + throwable.getMessage());
//            } else {
//                System.out.println("Thành công: " + result);
//            }
//            System.out.println("Đang shutdown pool...");
//            dao.shutdown();
//            HibernateUtil.shutdown();
//        });
        dao.findPage(2).thenAccept(diemCong -> {
            if (diemCong !=null) {
                System.out.println("Thành công! Data: " + diemCong);
                for (DiemCong dc : diemCong) {
                    System.out.println(dc.getId());
                }
            }
        }).exceptionally( throwable -> {
            System.err.println("Lỏ rồi " + throwable.getMessage());
            return null;
        }).whenComplete((result, throwable) -> {
            if (throwable != null) {
                System.err.println("Lỏ rồi: " + throwable.getMessage());
            } else {
                System.out.println("Thành công: " + result);
            }
            System.out.println("Đang shutdown pool...");
            dao.shutdownPool();
            HibernateUtil.shutdown();
        });
        System.out.println("Hàm main continue chạy mà không đợi DB");
    }
}
