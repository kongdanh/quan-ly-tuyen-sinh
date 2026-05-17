package com.tuyensinh.dao;

import com.tuyensinh.util.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.StatelessSession;
import org.hibernate.Transaction;
import org.hibernate.query.Query;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public abstract class GenericDAO<T> {
    protected final Class<T> entityClass;
    
    private static final ExecutorService dbThreadPool = Executors.newFixedThreadPool(20);
    private static final int BATCH_SIZE = 100;

    public GenericDAO(Class<T> entityClass) {
        this.entityClass = entityClass;
    }

    public void save(T entity) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Transaction tx = session.beginTransaction();
            session.persist(entity);
            tx.commit();
        }
    }

    public void update(T entity) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Transaction tx = session.beginTransaction();
            session.merge(entity);
            tx.commit();
        }
    }

    public void delete(T entity) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Transaction tx = session.beginTransaction();
            T managedEntity = session.merge(entity);
            if (managedEntity != null) {
                session.remove(managedEntity);
            }
            tx.commit();
        }
    }

    // find by id
    public T findById(Serializable id) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.get(entityClass, id);
        }
    }

    // find all
    public List<T> findByProperty(String propertyName, Object value) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            String hql = "FROM " + entityClass.getSimpleName() + " e WHERE e." + propertyName + " = :value";
            return session.createQuery(hql, entityClass)
                        .setParameter("value", value)
                        .list();
        }
    }

    public List<T> findAllSync() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery("FROM " + entityClass.getSimpleName(), entityClass).list();
        }
    }

    /**
     * Batch insert
     */
    public CompletableFuture<Void> saveOrUpdateAll(List<T> entities) {
        if (entities == null || entities.isEmpty()) {
            return CompletableFuture.completedFuture(null);
        }
        return CompletableFuture.runAsync(() -> {
            Transaction tx = null;
            try (StatelessSession statelessSession = HibernateUtil.getSessionFactory().openStatelessSession()) {
                tx = statelessSession.beginTransaction();
                int count = 0;
                for (T entity : entities) {
                    statelessSession.insert(entity);
                    if (++count % BATCH_SIZE == 0) {
                        System.out.println("[Batch] Đã xử lý: " + count + " thực thể " + entityClass.getSimpleName());
                    }
                }
                tx.commit();
                System.out.println("==> Hoàn thành Async Batch Insert: " + count + " dòng.");
            } catch (Exception e) {
                if (tx != null) tx.rollback();
                throw new RuntimeException("Lỗi Async Batch Insert: " + e.getMessage(), e);
            }
        }, dbThreadPool);
    }

    public CompletableFuture<T> findByIdAsync(Serializable id) {
        return CompletableFuture.supplyAsync(() -> findById(id), dbThreadPool);
    }

    public CompletableFuture<List<T>> findAllAsync() {
        return CompletableFuture.supplyAsync(this::findAllSync, dbThreadPool);
    }

    /**
     * Pagination
     */
    public CompletableFuture<List<T>> findPage(int pageIndex, int pageSize) {
        return CompletableFuture.supplyAsync(() -> {
            try (Session session = HibernateUtil.getSessionFactory().openSession()) {
                String hql = "FROM " + entityClass.getSimpleName();
                Query<T> query = session.createQuery(hql, entityClass);
                query.setFirstResult((pageIndex - 1) * pageSize);
                query.setMaxResults(pageSize);
                return query.list();
            }
        }, dbThreadPool);
    }

    public CompletableFuture<List<T>> findPage(int pageIndex) {
        return findPage(pageIndex, 20);
    }
    
    public long count() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            String hql = "SELECT count(e) FROM " + entityClass.getSimpleName() + " e";
            return session.createQuery(hql, Long.class).uniqueResult();
        }
    }

    public void shutdownPool() {
        if (!dbThreadPool.isShutdown()) {
            dbThreadPool.shutdown();
        }
    }

    /**
     * Tìm kiếm kết hợp nhiều trường và nhiều bộ lọc (Async)
     * @param keyword Từ khóa tìm kiếm (dùng LIKE)
     * @param searchFields Danh sách cột để tìm kiếm từ khóa
     * @param filters Danh sách các bộ lọc chính xác (VD: "gioiTinh" -> "Nam")
     */
    public CompletableFuture<List<T>> findPageWithFilters(String keyword, List<String> searchFields, Map<String, Object> filters, int pageIndex, int pageSize) {
        return CompletableFuture.supplyAsync(() -> {
            try (Session session = HibernateUtil.getSessionFactory().openSession()) {
                StringBuilder hql = new StringBuilder("FROM " + entityClass.getSimpleName() + " e WHERE 1=1 ");
                
                boolean hasSearch = keyword != null && !keyword.trim().isEmpty() && searchFields != null && !searchFields.isEmpty();
                boolean hasFilters = filters != null && !filters.isEmpty();

                // 1. Gắn điều kiện Tìm kiếm
                if (hasSearch) {
                    hql.append(" AND (");
                    for (int i = 0; i < searchFields.size(); i++) {
                        hql.append("e.").append(searchFields.get(i)).append(" LIKE :kw");
                        if (i < searchFields.size() - 1) hql.append(" OR ");
                    }
                    hql.append(") ");
                }

                // 2. Gắn điều kiện Lọc (Filter)
                if (hasFilters) {
                    for (String key : filters.keySet()) {
                        String paramName = key.replace(".", "_");
                        hql.append(" AND e.").append(key).append(" = :").append(paramName);
                    }
                }

                String hqlString = hql.toString();
                System.out.println("[GenericDAO] HQL: " + hqlString);
                System.out.println("   Filters: " + filters);
                System.out.println("   Page: " + pageIndex + ", Size: " + pageSize);
                
                Query<T> query = session.createQuery(hqlString, entityClass);

                // 3. Truyền giá trị vào tham số
                if (hasSearch) {
                    query.setParameter("kw", "%" + keyword.trim() + "%");
                }
                if (hasFilters) {
                    for (Map.Entry<String, Object> entry : filters.entrySet()) {
                        String paramName = entry.getKey().replace(".", "_");
                        query.setParameter(paramName, entry.getValue());
                    }
                }
                
                query.setFirstResult((pageIndex - 1) * pageSize);
                query.setMaxResults(pageSize);
                List<T> result = query.list();
                System.out.println("[GenericDAO] Query returned " + result.size() + " results for " + entityClass.getSimpleName());
                return result;
            } catch (Exception e) {
                System.err.println("[GenericDAO] findPageWithFilters exception: " + e.getMessage());
                e.printStackTrace();
                throw e;
            }
        }, dbThreadPool);
    }

    /**
     * Đếm tổng số bản ghi khớp với từ khóa tìm kiếm và bộ lọc (Async)
     */
    public CompletableFuture<Long> countWithFiltersAsync(String keyword, List<String> searchFields, Map<String, Object> filters) {
        return CompletableFuture.supplyAsync(() -> {
            try (Session session = HibernateUtil.getSessionFactory().openSession()) {
                StringBuilder hql = new StringBuilder("SELECT count(e) FROM " + entityClass.getSimpleName() + " e WHERE 1=1 ");
                
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

                String hqlString = hql.toString();
                System.out.println("[GenericDAO] COUNT HQL: " + hqlString);
                System.out.println("   Filters: " + filters);
                
                Query<Long> query = session.createQuery(hqlString, Long.class);

                if (hasSearch) {
                    query.setParameter("kw", "%" + keyword.trim() + "%");
                }
                if (hasFilters) {
                    for (Map.Entry<String, Object> entry : filters.entrySet()) {
                        String paramName = entry.getKey().replace(".", "_");
                        query.setParameter(paramName, entry.getValue());
                    }
                }
                
                Long count = query.uniqueResult();
                System.out.println("[GenericDAO] COUNT returned " + count + " total records for " + entityClass.getSimpleName());
                return count != null ? count : 0L;
            } catch (Exception e) {
                System.err.println("[GenericDAO] countWithFiltersAsync exception: " + e.getMessage());
                e.printStackTrace();
                throw e;
            }
        }, dbThreadPool);
    }

    /**
     * Xóa thực thể theo ID (Async)
     */
    public CompletableFuture<Boolean> deleteByIdAsync(Serializable id) {
        return CompletableFuture.supplyAsync(() -> {
            Transaction tx = null;
            try (Session session = HibernateUtil.getSessionFactory().openSession()) {
                tx = session.beginTransaction();
                T entity = session.get(entityClass, id);
                if (entity != null) {
                    session.remove(entity);
                    tx.commit();
                    return true;
                }
                return false;
            } catch (Exception e) {
                if (tx != null) tx.rollback();
                throw new RuntimeException("Lỗi khi xóa ID " + id + ": " + e.getMessage());
            }
        }, dbThreadPool);
    }
}