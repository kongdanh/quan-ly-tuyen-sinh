package com.tuyensinh.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "xt_quyen_chuc_nang")
public class QuyenChucNang {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_nhom", nullable = false)
    private NhomQuyen nhomQuyen;

    @Column(name = "ma_chuc_nang", nullable = false, length = 50)
    private String maChucNang;

    @Column(name = "co_xem", nullable = false)
    private Boolean coXem;

    @Column(name = "co_them", nullable = false)
    private Boolean coThem;

    @Column(name = "co_sua", nullable = false)
    private Boolean coSua;

    @Column(name = "co_xoa", nullable = false)
    private Boolean coXoa;

    @Column(name = "co_xuat", nullable = false)
    private Boolean coXuat;
}