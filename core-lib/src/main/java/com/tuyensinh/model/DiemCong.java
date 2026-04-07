package com.tuyensinh.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "xt_diemcongxetuyen")
public class DiemCong {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "iddiemcong", nullable = false)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ts_cccd", nullable = false, referencedColumnName = "cccd")
    private ThiSinh thiSinh;

    @Column(name = "manganh", length = 45)
    private String manganh;

    @Column(name = "matohop", length = 45)
    private String matohop;

    @Column(name = "phuongthuc", length = 10)
    private String phuongthuc;

    @Column(name = "diemCC", precision = 4, scale = 2)
    private BigDecimal diemCC;

    @Column(name = "diemUtxt", precision = 4, scale = 2)
    private BigDecimal diemUtxt;

    @Column(name = "diemTong", precision = 4, scale = 2)
    private BigDecimal diemTong;

    @Column(name = "ghichu", length = 200)
    private String ghichu;

    // ts_cccd_manganh_matohop
    @Column(name = "dc_keys", length = 100, unique = true)
    private String dcKeys;
}