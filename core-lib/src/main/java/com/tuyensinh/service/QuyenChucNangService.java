package com.tuyensinh.service;

import com.tuyensinh.dao.QuyenChucNangDAO;
import com.tuyensinh.model.QuyenChucNang;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class QuyenChucNangService {
    
    private final QuyenChucNangDAO quyenDAO = new QuyenChucNangDAO();

    public Set<String> getMaChucNangCoXem(Integer idNhomQuyen) {
        List<String> listQuyen = quyenDAO.getMaChucNangCoXem(idNhomQuyen);
        return new HashSet<>(listQuyen);
    }

    public List<QuyenChucNang> findByNhomQuyenId(Integer idNhom) {
        return quyenDAO.findByNhomQuyenId(idNhom);
    }

    /**
     * Cập nhật danh sách quyền cho 1 Nhóm: Xóa sạch quyền cũ -> Lưu quyền mới
     */
    public boolean saveQuyenChucNangTheoNhom(Integer idNhom, List<QuyenChucNang> dsQuyenMoi) {
        if (idNhom == null || dsQuyenMoi == null) return false;

        org.hibernate.Transaction tx = null;
        try (org.hibernate.Session session = com.tuyensinh.util.HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();

            // 1. Xóa toàn bộ quyền cũ của nhóm này
            String deleteHql = "DELETE FROM QuyenChucNang q WHERE q.nhomQuyen.id = :idNhom";
            session.createMutationQuery(deleteHql)
                   .setParameter("idNhom", idNhom)
                   .executeUpdate();

            // 2. Insert toàn bộ quyền mới
            for (QuyenChucNang q : dsQuyenMoi) {
                session.persist(q);
            }

            tx.commit();
            return true;
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            System.err.println("[QuyenChucNangService] Lỗi lưu phân quyền: " + e.getMessage());
            return false;
        }
    }

}