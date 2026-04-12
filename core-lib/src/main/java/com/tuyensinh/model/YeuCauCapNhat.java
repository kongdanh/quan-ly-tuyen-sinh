package com.tuyensinh.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "xt_yeucau_capnhat")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class YeuCauCapNhat {
    @Id 
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "cccd", nullable = false)
    private String cccd;

    @Column(name = "dien_thoai")
    private String dienThoai;

    @Column(name = "email")
    private String email;

    @Column(name = "noi_sinh")
    private String noiSinh;

    @Column(name = "khu_vuc")
    private String khuVuc;

    @Column(name = "doi_tuong")
    private String doiTuong;

    @Column(name = "minh_chung_url")
    private String minhChungUrl;

    @Column(name = "trang_thai")
    private String trangThai;

    @Column(name = "ngay_tao", insertable = false, updatable = false)
    private LocalDateTime ngayTao;
}