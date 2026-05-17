package com.tuyensinh.service;

import com.tuyensinh.dao.QuyenChucNangDAO;
import com.tuyensinh.model.QuyenChucNang;
import com.tuyensinh.util.SystemLogger;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class QuyenChucNangService {
    
    private final QuyenChucNangDAO quyenDAO = new QuyenChucNangDAO();

    /**
     * Lay danh sach ma chuc nang co quyen xem theo nhom quyen
     */
    public Set<String> getMaChucNangCoXem(Integer idNhomQuyen) {
        List<String> listQuyen = quyenDAO.getMaChucNangCoXem(idNhomQuyen);
        return new HashSet<>(listQuyen);
    }

    /**
     * Lay danh sach quyen chuc nang theo nhom quyen
     */
    public List<QuyenChucNang> findByNhomQuyenId(Integer idNhom) {
        return quyenDAO.findByNhomQuyenId(idNhom);
    }

    /**
     * Cap nhat danh sach quyen cho 1 Nhom: Xoa sach quyen cu -> Luu quyen moi
     */
    public boolean saveQuyenChucNangTheoNhom(Integer idNhom, List<QuyenChucNang> dsQuyenMoi) {
        if (idNhom == null || dsQuyenMoi == null) return false;

        System.out.println("[QuyenChucNangService] Cap nhat quyen cho nhom ID=" + idNhom + " (" + dsQuyenMoi.size() + " quyen)");

        org.hibernate.Transaction tx = null;
        try (org.hibernate.Session session = com.tuyensinh.util.HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();

            String deleteHql = "DELETE FROM QuyenChucNang q WHERE q.nhomQuyen.id = :idNhom";
            session.createMutationQuery(deleteHql)
                   .setParameter("idNhom", idNhom)
                   .executeUpdate();

            for (QuyenChucNang q : dsQuyenMoi) {
                session.persist(q);
            }

            tx.commit();
            System.out.println("[QuyenChucNangService] Cap nhat quyen thanh cong cho nhom ID=" + idNhom);
            SystemLogger.log(null, "System", "Cập nhật phân quyền cho nhóm ID=" + idNhom + " (" + dsQuyenMoi.size() + " quyền)", true);
            return true;
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            System.err.println("[QuyenChucNangService] Loi luu phan quyen: " + e.getMessage());
            SystemLogger.log(null, "System", "Lỗi cập nhật phân quyền nhóm ID=" + idNhom + ": " + e.getMessage(), false);
            return false;
        }
    }

}