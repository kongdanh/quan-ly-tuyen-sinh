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
@Table(name = "xt_nguyenvongxettuyen")
public class NguyenVong {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idnv", nullable = false)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "nn_cccd", nullable = false, referencedColumnName = "cccd")
    private ThiSinh thiSinh;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "nv_manganh", nullable = false, referencedColumnName = "manganh")
    private Nganh nganh;

    @Column(name = "nv_tt", nullable = false)
    private Integer nvTt;

    @Column(name = "diem_thxt", precision = 10, scale = 5)
    private BigDecimal diemThxt;

    @Column(name = "diem_utqd", precision = 10, scale = 5)
    private BigDecimal diemUtqd;

    @Column(name = "diem_cong", precision = 6, scale = 2)
    private BigDecimal diemCong;

    @Column(name = "diem_xettuyen", precision = 10, scale = 5)
    private BigDecimal diemXettuyen;

    @Column(name = "nv_ketqua", length = 45)
    private String nvKetqua;

    // nn_cccd_manganh_tt_phuongthuc
    @Column(name = "nv_keys", length = 45, unique = true)
    private String nvKeys;

    @Column(name = "tt_phuongthuc", length = 45)
    private String ttPhuongthuc;

    @Column(name = "tt_thm", length = 45)
    private String ttThm;
}