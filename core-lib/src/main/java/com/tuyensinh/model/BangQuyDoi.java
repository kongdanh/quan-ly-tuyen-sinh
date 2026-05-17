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
@Table(name = "xt_bangquydoi")
public class BangQuyDoi {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idqd;

    @Column(name = "d_phuongthuc", length = 45)
    private String dPhuongthuc;

    @Column(name = "d_tohop", length = 45)
    private String dTohop;

    @Column(name = "d_mon", length = 45)
    private String dMon;

    @Column(name = "d_diema", precision = 6, scale = 2)
    private BigDecimal dDiema;

    @Column(name = "d_diemb", precision = 6, scale = 2)
    private BigDecimal dDiemb;

    @Column(name = "d_diemc", precision = 6, scale = 2)
    private BigDecimal dDiemc;

    @Column(name = "d_diemd", precision = 6, scale = 2)
    private BigDecimal dDiemd;

    @Column(name = "d_maquydoi", length = 45, unique = true)
    private String dMaquydoi;

    @Column(name = "d_phanvi", length = 45)
    private String dPhanvi;
}