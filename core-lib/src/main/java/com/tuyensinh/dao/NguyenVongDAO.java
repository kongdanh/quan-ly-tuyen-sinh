package com.tuyensinh.dao;

import com.tuyensinh.model.NganhToHop;
import com.tuyensinh.model.NguyenVong;
import com.tuyensinh.util.HibernateUtil;
import org.hibernate.Session;

import java.util.Collections;
import java.util.List;

public class NguyenVongDAO extends GenericDAO<NguyenVong> {

    public NguyenVongDAO() {
        super(NguyenVong.class);
    }

    /**
     * Trả về danh sách nguyện vọng của thí sinh (kèm thông tin ngành).
     *
     * @param cccd Căn cước công dân của thí sinh
     */
    public List<NguyenVong> findByCccd(String cccd) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            String hql = "SELECT nv FROM NguyenVong nv JOIN FETCH nv.nganh WHERE nv.thiSinh.cccd = :cccd ORDER BY nv.nvTt ASC";
            return session.createQuery(hql, NguyenVong.class)
                    .setParameter("cccd", cccd)
                    .list();
        }
    }

    /**
     * JOIN 3 bảng: xt_nguyenvongxettuyen (nv) → xt_nganh_tohop (nth) → xt_tohop_monthi (th).
     *
     * <p>Trả về tất cả các tổ hợp xét tuyển mà thí sinh đã đăng ký.
     * Mỗi {@link NganhToHop} đã được FETCH kèm {@code Nganh} và {@code ToHopMon}.
     *
     * <p>Dùng trong {@code XetTuyenService#processIELTSImport} để duyệt qua
     * từng tổ hợp và quyết định UPDATE N1_CC hay UPSERT vào xt_diemcongxetuyen.
     *
     * @param cccd Căn cước công dân của thí sinh
     * @return Danh sách {@link NganhToHop} tương ứng với các ngành/tổ hợp đăng ký;
     *         trả về danh sách rỗng nếu không tìm thấy hoặc xảy ra lỗi.
     */
    public List<NganhToHop> findNganhToHopByCccd(String cccd) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            /*
             * HQL tương đương SQL:
             *   SELECT nth.*
             *   FROM xt_nguyenvongxettuyen nv
             *   JOIN xt_nganh_tohop    nth ON nth.manganh  = nv.nv_manganh
             *   JOIN xt_tohop_monthi   th  ON th.matohop   = nth.matohop
             *   WHERE nv.nn_cccd = :cccd
             *
             * Dùng DISTINCT để tránh nhân bản khi 1 ngành có nhiều nguyện vọng.
             */
            String hql = "SELECT DISTINCT nth " +
                         "FROM NguyenVong nv " +
                         "JOIN nv.nganh n " +
                         "JOIN NganhToHop nth ON nth.nganh = n " +
                         "JOIN FETCH nth.nganh " +
                         "JOIN FETCH nth.toHopMon " +
                         "WHERE nv.thiSinh.cccd = :cccd";

            return session.createQuery(hql, NganhToHop.class)
                    .setParameter("cccd", cccd)
                    .list();
        } catch (Exception e) {
            System.err.println("[NguyenVongDAO] Lỗi findNganhToHopByCccd cho CCCD=" + cccd + ": " + e.getMessage());
            return Collections.emptyList();
        }
    }
}