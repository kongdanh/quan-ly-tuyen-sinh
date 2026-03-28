package com.tuyensinh.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "xt_quyen_chuc_nang")
public class QuyenChucNang {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id")
  private Integer id;

  @Column(name = "id_nhom")
  private Integer idNhom;

  @Column(name = "ma_chuc_nang")
  private String maChucNang;

  @Column(name = "co_xem")
  private Integer coXem;

  @Column(name = "co_them")
  private Integer coThem;

  @Column(name = "co_sua")
  private Integer coSua;

  @Column(name = "co_xoa")
  private Integer coXoa;

  @Column(name = "co_xuat")
  private Integer coXuat;

}
