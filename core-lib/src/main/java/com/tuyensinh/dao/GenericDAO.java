package com.tuyensinh.dao;

import com.tuyensinh.util.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.StatelessSession;
import org.hibernate.Transaction;
import org.hibernate.query.Query;

import java.io.Serializable;
import java.util.List;
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
}