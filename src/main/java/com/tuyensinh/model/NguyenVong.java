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
@Table(name = "xt_nguyenvongxettuyen")
public class NguyenVong {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "idnv")
  private Integer idnv;

  @ManyToOne
  @ToString.Exclude
  @EqualsAndHashCode.Exclude
  @JoinColumn(name = "nn_cccd", referencedColumnName = "cccd")
  private ThiSinh thiSinh;

  @ManyToOne
  @ToString.Exclude
  @EqualsAndHashCode.Exclude
  @JoinColumn(name = "nv_manganh", referencedColumnName = "manganh")
  private Nganh nganh;

  @Column(name = "nv_tt")
  private Integer nvTt;

  @Column(name = "diem_thxt")
  private Double diemThxt;

  @Column(name = "diem_utqd")
  private Double diemUtqd;

  @Column(name = "diem_cong")
  private Double diemCong;

  @Column(name = "diem_xettuyen")
  private Double diemXettuyen;

  @Column(name = "nv_ketqua")
  private String nvKetqua;

  @Column(name = "nv_keys", unique = true, nullable = false)
  private String nvKeys;

  @Column(name = "tt_phuongthuc")
  private String ttPhuongthuc;

  @ManyToOne
  @ToString.Exclude
  @EqualsAndHashCode.Exclude
  @JoinColumn(name = "tt_thm", referencedColumnName = "matohop")
  private ToHopMon toHopMon;

}
