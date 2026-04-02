package com.tuyensinh.dao;

import com.tuyensinh.util.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.StatelessSession;
import org.hibernate.Transaction;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public abstract class GenericDAO<T> {
    public final Class<T> entityClass;
    private static final ExecutorService dbThreadPool = Executors.newFixedThreadPool(10);
    private static final int BATCH_SIZE = 500;

    public GenericDAO(Class<T> entityClass){
        this.entityClass = entityClass;
    }

    public void saveOrUpdate(T entity){
        try(Session session = HibernateUtil.createSession(Session.class)){
            Transaction tx = session.beginTransaction();
            entity = session.merge(entity);
            tx.commit();
        }
    }

    /**
     * Batch insert/update nhiều entities
     */
    public CompletableFuture<Void> saveOrUpdateAll(List<T> entities){
        if (entities == null || entities.isEmpty()) {
            return CompletableFuture.completedFuture(null);
        }
        return CompletableFuture.runAsync(() -> {
            StatelessSession statelessSession = null;
            Transaction tx = null;

            try{
                statelessSession = HibernateUtil.createSession(StatelessSession.class);
                tx = statelessSession.beginTransaction();
                int count = 0;
                for (T entity : entities){
                    statelessSession.insert(entity);
                    count++;

                    if (count % BATCH_SIZE == 0)
                        // Auto đẩy xuống hàng chờ (queue) của database
                        // Khi dùng session bình thường thì cần flush (đẩy) và clear (dọn RAM)
                        System.out.println(Thread.currentThread().getName() + " Đã insert " + count + " entities...");                }
                tx.commit();
                System.out.println("==> Hoàn thành Async Insert: " + count + " entities trên thread " + Thread.currentThread().getName());
            }catch (Exception e) {
                if (tx != null)
                    tx.rollback();
                System.err.println("Lỗi khi chạy Async Insert: " + e.getMessage());
                throw new RuntimeException(e);
            }finally {
                if (statelessSession != null)
                    statelessSession.close();
            }
        }, dbThreadPool);
    }

    public CompletableFuture<T> findById(Object id){
        return CompletableFuture.supplyAsync(() -> {
            try(Session session = HibernateUtil.createSession(Session.class)){
                return session.get(entityClass, (java.io.Serializable) id);
            }
        }, dbThreadPool);
    }

    public CompletableFuture<List<T>> findAll(){
        return CompletableFuture.supplyAsync(()->{
            try(Session session = HibernateUtil.createSession(Session.class)){
                return session.createQuery("FROM " + entityClass.getName(), entityClass)
                        .list();
            }
        }, dbThreadPool);
    }

    public void delete(T entity){
        try(Session session = HibernateUtil.createSession(Session.class)){
            Transaction tx = session.beginTransaction();
            T managedEntity = session.merge(entity);
            if (managedEntity != null)
                session.remove(managedEntity);
            tx.commit();
        }
    }

    public void shutdown(){
        dbThreadPool.shutdown();
    }
}
