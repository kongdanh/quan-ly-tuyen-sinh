package com.tuyensinh.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "xt_users")
public class User {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id")
  private Integer id;

  @Column(name = "username")
  private String username;

  @Column(name = "password_hash")
  private String passwordHash;

  @Column(name = "ho_ten")
  private String hoTen;

  @Column(name = "email")
  private String email;

  @Column(name = "bo_phan")
  private String boPhan;

  @Column(name = "id_nhom")
  private Integer idNhom;

  @Column(name = "trang_thai")
  private String trangThai;

  @Column(name = "ngay_tao")
  private java.util.Date ngayTao;

}
