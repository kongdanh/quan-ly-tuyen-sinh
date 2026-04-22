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
@Table(name = "xt_nganh")
public class Nganh {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idnganh", nullable = false)
    private Integer id;

    @Column(name = "manganh", nullable = false, length = 20)
    private String manganh;

    @Column(name = "tennganh", nullable = false, length = 200)
    private String tennganh;

    @Column(name = "n_tohopgoc", length = 10)
    private String nTohopgoc;

    @Column(name = "n_chitieu", nullable = false)
    private Integer nChitieu;

    @Column(name = "n_diemsan", precision = 10, scale = 2)
    private BigDecimal nDiemsan;

    @Column(name = "n_diemtrungtuyen", precision = 10, scale = 2)
    private BigDecimal nDiemtrungtuyen;

    @Column(name = "n_tuyenthang", length = 1)
    private String nTuyenthang;

    @Column(name = "n_dgnl", length = 1)
    private String nDgnl;

    @Column(name = "n_thpt", length = 1)
    private String nThpt;

    @Column(name = "n_vsat", length = 1)
    private String nVsat;

    @Column(name = "sl_xtt")
    private Integer slXtt;

    @Column(name = "sl_dgnl")
    private Integer slDgnl;

    @Column(name = "sl_vsat")
    private Integer slVsat;

    @Column(name = "sl_thpt", length = 45)
    private String slThpt;

    /** Tổng số thí sinh đã đăng ký — ánh xạ cột sl_dadangky (thêm mới) */
    @Column(name = "sl_dadangky", nullable = false)
    private Integer slDadangky = 0;

    @Override
    public String toString() {
        return manganh + " - " + tennganh;
    }
}