package com.tuyensinh.service;

import com.tuyensinh.dao.BangQuyDoiDAO;
import com.tuyensinh.model.BangQuyDoi;
import java.util.List;

public class TraCuuService {

    private final BangQuyDoiDAO bangQuyDoiDAO = new BangQuyDoiDAO();

    /**
     * Tinh diem quy doi DGNL bang thuat toan noi suy tuyen tinh
     */
    public Double tinhDiemQuyDoiDGNL(double diemThi) {
        if (diemThi == 0) return 0.0;
        List<BangQuyDoi> listPhanVi = bangQuyDoiDAO.getBangQuyDoiByType("DGNL");
        if (listPhanVi == null || listPhanVi.isEmpty()) return null; 
        return noiSuy(diemThi, listPhanVi);
    }

    /**
     * Tinh diem quy doi VSAT tung mon bang thuat toan noi suy tuyen tinh
     */
    public Double tinhDiemQuyDoiVSAT(double diemThi, String maMon) {
        if (diemThi <= 0) return 0.0;
        List<BangQuyDoi> listPhanVi = bangQuyDoiDAO.getBangQuyDoiByTypeAndMon("VSAT", maMon);
        
        if (listPhanVi == null || listPhanVi.isEmpty()) {
            return Math.round((diemThi / 15.0) * 100.0) / 100.0;
        }
        return noiSuy(diemThi, listPhanVi);
    }

    /**
     * Thuat toan noi suy tuyen tinh: tim khoang phan vi chua diem thi,
     * tinh diem quy doi tuong ung theo cong thuc noi suy chuan.
     */
    private Double noiSuy(double diemThi, List<BangQuyDoi> listPhanVi) {
        for (BangQuyDoi current : listPhanVi) {
            Double gocDuoi = current.getDDiema() != null ? current.getDDiema().doubleValue() : 0.0;
            Double gocTren = current.getDDiemb() != null ? current.getDDiemb().doubleValue() : 0.0;
            Double qdDuoi = current.getDDiemc() != null ? current.getDDiemc().doubleValue() : 0.0;
            Double qdTren = current.getDDiemd() != null ? current.getDDiemd().doubleValue() : 0.0;

            if (diemThi >= gocDuoi && diemThi <= gocTren) {
                if (gocTren.equals(gocDuoi)) return qdDuoi;

                double phanTram = (diemThi - gocDuoi) / (gocTren - gocDuoi);
                double diemQuyDoi = qdDuoi + (phanTram * (qdTren - qdDuoi));
                return Math.round(diemQuyDoi * 100.0) / 100.0;
            }
        }

        BangQuyDoi lastRow = listPhanVi.get(listPhanVi.size() - 1);
        Double maxGoc = lastRow.getDDiemb() != null ? lastRow.getDDiemb().doubleValue() : 0.0;
        if (diemThi > maxGoc) {
            return lastRow.getDDiemd() != null ? lastRow.getDDiemd().doubleValue() : 0.0;
        }

        return 0.0;
    }

    /**
     * Tinh diem uu tien theo khu vuc va doi tuong.
     * Ap dung cong thuc giam dan khi tong diem >= 22.5
     */
    public Double tinhDiemUuTien(double tongDiem3Mon, String maKhuVuc, String maDoiTuong) {
        double diemKV = switch (maKhuVuc) {
            case "1" -> 0.75;
            case "2NT" -> 0.5;
            case "2" -> 0.25;
            default -> 0.0; 
        };

        double diemDT = switch (maDoiTuong) {
            case "01", "02", "03", "04", "05", "06", "07" -> 2.0;
            default -> 0.0; 
        };

        double tongUuTien = diemKV + diemDT;
        if (tongDiem3Mon >= 22.5) {
            tongUuTien = tongUuTien * ((30.0 - tongDiem3Mon) / 7.5);
        }
        return Math.round(tongUuTien * 100.0) / 100.0;
    }
}