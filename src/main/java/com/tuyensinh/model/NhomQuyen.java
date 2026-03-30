package com.tuyensinh.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;

import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "xt_nhom_quyen")
public class NhomQuyen {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;

    @Column(name = "ma_nhom", nullable = false, length = 50)
    private String maNhom;

    @Column(name = "ten_nhom", nullable = false, length = 100)
    private String tenNhom;

    @Column(name = "mo_ta", length = 200)
    private String moTa;

    @Column(name = "trang_thai", nullable = false, length = 20)
    private String trangThai;

    @Column(name = "ngay_tao", nullable = false)
    private LocalDateTime ngayTao;
}