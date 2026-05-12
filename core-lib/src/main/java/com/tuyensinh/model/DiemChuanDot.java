package com.tuyensinh.model;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;

@Entity
@Table(name = "xt_diem_chuan_dot")
@Data
public class DiemChuanDot {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_dot")
    private DotTuyenSinh dotTuyenSinh;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_nganh_tohop")
    private NganhToHop nganhToHop;

    @Column(name = "diem_chuan")
    private BigDecimal diemChuan;
}