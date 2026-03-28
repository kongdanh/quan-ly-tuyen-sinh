package com.tuyensinh.model;

import java.util.ArrayList;
import java.util.List;

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
@Table(name = "xt_thisinhxettuyen25")
public class ThiSinh {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "idthisinh")
  private Integer idthisinh;

  @Column(name = "cccd", unique = true, nullable = false)
  private String cccd;

  @OneToMany(mappedBy = "thiSinh", cascade = {CascadeType.PERSIST, CascadeType.MERGE})
  @ToString.Exclude
  @EqualsAndHashCode.Exclude
  private List<ThiSinhAccount> thiSinhAccounts = new ArrayList<>();

  @OneToMany(mappedBy = "thiSinh", cascade = {CascadeType.PERSIST, CascadeType.MERGE})
  @ToString.Exclude
  @EqualsAndHashCode.Exclude
  private List<DiemThiXetTuyen> diemThiXetTuyens = new ArrayList<>();

  @OneToMany(mappedBy = "thiSinh", cascade = CascadeType.ALL)
  @ToString.Exclude
  @EqualsAndHashCode.Exclude
  private List<NguyenVong> danhSachNguyenvong = new ArrayList<>();

  @OneToMany(mappedBy = "thiSinh", cascade = CascadeType.ALL)
  @ToString.Exclude
  @EqualsAndHashCode.Exclude
  private List<DiemCong> danhSachDiemCong = new ArrayList<>();

  @Column(name = "sobaodanh")
  private String sobaodanh;

  @Column(name = "ho")
  private String ho;

  @Column(name = "ten")
  private String ten;

  @Column(name = "ngay_sinh")
  private String ngaySinh;

  @Column(name = "dien_thoai")
  private String dienThoai;

  @Column(name = "gioi_tinh")
  private String gioiTinh;

  @Column(name = "email")
  private String email;

  @Column(name = "noi_sinh")
  private String noiSinh;

  @Column(name = "updated_at")
  private java.time.LocalDateTime updatedAt;

  @Column(name = "doi_tuong")
  private String doiTuong;

  @Column(name = "khu_vuc")
  private String khuVuc;

}
