package com.tuyensinh.util;
import com.tuyensinh.model.DiemCong;
import lombok.AccessLevel;
import lombok.Getter;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.StatelessSession;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

import java.util.Iterator;
import java.util.List;

public class HibernateUtil {
    @Getter(AccessLevel.PUBLIC)
    private static final SessionFactory sessionFactory = buildSessionFactory();

    private static SessionFactory buildSessionFactory(){
        try{
            return new Configuration().configure().buildSessionFactory();
        } catch (HibernateException ex) {
            System.err.println("Khởi tạo SessionFactory thất bại: " + ex);
            throw new RuntimeException(ex);
        }
    }

    public static void shutdown(){
        if (sessionFactory != null)
            sessionFactory.close();
    }

    public static <T> T createSession(Class<T> tClass){
        if (tClass.equals(StatelessSession.class))
            return tClass.cast(sessionFactory.openStatelessSession());
        else if (tClass.equals(Session.class))
            return tClass.cast(sessionFactory.openSession());
        throw new IllegalArgumentException("Kiểu session không hợp lệ: " + tClass.getName());
    }

    public static void main(String[] args){
        try (Session session = HibernateUtil.createSession(Session.class)){
            session.beginTransaction();

            String hql = "FROM DiemCong";

            List<DiemCong> diemCongList = session.createQuery(hql, DiemCong.class).list();

            Iterator<DiemCong> it = diemCongList.iterator();
            while (it.hasNext()){
                System.out.println(it.next().getDiemTong());
            }
            session.getTransaction().commit();
        }
        HibernateUtil.shutdown();
    }
}