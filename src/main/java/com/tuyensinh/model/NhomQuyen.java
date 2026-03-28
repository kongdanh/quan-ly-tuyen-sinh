package com.tuyensinh.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "xt_nhom_quyen")
public class NhomQuyen {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id")
  private Integer id;

  @Column(name = "ma_nhom")
  private String maNhom;

  @Column(name = "ten_nhom")
  private String tenNhom;

  @Column(name = "mo_ta")
  private String moTa;

  @Column(name = "trang_thai")
  private String trangThai;

  @Column(name = "ngay_tao")
  private java.util.Date ngayTao;

}
