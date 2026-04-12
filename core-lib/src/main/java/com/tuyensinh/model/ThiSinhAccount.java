package com.tuyensinh.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@AllArgsConstructor 
@NoArgsConstructor 
@Getter 
@Setter
@Entity
@Table(name = "xt_thisinh_account")
public class ThiSinhAccount {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cccd", referencedColumnName = "cccd", nullable = false)
    private ThiSinh thiSinh;

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @Column(name = "trang_thai", nullable = false, length = 20)
    private String trangThai = "HOAT_DONG";

    @Column(name = "lan_dang_nhap_cuoi")
    private LocalDateTime lanDangNhapCuoi;

    @Column(name = "ngay_tao", nullable = false, updatable = false)
    private LocalDateTime ngayTao;

    @PrePersist
    protected void onCreate() {
        if (ngayTao == null) ngayTao = LocalDateTime.now();
        if (trangThai == null) trangThai = "HOAT_DONG";
    }

    public String getCccd() {
        return thiSinh != null ? thiSinh.getCccd() : null;
    }
}