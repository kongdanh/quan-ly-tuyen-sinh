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
@Table(name = "xt_bangquydoi")
public class BangQuyDoi {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "idqd")
  private Integer idqd;

  @Column(name = "d_phuongthuc")
  private String dPhuongthuc;

  @ManyToOne
  @ToString.Exclude
  @EqualsAndHashCode.Exclude
  @JoinColumn(name = "d_tohop", referencedColumnName = "matohop")
  private ToHopMon toHopMon;

  @Column(name = "d_mon")
  private String dMon;

  @Column(name = "d_diema")
  private Double dDiema;

  @Column(name = "d_diemb")
  private Double dDiemb;

  @Column(name = "d_diemc")
  private Double dDiemc;

  @Column(name = "d_diemd")
  private Double dDiemd;

  @Column(name = "d_maquydoi", unique = true, nullable = false)
  private String dMaquydoi;

  @Column(name = "d_phanvi")
  private String dPhanvi;

}
