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
@Table(name = "xt_nganh_tohop")
public class NganhToHop {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id")
  private Integer id;

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

  @Column(name = "th_mon1")
  private String thMon1;

  @Column(name = "hsmon1")
  private Integer hsmon1;

  @Column(name = "th_mon2")
  private String thMon2;

  @Column(name = "hsmon2")
  private Integer hsmon2;

  @Column(name = "th_mon3")
  private String thMon3;

  @Column(name = "hsmon3")
  private Integer hsmon3;

  @Column(name = "tb_keys", unique = true, nullable = false)
  private String tbKeys;

  @Column(name = "N1")
  private Integer n1;

  @Column(name = "TO")
  private Integer to;

  @Column(name = "LI")
  private Integer li;

  @Column(name = "HO")
  private Integer ho;

  @Column(name = "SI")
  private Integer si;

  @Column(name = "VA")
  private Integer va;

  @Column(name = "SU")
  private Integer su;

  @Column(name = "DI")
  private Integer di;

  @Column(name = "TI")
  private Integer ti;

  @Column(name = "KHAC")
  private Integer khac;

  @Column(name = "KTPL")
  private Integer ktpl;

  @Column(name = "dolech")
  private Double dolech;

}
