package com.tuyensinh.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.io.Serializable;
import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "xt_ho_so_tuyen_sinh")
public class HoSoTuyenSinh implements Serializable {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_thi_sinh", referencedColumnName = "idthisinh", nullable = false)
    private ThiSinh thiSinh;

    // Nối với bảng Đợt Tuyển Sinh
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_dot_tuyen_sinh", nullable = false)
    private DotTuyenSinh dotTuyenSinh;

    @Column(name = "ma_ho_so", nullable = false, unique = true)
    private String maHoSo;

    @Column(name = "tong_diem_xet_tuyen")
    private Double tongDiemXetTuyen;

    @Column(name = "trang_thai")
    private String trangThai;

    @Column(name = "ngay_nop")
    private LocalDateTime ngayNop;
}