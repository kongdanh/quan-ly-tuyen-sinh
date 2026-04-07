package com.tuyensinh.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;

import java.math.BigDecimal;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "xt_diemthixettuyen")
public class DiemThiXetTuyen {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "iddiemthi", nullable = false)
    private Integer id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "cccd", nullable = false, referencedColumnName = "cccd")
    private ThiSinh thiSinh;

    @Column(name = "sobaodanh", length = 45)
    private String sobaodanh;

    @Column(name = "d_phuongthuc", length = 10)
    private String dPhuongthuc;

    @Column(name = "`TO`", precision = 8, scale = 2)
    private BigDecimal to;

    @Column(name = "LI", precision = 8, scale = 2)
    private BigDecimal li;

    @Column(name = "HO", precision = 8, scale = 2)
    private BigDecimal ho;

    @Column(name = "SI", precision = 8, scale = 2)
    private BigDecimal si;

    @Column(name = "SU", precision = 8, scale = 2)
    private BigDecimal su;

    @Column(name = "DI", precision = 8, scale = 2)
    private BigDecimal di;

    @Column(name = "VA", precision = 8, scale = 2)
    private BigDecimal va;

    @Column(name = "N1_THI", precision = 8, scale = 2)
    private BigDecimal n1Thi;

    @Column(name = "N1_CC", precision = 8, scale = 2)
    private BigDecimal n1Cc;

    @Column(name = "CNCN", precision = 8, scale = 2)
    private BigDecimal cncn;

    @Column(name = "CNNN", precision = 8, scale = 2)
    private BigDecimal cnnn;

    @Column(name = "TI", precision = 8, scale = 2)
    private BigDecimal ti;

    @Column(name = "KTPL", precision = 8, scale = 2)
    private BigDecimal ktpl;

    @Column(name = "NL1", precision = 8, scale = 2)
    private BigDecimal nl1;

    @Column(name = "NK1", precision = 8, scale = 2)
    private BigDecimal nk1;

    @Column(name = "NK2", precision = 8, scale = 2)
    private BigDecimal nk2;
}