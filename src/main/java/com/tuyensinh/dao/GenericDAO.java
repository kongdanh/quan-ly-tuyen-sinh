package com.tuyensinh.dao;

import com.tuyensinh.util.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public abstract class GenericDAO<T> {
    public final Class<T> entityClass;
    private static final ExecutorService dbThreadPool = Executors.newFixedThreadPool(10);

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
