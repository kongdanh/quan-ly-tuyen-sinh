package com.tuyensinh.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "xt_thisinh_account")
public class ThiSinhAccount {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id")
  private Integer id;

  @ManyToOne
  @ToString.Exclude
  @EqualsAndHashCode.Exclude
  @JoinColumn(name = "cccd", referencedColumnName = "cccd")
  private ThiSinh thiSinh;

  @Column(name = "password_hash")
  private String passwordHash;

  @Column(name = "trang_thai")
  private String trangThai;

  @Column(name = "lan_dang_nhap_cuoi")
  private java.time.LocalDateTime lanDangNhapCuoi;

  @Column(name = "ngay_tao")
  private java.time.LocalDateTime ngayTao;

}
