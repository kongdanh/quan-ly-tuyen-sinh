package com.tuyensinh.dao;

import com.tuyensinh.model.KetQuaXetTuyen;
import com.tuyensinh.model.HoSoTuyenSinh;
import com.tuyensinh.util.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;
import java.util.List;

public class XetTuyenDAO extends GenericDAO<KetQuaXetTuyen> {
    
    public XetTuyenDAO() {
        super(KetQuaXetTuyen.class);
    }

    // Hàm này dùng để xóa kết quả xét tuyển cũ của đợt đó trước khi chạy mới
    public void clearKetQuaCu(Integer idDot) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Transaction tx = session.beginTransaction();
            session.createQuery("DELETE FROM KetQuaXetTuyen k WHERE k.hoSo.dotTuyenSinh.id = :idDot")
                   .setParameter("idDot", idDot)
                   .executeUpdate();
            tx.commit();
        }
    }

    // Hàm này lưu kết quả vào DB
    public void saveKetQua(KetQuaXetTuyen kq) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Transaction tx = session.beginTransaction();
            session.save(kq); 
            tx.commit();
        }
    }
    
    public List<HoSoTuyenSinh> getHoSoHopLe(Integer idDot) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            // Sử dụng JOIN FETCH để gom sẵn toàn bộ Dữ liệu (Thí sinh, Điểm, Nguyện vọng, Ngành) vào bộ nhớ 
            String hql = "SELECT DISTINCT h FROM HoSoTuyenSinh h " +
                         "JOIN FETCH h.thiSinh t " +
                         "LEFT JOIN FETCH t.diemThiXetTuyen " +
                         "LEFT JOIN FETCH h.nguyenVongs nv " + 
                         "LEFT JOIN FETCH nv.nganhToHop nth " +
                         "LEFT JOIN FETCH nth.nganh " +
                         "WHERE h.dotTuyenSinh.id = :idDot AND h.trangThai = 'HOP_LE'";
                         
            return session.createQuery(hql, HoSoTuyenSinh.class)
                .setParameter("idDot", idDot)
                .list();
        } catch (Exception e) {
            e.printStackTrace();
            return java.util.Collections.emptyList();
        }
    }

    // Thêm vào file XetTuyenDAO.java
    public List<KetQuaXetTuyen> getKetQuaTheoDot(Integer idDot) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            String hql = "SELECT k FROM KetQuaXetTuyen k " +
                        "JOIN FETCH k.hoSo h " +
                        "JOIN FETCH k.nganh n " +
                        "JOIN FETCH h.thiSinh t " +
                        "WHERE h.dotTuyenSinh.id = :idDot " +
                        "ORDER BY k.diemXetTuyen DESC";
            return session.createQuery(hql, KetQuaXetTuyen.class)
                        .setParameter("idDot", idDot)
                        .list();
        }
    }

    public KetQuaXetTuyen timKetQuaTheoCccd(String cccd) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            String hql = "SELECT k FROM KetQuaXetTuyen k " +
                         "JOIN FETCH k.hoSo h " +
                         "JOIN FETCH k.nganh n " +
                         "WHERE h.thiSinh.cccd = :cccd";
            return session.createQuery(hql, KetQuaXetTuyen.class)
                          .setParameter("cccd", cccd)
                          .uniqueResult();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    // Thêm hàm lấy kết quả theo ID Hồ sơ
    public List<KetQuaXetTuyen> getKetQuaTheoHoSo(Integer idHoSo) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            String hql = "SELECT k FROM KetQuaXetTuyen k " +
                         "JOIN FETCH k.hoSo h " +
                         "JOIN FETCH k.nganh n " +
                         "JOIN FETCH h.thiSinh t " +
                         "WHERE h.id = :idHoSo";
            return session.createQuery(hql, KetQuaXetTuyen.class)
                        .setParameter("idHoSo", idHoSo)
                        .list();
        } catch (Exception e) {
            e.printStackTrace();
            return java.util.Collections.emptyList();
        }
    }

}