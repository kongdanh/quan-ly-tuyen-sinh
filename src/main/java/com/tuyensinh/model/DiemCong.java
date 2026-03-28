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
@Table(name = "xt_diemcongxetuyen")
public class DiemCong {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "iddiemcong")
  private Integer iddiemcong;

  @ManyToOne
  @ToString.Exclude
  @EqualsAndHashCode.Exclude
  @JoinColumn(name = "ts_cccd", referencedColumnName = "cccd")
  private ThiSinh thiSinh;

  @ManyToOne
  @ToString.Exclude
  @EqualsAndHashCode.Exclude
  @JoinColumn(name = "manganh", referencedColumnName = "manganh")
  private Nganh nganh;

  @ManyToOne
  @ToString.Exclude
  @EqualsAndHashCode.Exclude
  @JoinColumn(name = "matohop", referencedColumnName = "matohop")
  private ToHopMon toHopMon;

  @Column(name = "phuongthuc")
  private String phuongthuc;

  @Column(name = "diemCC")
  private Double diemCC;

  @Column(name = "diemUtxt")
  private Double diemUtxt;

  @Column(name = "diemTong")
  private Double diemTong;

  @Column(name = "ghichu")
  private String ghichu;

  @Column(name = "dc_keys", unique = true, nullable = false)
  private String dcKeys;

}
