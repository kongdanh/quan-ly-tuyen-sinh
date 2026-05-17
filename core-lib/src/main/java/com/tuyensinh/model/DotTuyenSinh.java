package com.tuyensinh.model;

import jakarta.persistence.*;
import lombok.*;
import java.io.Serializable;
import java.time.LocalDateTime;

@Entity
@Table(name = "xt_dot_tuyen_sinh")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DotTuyenSinh implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "ten_dot", nullable = false)
    private String tenDot;

    @Column(name = "ngay_bat_dau", nullable = false)
    private LocalDateTime ngayBatDau;

    @Column(name = "ngay_ket_thuc", nullable = false)
    private LocalDateTime ngayKetThuc;

    @Column(name = "ngay_cong_bo")
    private LocalDateTime ngayCongBo;

    @Column(name = "trang_thai")
    private String trangThai;

    @Column(name = "ngay_tao")
    private LocalDateTime ngayTao;

    @Column(name = "ma_phuong_thuc", length = 20)
    private String maPhuongThuc = "THPT";

    // debug tí...
    @Override
    public String toString() {
        return "[" + id + "] " + tenDot + " (" + maPhuongThuc + ")";
    }
}