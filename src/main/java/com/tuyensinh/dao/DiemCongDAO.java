package com.tuyensinh.dao;

import com.tuyensinh.model.DiemCong;
import com.tuyensinh.util.HibernateUtil;

public class DiemCongDAO extends GenericDAO<DiemCong>{

    public DiemCongDAO(Class<DiemCong> entityClass) {
        super(entityClass);
    }

    public DiemCongDAO() {
        super(DiemCong.class);
    }

    public static void main(String[] args) {
        System.out.println("Start Async "+ DiemCong.class);
        GenericDAO<DiemCong> dao = new DiemCongDAO();
        dao.findAll().thenAccept(diemCong -> {
            if (diemCong !=null) {
                System.out.println("Thành công! Data: " + diemCong);
                for (DiemCong dc : diemCong) {
                    System.out.println(dc.getId());
                }
            }
            else
                System.out.println("Không tìm thấy data trong "+ DiemCong.class+ ".");
        }).exceptionally(throwable -> {
            System.err.println("Lỏ rồi " + throwable.getMessage());
            return null;
        }).whenComplete((result, throwable) -> {
            if (throwable != null) {
                System.err.println("Lỏ rồi: " + throwable.getMessage());
            } else {
                System.out.println("Thành công: " + result);
            }
            System.out.println("Đang shutdown pool...");
            dao.shutdown();
            HibernateUtil.shutdown();
        });

        System.out.println("Hàm main continue chạy mà không đợi DB");
    }
}
