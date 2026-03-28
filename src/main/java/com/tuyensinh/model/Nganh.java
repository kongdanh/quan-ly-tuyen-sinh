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
@Table(name = "xt_nganh")
public class Nganh {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "idnganh")
  private Integer idnganh;

  @Column(name = "manganh",unique = true, nullable = false)
  private String manganh;

  @OneToMany(mappedBy = "nganh", cascade = {CascadeType.PERSIST, CascadeType.MERGE})
  @ToString.Exclude
  @EqualsAndHashCode.Exclude
  private List<NganhToHop> nganhToHops = new ArrayList<>();

  @Column(name = "tennganh")
  private String tennganh;

  @Column(name = "n_tohopgoc")
  private String nTohopgoc;

  @Column(name = "n_chitieu")
  private Integer nChitieu;

  @Column(name = "n_diemsan")
  private Double nDiemsan;

  @Column(name = "n_diemtrungtuyen")
  private Double nDiemtrungtuyen;

  @Column(name = "n_tuyenthang")
  private String nTuyenthang;

  @Column(name = "n_dgnl")
  private String nDgnl;

  @Column(name = "n_thpt")
  private String nThpt;

  @Column(name = "n_vsat")
  private String nVsat;

  @Column(name = "sl_xtt")
  private Integer slXtt;

  @Column(name = "sl_dgnl")
  private Integer slDgnl;

  @Column(name = "sl_vsat")
  private Integer slVsat;

  @Column(name = "sl_thpt")
  private String slThpt;

}