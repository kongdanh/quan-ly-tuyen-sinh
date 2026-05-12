package com.tuyensinh.service;

import com.tuyensinh.dao.XetTuyenDAO;
import com.tuyensinh.model.KetQuaXetTuyen;
import com.tuyensinh.model.NguyenVong;
import com.tuyensinh.util.HibernateUtil;
import com.tuyensinh.util.SystemLogger;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class XetTuyenService {
    private final XetTuyenDAO xetTuyenDAO = new XetTuyenDAO();

    /**
     * Chay thuat toan xet tuyen cho 1 dot.
     * Duyet tung thi sinh theo thu tu nguyen vong:
     *   - So diem xet tuyen voi diem chuan cua nganh
     *   - Neu dat: ghi nhan trung tuyen, huy cac NV con lai
     *   - Neu khong dat: danh dau rot
     */
    public void chayThuatToanXetTuyen(Integer idDot) {
        System.out.println("[XetTuyenService] Bat dau chay thuat toan xet tuyen cho Dot ID=" + idDot);
        SystemLogger.log(null, "System", "Bắt đầu chạy thuật toán xét tuyển cho Đợt ID=" + idDot, true);
        
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();

            session.createNativeMutationQuery(
                "DELETE FROM xt_ket_qua_xet_tuyen WHERE id_ho_so IN (SELECT id FROM xt_ho_so_tuyen_sinh WHERE id_dot_tuyen_sinh = :idDot)")
                   .setParameter("idDot", idDot)
                   .executeUpdate();

            Query<NguyenVong> query = session.createQuery(
                "SELECT nv FROM NguyenVong nv JOIN FETCH nv.nganh JOIN FETCH nv.hoSoTuyenSinh hs JOIN FETCH nv.thiSinh t WHERE hs.dotTuyenSinh.id = :idDot AND hs.trangThai = 'HOP_LE' ORDER BY t.cccd, nv.nvTt ASC", 
                NguyenVong.class);
            query.setParameter("idDot", idDot);
            List<NguyenVong> listNV = query.getResultList();
            
            System.out.println("[XetTuyenService] Tim thay " + listNV.size() + " nguyen vong can xet");

            Map<String, List<NguyenVong>> mapThiSinh = listNV.stream()
                    .collect(Collectors.groupingBy(nv -> nv.getThiSinh().getCccd()));

            int soTrungTuyen = 0;
            int soRot = 0;

            for (Map.Entry<String, List<NguyenVong>> entry : mapThiSinh.entrySet()) {
                boolean daDau = false;
                for (NguyenVong nv : entry.getValue()) {
                    if (daDau) {
                        nv.setNvKetqua("HUY");
                        session.merge(nv);
                        continue;
                    }

                    BigDecimal bdDiemChuan = (BigDecimal) session.createQuery(
                        "SELECT dc.diemChuan FROM DiemChuanDot dc " +
                        "WHERE dc.dotTuyenSinh.id = :idDot AND dc.nganhToHop.manganh = :maNganh AND dc.nganhToHop.matohop = :maTh")
                        .setParameter("idDot", idDot)
                        .setParameter("maNganh", nv.getNganh().getManganh())
                        .setParameter("maTh", nv.getTtThm())
                        .uniqueResult();

                    Double diemChuan = (bdDiemChuan != null) ? bdDiemChuan.doubleValue() : 999.0;
                    Double diemThiSinh = nv.getDiemXettuyen() != null ? nv.getDiemXettuyen() : 0.0;

                    if (diemThiSinh >= diemChuan) {
                        nv.setNvKetqua("DAU");
                        session.merge(nv);
                        daDau = true;
                        soTrungTuyen++;

                        KetQuaXetTuyen kq = new KetQuaXetTuyen();
                        kq.setHoSo(nv.getHoSoTuyenSinh());
                        kq.setNganh(nv.getNganh());
                        kq.setDiemXetTuyen(diemThiSinh);
                        kq.setNguyenVongThu(nv.getNvTt());
                        kq.setMaToHop(nv.getTtThm());
                        kq.setPhuongThuc(nv.getTtPhuongthuc());
                        kq.setTrangThai("TRUNG_TUYEN");
                        session.persist(kq);
                    } else {
                        nv.setNvKetqua("ROT");
                        session.merge(nv);
                        soRot++;
                    }
                }
            }
            transaction.commit();
            
            System.out.println("[XetTuyenService] Hoan thanh xet tuyen Dot ID=" + idDot + ": " + soTrungTuyen + " trung tuyen, " + soRot + " rot");
            SystemLogger.log(null, "System", "Hoàn thành xét tuyển Đợt ID=" + idDot + ": " + soTrungTuyen + " trúng tuyển, " + soRot + " rớt", true);
            
        } catch (Exception e) {
            if (transaction != null) transaction.rollback();
            System.err.println("[XetTuyenService] Loi chay thuat toan xet tuyen: " + e.getMessage());
            e.printStackTrace();
            SystemLogger.log(null, "System", "Lỗi chạy thuật toán xét tuyển Đợt ID=" + idDot + ": " + e.getMessage(), false);
            throw new RuntimeException("Loi chay thuat toan: " + e.getMessage());
        }
    }

    /**
     * Tim ket qua xet tuyen cua thi sinh theo CCCD
     */
    public KetQuaXetTuyen timKetQuaTheoCccd(String cccd) {
        System.out.println("[XetTuyenService] Tim ket qua xet tuyen cho CCCD=" + cccd);
        return xetTuyenDAO.timKetQuaTheoCccd(cccd);
    }
}