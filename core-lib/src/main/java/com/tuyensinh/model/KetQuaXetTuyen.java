package com.tuyensinh.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "xt_ket_qua_xet_tuyen")
@Data
public class KetQuaXetTuyen {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @OneToOne
    @JoinColumn(name = "id_ho_so")
    private HoSoTuyenSinh hoSo;

    @ManyToOne
    @JoinColumn(name = "id_nganh")
    private Nganh nganh;

    @Column(name = "diem_xet_tuyen")
    private Double diemXetTuyen;

    @Column(name = "nguyen_vong_thu")
    private Integer nguyenVongThu;

    @Column(name = "ma_to_hop")
    private String maToHop;

    @Column(name = "phuong_thuc")
    private String phuongThuc;

    @Column(name = "trang_thai")
    private String trangThai = "TRUNG_TUYEN";

    @Column(name = "xac_nhan")
    private Boolean xacNhan = false;

    @Column(name = "ngay_tao")
    private LocalDateTime ngayTao = LocalDateTime.now();
}