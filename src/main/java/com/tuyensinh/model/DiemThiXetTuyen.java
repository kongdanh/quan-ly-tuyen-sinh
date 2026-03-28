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
@Table(name = "xt_diemthixettuyen")
public class DiemThiXetTuyen {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "iddiemthi")
  private Integer iddiemthi;

  @ManyToOne
  @ToString.Exclude
  @EqualsAndHashCode.Exclude
  @JoinColumn(name = "cccd", referencedColumnName = "cccd")
  private ThiSinh thiSinh;

  @Column(name = "sobaodanh")
  private String sobaodanh;

  @Column(name = "d_phuongthuc")
  private String dPhuongthuc;

  @Column(name = "TO")
  private Double to;

  @Column(name = "LI")
  private Double li;

  @Column(name = "HO")
  private Double ho;

  @Column(name = "SI")
  private Double si;

  @Column(name = "SU")
  private Double su;

  @Column(name = "DI")
  private Double di;

  @Column(name = "VA")
  private Double va;

  @Column(name = "N1_THI")
  private Double n1Thi;

  @Column(name = "N1_CC")
  private Double n1Cc;

  @Column(name = "CNCN")
  private Double cncn;

  @Column(name = "CNNN")
  private Double cnnn;

  @Column(name = "TI")
  private Double ti;

  @Column(name = "KTPL")
  private Double ktpl;

  @Column(name = "NL1")
  private Double nl1;

  @Column(name = "NK1")
  private Double nk1;

  @Column(name = "NK2")
  private Double nk2;

}
